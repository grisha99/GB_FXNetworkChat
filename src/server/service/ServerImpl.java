package server.service;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.except.ServerErrorException;
import server.handler.ClientHandler;
import server.interf.AuthService;
import server.interf.Server;
import server.interf.UserDAO;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ServerImpl implements Server {

    private List<ClientHandler> clients;
    private AuthService authService;
    private UserDAO userDAO;
    private static final Logger LOGGER = LogManager.getLogger(ServerImpl.class.getName());


    public ServerImpl() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            userDAO = new UserDAOImpl();
            authService = new AuthServiceImpl(userDAO);
            authService.start();;
            clients = new LinkedList<>();
            while (true) {
                LOGGER.log(Level.INFO, "Ожидание подключения");
                Socket socket = serverSocket.accept();
                LOGGER.log(Level.INFO, "Клиент подключился");
                ExecutorService es = Executors.newCachedThreadPool();
                es.execute(new ClientHandler(this, socket));
            }
        } catch (IOException e) {
            LOGGER.error("ErrorIO", e);
        } catch (ServerErrorException see) {
            LOGGER.error("ErrorServer", see);
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
    public synchronized void broadcastClientsList() {
        StringBuilder clientsListMsg = new StringBuilder("/clients ");
        for(ClientHandler ch : clients) {
            clientsListMsg.append(ch.getNick() + " ");
        }
        broadcastMsg(clientsListMsg.toString());
    }

    @Override
    public synchronized boolean sendPrivateMag(String msg, String toNick, ClientHandler fromClient) {
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
        LOGGER.log(Level.INFO, "Зарегистрирован клиент с ником: " + client.getNick());
    }

    @Override
    public synchronized void unSubscribe(ClientHandler client) {
        clients.remove(client);
        broadcastClientsList();
        LOGGER.log(Level.INFO, "Клиент отключился, ник: " + client.getNick());
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

    @Override
    public synchronized boolean changeNick(ClientHandler sender, String newNick) {
        if (userDAO.changeUserNick(sender.getNick(), newNick)) {
            broadcastMsg("Ник изменен: " + sender.getNick() + " -> " + newNick);
            LOGGER.log(Level.INFO, "Клиент изменил Ник: " + sender.getNick() + " -> " + newNick);
            return true;
        } else {
            sender.sendMessage("Пользователь с ником \"" + newNick + "\" уже существует.");
            LOGGER.log(Level.INFO, "Клиент не смог сменить Ник: " + sender.getNick() + " -> " + newNick + " (Ник занят)");
            return false;
        }
    }
}
