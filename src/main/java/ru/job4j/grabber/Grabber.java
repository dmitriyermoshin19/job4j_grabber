package ru.job4j.grabber;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import java.sql.Connection;

public class Grabber implements Grab {
    private static final Logger LOG = LogManager.getLogger(Grabber.class.getName());

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
            String times = new Config().get("cron.time");
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

    public static void main(String[] args) throws SchedulerException {
        Config config = new Config();
        Connection connection = ConnectionManager.withoutRollback(config);
        SqlStore sqlStore = new SqlStore(connection);
        HtmlParse htmlParse = new HtmlParse();
        Grabber grabber = new Grabber();
        grabber.init(htmlParse, sqlStore, grabber.scheduler());
    }
}
