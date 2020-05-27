package ru.job4j.grabber;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;

import java.time.LocalDateTime;
import java.util.List;

public class GrabJob implements Job {
    private static final Logger LOG = LogManager.getLogger(GrabJob.class.getName());

    private static void setLastLaunchDate(HtmlParse htmlParse, SqlStore sqlStore) {
        LocalDateTime lastDate = sqlStore.getLastDate();
        if (lastDate != null) {
            htmlParse.setLastDate(lastDate);
            LOG.info(String.format("Last launch date: %s", lastDate.toString()));
        } else {
            htmlParse.setLastDate(LocalDateTime.of(
                    LocalDateTime.now().getYear(), 5, 1, 0, 0, 0));
            LOG.info("First launch.");
        }
    }

    @Override
    public void execute(JobExecutionContext context) {
        LOG.info("Start program...");
        JobDataMap map = context.getJobDetail().getJobDataMap();
        SqlStore sqlStore = (SqlStore) map.get("store");
        HtmlParse htmlParse = (HtmlParse) map.get("parse");
        GrabJob.setLastLaunchDate(htmlParse, sqlStore);
        String link = new Config().get("link");
        LOG.info(String.format("InitGrab with : %s", link));
        List<Post> posts = htmlParse.parser(link);
        sqlStore.save(posts);
        sqlStore.saveDate(LocalDateTime.now());
        LOG.info("Program completed.");
    }
}
