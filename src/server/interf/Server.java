package server.interf;

import server.handler.ClientHandler;

public interface Server {

    int PORT = 55555;

    int AUTH_TIME_OUT = 120000; // миллисекунды, таймаут авторизации

    boolean isNickBusy(String nick);

    void broadcastMsg(String msg);

    void broadcastClientsList();

    boolean sendPrivateMag(String msg, String toClient, ClientHandler fromClient);

    void subscribe(ClientHandler client);

    void unSubscribe(ClientHandler client);

    AuthService getAuthService();

    ClientHandler getClientHandlerByNick(String nick);

    boolean changeNick(ClientHandler sender, String newNick);
}
