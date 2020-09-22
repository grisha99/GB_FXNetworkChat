package server.service;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.interf.UserDAO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAOImpl implements UserDAO {

    private PreparedStatement ps;

    private static final Logger LOGGER = LogManager.getLogger(UserDAOImpl.class.getName());

    @Override
    public String getUserNick(String login, String pass) {

        ResultSet rs;
        try {
            ps = DBConnect.getInstance().getConnection()
                    .prepareStatement("SELECT nick FROM `netchat`.`users` WHERE (`login` = ? and `pass` = ?);");
            ps.setString(1, login);
            ps.setString(2, pass);
            rs = ps.executeQuery();
        } catch (SQLException e) {
            LOGGER.error("Ошибка получения Ника из БД", e);
            LOGGER.log(Level.ERROR, "Запрос к ДБ: " + ps.toString());
            return null;
        }
        try {
            if (rs.next()) {
                return rs.getString("nick");
            }

        } catch (SQLException e) {
            LOGGER.error("Ошибка чтения ResultSet", e);
            return null;
        }
        return null;
    }

    @Override
    public boolean changeUserNick(String oldNick, String newNick) {
        try {
            ps = DBConnect.getInstance().getConnection()
                    .prepareStatement("UPDATE `netchat`.`users` SET `nick` = ? WHERE (`nick` = ?);");
            ps.setString(1, newNick);
            ps.setString(2, oldNick);
            ps.execute();
        } catch (SQLException e) {
            LOGGER.error("Ошибка изменения Ника", e);
            LOGGER.log(Level.DEBUG, ps.toString());
            LOGGER.log(Level.DEBUG, "Возможно ник уже есть в БД: " + newNick);
            return false;
        }
        return true;
    }

}
