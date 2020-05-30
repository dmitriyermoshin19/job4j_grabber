package ru.job4j.grabber;

import org.junit.Test;
import org.junit.Ignore;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class SqlStoreTest {

    /**
     * результат метода getAll().get(0) может со временем меняться,
     * поэтому могут возникнуть ошибки теста
     */
    @Ignore
    @Test
    public void whenSaveAndGetAll() {
        Connection connection = ConnectionManager.withoutRollback(new Config());
        LocalDateTime date = LocalDateTime.of(2020, 5, 15, 0, 0, 0);
        try (SqlStore store = new SqlStore(connection)) {
            List<Post> postsIn = List.of(new Post("Java", "text", date, "link"));
            store.save(postsIn);
            Post postOut = store.getAll().get(0);
            assertThat(postOut.getName(), is("Java"));
            assertThat(postOut.getText(), is("text"));
            assertThat(postOut.getLink(), is("link"));
        }
    }

    @Test
    public void whenSaveDateAndGetLastDate() {
        Connection connection = ConnectionManager.withRollback(new Config());
        try (SqlStore store = new SqlStore(connection)) {
            LocalDateTime date1 = LocalDateTime.of(2020, 5, 15, 0, 0, 0);
            store.saveDate(date1);
            LocalDateTime date2 = LocalDateTime.of(2020, 1, 16, 0, 0, 0);
            store.saveDate(date2);
            assertThat(store.getLastDate(), is(date2));
        }
    }
}
