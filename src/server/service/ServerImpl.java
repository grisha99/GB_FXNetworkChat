package server.service;

import server.except.ServerErrorException;
import server.handler.ClientHandler;
import server.interf.AuthService;
import server.interf.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ServerImpl implements Server {

    private List<ClientHandler> clients;
    private AuthService authService;

    public ServerImpl() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            authService = new AuthServiceImpl();
            authService.start();;
            clients = new LinkedList<>();
            while (true) {
                System.out.println("Ожидание подключения");
                Socket socket = serverSocket.accept();
                System.out.println("Клиент подключен");
                new ClientHandler(this, socket);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServerErrorException see) {
            System.out.println(see.getMessage());
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    @Override
    public synchronized boolean isNickBusy(String nick) {
        for(ClientHandler c : clients) {
            if(c.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void broadcastMsg(String msg) {
        for(ClientHandler c : clients) {
            c.sendMessage(msg);
        }
    }

    @Override
    public void broadcastClientsList() {
        StringBuilder clientsListMsg = new StringBuilder("/clients ");
        for(ClientHandler ch : clients) {
            clientsListMsg.append(ch.getNick() + " ");
        }
        broadcastMsg(clientsListMsg.toString());
    }

    @Override
    public boolean sendPrivateMag(String msg, String toNick, ClientHandler fromClient) {
        ClientHandler toClient = getClientHandlerByNick(toNick);
        if (toClient != null) {
            toClient.sendMessage("ВАМ от " + fromClient.getNick() + ": " + msg);
            return true;
        } else {
            fromClient.sendMessage("Пользователь с ником \"" + toNick + "\" не в сети.");
            return false;
        }
    }

    @Override
    public synchronized void subscribe(ClientHandler client) {
        clients.add(client);
        broadcastClientsList();
    }

    @Override
    public synchronized void unSubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastClientsList();
    }

    @Override
    public AuthService getAuthService() {
        return authService;
    }

    @Override
    public ClientHandler getClientHandlerByNick(String nick) {
        for(ClientHandler ch : clients) {
            if(ch.getNick().equals(nick)) {
                return ch;
            }
        }
        return null;
    }
}