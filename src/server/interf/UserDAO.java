package server.interf;

import server.service.AuthServiceImpl;

import java.sql.ResultSet;

public interface UserDAO {

    ResultSet getUserSet();

    boolean changeUserNick(String oldNick, String newNick);

}
