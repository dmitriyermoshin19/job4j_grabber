package ru.job4j.grabber;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    private static final Logger LOG = LogManager.getLogger(ConnectionManager.class.getName());

    private static Connection init(Config config) {
        Connection connection = null;
        try  {
            Class.forName(config.get("driver-class-name"));
            connection = DriverManager.getConnection(
                    config.get("url"),
                    config.get("username"),
                    config.get("password")
            );
            LOG.info("Connection completed successfully.");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return connection;
    }

    public static Connection withoutRollback(Config config) {
        return init(config);
    }

    public static Connection withRollback(Config config) {
        Connection connection = init(config);
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return (Connection) Proxy.newProxyInstance(
                ConnectionManager.class.getClassLoader(),
                new Class[] {
                        Connection.class
                },
                (proxy, method, args) -> {
                    Object rsl = null;
                    if ("close".equals(method.getName())) {
                        connection.rollback();
                        connection.close();
                    } else {
                        rsl = method.invoke(connection, args);
                    }
                    return rsl;
                }
        );
    }
}
