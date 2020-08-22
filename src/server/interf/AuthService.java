package server.interf;

public interface AuthService {

    void start();

    String getNick(String login, String pass);

    void stop();
}
