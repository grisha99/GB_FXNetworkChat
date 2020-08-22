package server.handler;

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

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            new Thread( () -> {
                try {
                    authentication();
                    readMessage();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Проблеммы на сервере"); //TODO дописать свое исключение
        }
    }

    private void authentication() throws IOException{
        while (true) {
            String strFromClient = dis.readUTF().toLowerCase();
            if (strFromClient.startsWith("/auth")) {
                String [] clientData = strFromClient.split("\\s");
                String nick = server.getAuthService().getNick(clientData[1], clientData[2]);
                if (nick != null) {
                    if (!server.isNickBusy(nick)) {
                        sendMessage( "/authOK " + nick);
                        this.nick = nick;
                        server.broadcastMsg( this.nick + " вошел в чат");
                        server.subscribe(this);
                        return;
                    } else {
//                        System.out.println("Под вашим ником уже кто-то в сети");
                        dos.writeUTF("Под вашим ником уже кто-то в сети");
                    }
                } else {
//                    System.out.println("Не правильный логин или пароль");
                    dos.writeUTF("Не правильный логин или пароль");
                }
            } else {
//                System.out.println("Не правильная команда авторизации.");
//                System.out.println("Наберите: \"/auth логин пароль\"");
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
            if (strFromClient.startsWith("/exit")) {
                sendMessage("/exit");
                return;
            }
//            System.out.println("от " + this.nick + ": " + strFromClient);
            if (strFromClient.startsWith("/w")) {
                String toNick = strFromClient.split("\\s")[1];
                String msg = strFromClient.substring(2 + toNick.length() + 1);
                if (server.sendPrivateMag(msg, server.getClientHandlerByNick(toNick), this)){
                    sendMessage("Личное СМС для " + toNick + ": " + msg);
                }
            } else {
                server.broadcastMsg(nick + ": " + strFromClient);
            }
        }

    }

    private void closeConnection() {
        server.unSubscribe(this);
        server.broadcastMsg(this.nick + ": Вышел из чата");

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
