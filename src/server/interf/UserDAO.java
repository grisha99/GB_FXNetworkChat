package server.interf;

public interface UserDAO {

    String getUserNick(String login, String pass);

    boolean changeUserNick(String oldNick, String newNick);

}
