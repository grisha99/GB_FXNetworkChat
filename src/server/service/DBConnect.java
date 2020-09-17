package server.service;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public class DBConnect {

    private Connection connection;

    private static DBConnect dbConnectImpl;

    private static final Logger LOGGER = LogManager.getLogger(DBConnect.class.getName());

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
            LOGGER.log(Level.INFO, "Успешное соединение БД: \"" + db + "\', сервер: " + host + ":" + port );
        } catch (SQLException e) {
            LOGGER.error("ОШИБКА соединения с БД: \"" + db + "\', сервер: " + host + ":" + port, e);
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
