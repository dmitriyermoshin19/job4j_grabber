package ru.job4j.grabber;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.quartz.SchedulerException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Locale;

public class HtmlParse implements Parse {
    private static final Logger LOG = LogManager.getLogger(HtmlParse.class.getName());
    private LocalDateTime lastDate = null;
    private LocalDateTime postDateTime = null;
    private DateTimeFormatter fmt = new DateTimeFormatterBuilder()
            .appendPattern("d ")
            .appendText(ChronoField.MONTH_OF_YEAR, new HashMap<>() { {
                put(1L, "янв"); put(2L,  "фев"); put(3L,  "мар"); put(4L,  "апр");
                put(5L, "май"); put(6L,  "июн"); put(7L,  "июл"); put(8L,  "авг");
                put(9L, "сен"); put(10L, "окт"); put(11L, "ноя"); put(12L, "дек");
            } })
            .appendPattern(" yy, HH:mm")
            .toFormatter(new Locale("ru"));

    public void setLastDate(LocalDateTime lastDate) {
        this.lastDate = lastDate;
    }

    private LocalDateTime parseDate(String date) {
        LocalDateTime dateOut;
        if (date.contains("сегодня")) {
            dateOut = LocalDateTime.of(
                    LocalDate.now(),
                    LocalTime.parse(date.split(" ")[1])
            );
        } else if (date.contains("вчера")) {
            dateOut = LocalDateTime.of(
                    LocalDate.now().minusDays(1L),
                    LocalTime.parse(date.split(" ")[1])
            );
        } else {
            dateOut = LocalDateTime.parse(date, fmt);
        }
        return dateOut;
    }

    private boolean filter(String nameTopic) {
        return Pattern.compile("(java)(?!\\s?script)", Pattern.CASE_INSENSITIVE)
                .matcher(nameTopic).find();
    }

    private String nextPageLink(Document doc) {
        int current = Integer.parseInt(doc.select("table.sort_options td b").text());
        return doc.select(String.format("table.sort_options td a:contains(%s)", current + 1))
                .attr("href");
    }

    @Override
    public Post detail(String postLink) {
        Post post = null;
        try {
            Document doc = Jsoup.connect(postLink).get();
            String name = doc.title().split(" / Вакансии")[0];
            String text = doc.select("td.msgBody").get(1).text();
            String date = doc.select("td.msgFooter").get(0).text().split("\\s+\\[\\d+\\]")[0]; /*
            String date = doc.select("td.msgFooter").get(0).text();
            Matcher matcher = Pattern.compile("\\d{1,2}\\s+\\p{L}{3}\\s+\\d{1,2},\\s+\\d{1,2}:\\d{1,2}")
                    .matcher(date);
            if (matcher.find()) {
                date = matcher.group();
            }*/
            this.postDateTime = this.parseDate(date);
            post = new Post(name, text, postDateTime, postLink);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return post;
    }

    @Override
    public List<Post> parser(String linkPage) {
        LOG.info("Start parsing...");
        List<Post> posts = new ArrayList<>();
        try {
            exitlabel: do {
                Document doc = Jsoup.connect(linkPage).get();
                Elements table = doc.select("table.forumtable tr:has(td)");
                for (Element tr : table) {
                    Elements td = tr.select("td");
                    String nameTopic = td.get(1).text();
                    String dateTopic = td.get(5).text();
                    if (this.filter(nameTopic)) {
                        LocalDateTime date = this.parseDate(dateTopic);
                        if (lastDate != null && lastDate.isAfter(date)) {
                            break exitlabel;
                        }
                        String hrefPost = td.get(1).select("a").attr("href");
                        Post post = this.detail(hrefPost);
                        if (postDateTime.isAfter(lastDate)) {
                            posts.add(post);
                        }
                    }
                }
                linkPage = this.nextPageLink(doc);
            } while (!linkPage.isEmpty());
            Collections.sort(posts);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        LOG.info(String.format("%s posts found.", posts.size()));
        return posts;
    }

    public static void main(String[] args) throws SchedulerException {
        String link = "https://www.sql.ru/forum/job-offers/";
        HtmlParse htmlParse = new HtmlParse();
        htmlParse.setLastDate(LocalDateTime.of(2021, 1, 1, 0, 0));
        List<Post> list = htmlParse.parser(link);
        for (Post p : list) {
            System.out.println(p);
        }
    }


}
