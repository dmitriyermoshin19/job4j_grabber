package ru.job4j.grabber;

import org.junit.Test;
import org.junit.Ignore;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class HtmlParseTest {
    private HtmlParse htmlParse = new HtmlParse();

    @Test
    public void whenDetail() {
        String postLink = "https://www.sql.ru/forum/1318912/java-developer";
        Post post = htmlParse.detail(postLink);
        assertThat(post.getName(), is("Java developer"));
        assertThat(post.getText().startsWith("Компания BizApps")
                && post.getText().endsWith("Ваших откликов!"), is(true));
        assertThat(post.getLink(), is(postLink));
        assertThat(post.getDate(), is(LocalDateTime.of(2019, 11, 6, 15, 25)));
    }

    /**
     * результат метода parser будет со временем меняться,
     * поэтому могут возникнуть ошибки теста
     */
    @Ignore
    @Test
    public void whenParser() {
        String link = "https://www.sql.ru/forum/job-offers/";
        htmlParse.setLastDate(LocalDateTime.of(2021, 1, 1, 0, 0));
        List<Post> list = htmlParse.parser(link);
        assertThat(list.size(), is(21));
    }
}
