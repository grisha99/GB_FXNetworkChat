package server.handler;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.except.ServerErrorException;
import server.interf.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Server server;

    private Socket socket;

    private DataInputStream dis;
    private DataOutputStream dos;

    private String nick;

    private volatile boolean isAuthOK;          // флаг успешной авторизации
    private volatile boolean exitWithoutAuth;   // флаг выхода без авторизации

    public static final Logger LOGGER = LogManager.getLogger(ClientHandler.class.getName());

    public ClientHandler(Server server, Socket socket) throws ServerErrorException {
        try {
            this.server = server;
            this.socket = socket;
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            LOGGER.error("ErrorCHConstructor", e);
            throw new ServerErrorException("Проблеммы на сервере");
        }
    }

    @Override
    public void run() {
        try {
            isAuthOK = false;
            exitWithoutAuth = false;
            Thread authThread = new Thread(() -> {  // отдельный поток для авторизации
                try {
                    authentication();
                } catch (IOException e) {
                    LOGGER.error("ErrorCHAuth", e);
                }
            });
            authThread.setDaemon(true);
            authThread.start();
            authThread.join(server.AUTH_TIME_OUT);         // таймаут авторизации
            if (isAuthOK) {                       // если авторизованы, сулшаем смс
                LOGGER.log(Level.INFO, "Клиент авторизован, ник: " + nick);
                readMessage();
            } else {                                       // авторизация по таймауту не прошла
                if (!exitWithoutAuth) {
                    sendMessage("Таймаут авторизации, вы отключены.");
                    sendMessage("/authTimeOut");          // смс клиенту об этом
                    LOGGER.log(Level.INFO, "Таймаут авторизации, клиент отключен ");
                }
                if (authThread.isAlive()) {
                    authThread.interrupt();
                    authThread.join();
                }
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("ErrorCH", e);
        } finally {
            closeConnection();
        }
    }

    private void authentication() throws IOException{
        String authNick;
        while (true) {
            String strFromClient = dis.readUTF();
            if (strFromClient.startsWith("/exitNoAuth") || strFromClient.startsWith("/exit")) {
                isAuthOK = false;
                exitWithoutAuth = true;
                return;
            }
            if (strFromClient.startsWith("/auth")) {                // команда на авторизацию
                String [] clientData = strFromClient.split("\\s");
                try {
                    authNick = server.getAuthService().getNick(clientData[1], clientData[2]); // проверка логина пароля
                } catch (ArrayIndexOutOfBoundsException e) {
                    dos.writeUTF("Не указаны логин или пароль");
                    LOGGER.log(Level.INFO, "Не указаны логин или пароль");
                    continue;
                }
                if (authNick != null) {
                    if (!server.isNickBusy(authNick)) {     // занят ли ник
                        sendMessage( "/authOK " + authNick + " " + clientData[1]);
                        this.nick = authNick;
                        server.broadcastMsg( this.nick + " вошел в чат");
                        server.subscribe(this);
                        isAuthOK = true;
                        return;
                    } else {
                        dos.writeUTF("Под вашим ником уже кто-то в сети");
                        LOGGER.log(Level.DEBUG, "Попытка входа под чужим ником: " + authNick);
                    }
                } else {
                    dos.writeUTF("Не правильный логин или пароль");
                    LOGGER.log(Level.DEBUG, "Не правильный логин пароль: \"" + clientData[1] + "\" - \"" + clientData[2] + "\"");
                }
            } else {
                dos.writeUTF("Не правильная команда авторизации.");
                dos.writeUTF("Наберите: \"/auth логин пароль\"");
                LOGGER.log(Level.DEBUG, "Не правильная команда авторизации");
            }
        }
    }

    public void sendMessage(String msg) {
        try {
            if (socket != null && !socket.isClosed()) {
                dos.writeUTF(msg);
            }
        } catch (IOException e) {
            LOGGER.error("ErrorCHSendMsg", e);
        }
    }

    private void readMessage() throws IOException{
        while (true) {
            String strFromClient = dis.readUTF();
            if (strFromClient.startsWith("/")) {        // признак команды
                if (strFromClient.startsWith("/exit")) {    //  клиент вызодит
                    sendMessage("/exit");              // подтверждение выхода
                    return;
                }
                if (strFromClient.startsWith("/w")) {       // приветное смс
                    String toNick = strFromClient.split("\\s")[1];
                    String msg = strFromClient.substring(2 + toNick.length() + 1);
                    if (server.sendPrivateMag(msg, toNick, this)){
                        sendMessage("Личное СМС для " + toNick + ": " + msg);
                    }
                }
                if (strFromClient.startsWith("/cnick")) {   // смена ника
                    String newNick = strFromClient.split("\\s")[1];
                    if (server.changeNick(this, newNick)) {
                        nick = newNick;
                        server.broadcastClientsList();
                    }
                }
                continue;       // команды отработаны
            }
            server.broadcastMsg(nick + ": " + strFromClient);
        }
    }

    private void closeConnection() {
        server.unSubscribe(this);
        if (isAuthOK) {
            server.broadcastMsg(this.nick + ": Вышел из чата");
        }

        try {
            dis.close();
            dos.close();
            socket.close();
            LOGGER.log(Level.INFO, "Успешное закрытие подключения, Ник: " + this.nick);
        } catch (IOException e) {
            LOGGER.error("Ошибка закрытия подключения", e);
        }
    }

    public String getNick() {
        return nick;
    }



}
