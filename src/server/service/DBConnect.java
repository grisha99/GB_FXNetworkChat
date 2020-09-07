package server.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public class DBConnect {

    private Connection connection;

    private static DBConnect dbConnectImpl;

    private DBConnect(){
        ResourceBundle rb = ResourceBundle.getBundle("dbCon");
        String host = rb.getString("host");
        String port = rb.getString("port");
        String db = rb.getString("dbName");
        String login = rb.getString("user");
        String pass = rb.getString("pass");
        String timeZone = "serverTimezone=UTC";

        String jdbcURL = MessageFormat.format("jdbc:mysql://{0}:{1}/{2}?{3}"
                , host, port, db, timeZone);

        try {
            connection = DriverManager.getConnection(jdbcURL, login, pass);
        } catch (SQLException throwables) {
            System.out.println("Ошибка соединения с БД!");
            throwables.printStackTrace();
        }
    }

    public Connection getConnection() {

        return connection;
    }

    public static DBConnect getInstance() {

        if (dbConnectImpl == null) {
            dbConnectImpl = new DBConnect();
        }

        return dbConnectImpl;
    }
}
