package server.interf;

import server.handler.ClientHandler;

public interface Server {

    int PORT = 55555;

    boolean isNickBusy(String nick);

    void broadcastMsg(String msg);

    boolean sendPrivateMag(String msg, ClientHandler toClient, ClientHandler fromClient);

    void subscribe(ClientHandler client);

    void unSubscribe(ClientHandler client);

    AuthService getAuthService();

    ClientHandler getClientHandlerByNick(String nick);
}
