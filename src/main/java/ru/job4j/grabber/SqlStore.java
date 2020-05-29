package ru.job4j.grabber;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqlStore implements Store, AutoCloseable {
    private static final Logger LOG = LogManager.getLogger(SqlStore.class.getName());
    private Connection connection;

    public SqlStore(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(List<Post> posts) {
        try (PreparedStatement ps = connection
                .prepareStatement("insert into post(name, text, date, link) values(?,?,?,?)")) {
            for (Post post : posts) {
                ps.setString(1, post.getName());
                ps.setString(2, post.getText());
                ps.setTimestamp(3, Timestamp.valueOf(post.getDate()));
                ps.setString(4, post.getLink());
                ps.addBatch();
            }
            ps.executeBatch();
            LOG.info(posts.size() + " new posts added to the database.");
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> list = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("select * from post;")) {
            while (rs.next()) {
                Post post = new Post(
                        rs.getString("name"),
                        rs.getString("text"),
                        rs.getTimestamp("date").toLocalDateTime(),
                        rs.getString("link"));
                list.add(post);
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return list;
    }

    public void saveDate(LocalDateTime date) {
        try (PreparedStatement ps = connection
                .prepareStatement("insert into log(date) values(?)")) {
            ps.setTimestamp(1, Timestamp.valueOf(date));
            ps.execute();
            LOG.info(String.format("Launch date saved: %s", date.toString()));
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public LocalDateTime getLastDate() {
        LocalDateTime lastDate = null;
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("select * from log")) {
            while (rs.next()) {
                lastDate = rs.getTimestamp("date").toLocalDateTime();
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return lastDate;
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
                LOG.info("Close database connection.");
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
