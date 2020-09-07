package server.handler;

import server.except.ServerErrorException;
import server.interf.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private Server server;

    private Socket socket;

    private DataInputStream dis;
    private DataOutputStream dos;

    private String nick;

    private boolean isAuthOK;   // флаг успешной авторизации

    public ClientHandler(Server server, Socket socket) throws ServerErrorException {
        try {
            this.server = server;
            this.socket = socket;
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            new Thread( () -> {     // поток прослушивания смс от клинта
                try {
                    isAuthOK = false;
                    Thread authThread = new Thread(() -> {  // отдельный поток для авторизации
                        try {
                            authentication();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    authThread.start();
                    authThread.join(server.AUTH_TIME_OUT);         // таймаут авторизации
                    if (isAuthOK) {                       // если авторизованы, сулшаем смс
                        readMessage();
                    } else {                                       // авторизация по таймауту не прошла
                        sendMessage("Таймаут авторизации, вы отключены.");
                        sendMessage("/authTimeOut");          // смс клиенту об этом
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new ServerErrorException("Проблеммы на сервере");
        }
    }

    private void authentication() throws IOException{
        while (true) {
            String strFromClient = dis.readUTF().toLowerCase();
            if (strFromClient.startsWith("/auth")) {                // команда на авторизацию
                String [] clientData = strFromClient.split("\\s");
                String nick = server.getAuthService().getNick(clientData[1], clientData[2]); // проверка логина пароля
                if (nick != null) {
                    if (!server.isNickBusy(nick)) {     // занят ли ник
                        sendMessage( "/authOK " + nick);
                        this.nick = nick;
                        server.broadcastMsg( this.nick + " вошел в чат");
                        server.subscribe(this);
                        isAuthOK = true;
                        return;
                    } else {
                        dos.writeUTF("Под вашим ником уже кто-то в сети");
                    }
                } else {
                    dos.writeUTF("Не правильный логин или пароль");
                }
            } else {
                dos.writeUTF("Не правильная команда авторизации.");
                dos.writeUTF("Наберите: \"/auth логин пароль\"");
            }
        }
    }

    public void sendMessage(String msg) {
        try {
            dos.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }

}
