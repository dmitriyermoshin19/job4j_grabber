package ru.job4j.grabber;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.sql.Connection;

public class Grabber implements Grab {
    private static final Logger LOG = LogManager.getLogger(Grabber.class.getName());
    public Config config = new Config();

    private Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return scheduler;
    }

    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) {
        try {
            JobDataMap data = new JobDataMap();
            data.put("store", store);
            data.put("parse", parse);
            JobDetail job = newJob(GrabJob.class)
                    .usingJobData(data)
                    .build();
            String times = this.config.get("cron.time");
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(cronSchedule(times))
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(50000);
            scheduler.shutdown();
            System.out.println(store);
        } catch (SchedulerException | InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void web(Store store) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(Integer.parseInt(this.config.get("port")))) {
                boolean flag = true;
                while (flag) {
                    Socket socket = server.accept();
                    try (OutputStream out = socket.getOutputStream();
                         BufferedReader in = new BufferedReader(
                                 new InputStreamReader(socket.getInputStream()))) {
                        String str = in.readLine();
                        Set<String> words = Set.of(str.split("[= ]+"));
                        if (words.contains("getAll")) {
                            out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                            for (Post post : store.getAll()) {
                                out.write(post.toString().getBytes());
                                out.write(System.lineSeparator().getBytes());
                            }
                        } else if (words.contains("Exit")) {
                            flag = false;
                        } else {
                            out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                            out.write("What?".getBytes());
                        }
                        System.out.println();
                        while (!(str).isEmpty()) {
                            System.out.println(str);
                            str = in.readLine();
                        }
                        System.out.println();
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) throws SchedulerException {
        Config config = new Config();
        Connection connection = ConnectionManager.withoutRollback(config);
        SqlStore sqlStore = new SqlStore(connection);
        HtmlParse htmlParse = new HtmlParse();
        Grabber grabber = new Grabber();
        grabber.web(sqlStore);
        grabber.init(htmlParse, sqlStore, grabber.scheduler());

    }
}
