package server.service;

import server.interf.UserDAO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAOImpl implements UserDAO {

    private PreparedStatement ps;

    @Override
    public ResultSet getUserSet() {
        try {
            ps = DBConnect.getInstance().getConnection().prepareStatement("SELECT * FROM users");
            return ps.executeQuery();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
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
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        return true;
    }

}
