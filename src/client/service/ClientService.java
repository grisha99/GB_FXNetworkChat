package client.service;


import client.interf.Controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientService {


    private Socket socket;

    private DataInputStream dis;
    private DataOutputStream dos;
    private Controller guiController;   // контроллер графиче кого интерфейса
//
    private boolean isAuthorized;       // признак успешной авторизации

    public ClientService(Controller guiController) {
        this.guiController = guiController;
        isAuthorized = false;

    }

    public void startServerListener() {
        try {
            socket = new Socket("localhost", 55555);

            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            Thread readThread = new Thread(new Runnable() {     // поток прослушивания смс от ервера
                @Override
                public void run() {
                    try {
                        while (true) {
                            String strFromServer = dis.readUTF();
                            if (strFromServer.startsWith("/authOK")) {      // если авторизация успешна
                                String myNick = strFromServer.split("\\s")[1];
                                isAuthorized = true;
                                guiController.sendMsgToGUI("Вы авторизованы под ником: " + myNick);
                                break;
                            }
                            if (strFromServer.startsWith("/authTimeOut")) {     // от сервера получено смс о таймауте
                                isAuthorized = false;
                                break;
                            }
                            guiController.sendMsgToGUI(strFromServer);
                        }

                        if (isAuthorized) {     // авторизованы, слушаем сервер
                            while (true) {
                                String strFromServer = dis.readUTF();
                                if (strFromServer.startsWith("/exit")) {    // команда на выход, была запрошена нами
                                    isAuthorized = false;
                                    break;
                                }
                                if (strFromServer.startsWith("/clients")) {     // рассылка списка клиентов
                                    strFromServer = strFromServer.replace("/clients", "В сети:");
                                }
                                guiController.sendMsgToGUI(strFromServer);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        closeConnection();
                    }
                }
            });
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        if (socket == null || socket.isClosed()) {  // если были отключены или вышли сами, запускаем поток заново
            startServerListener();
        }
        try {
            dos.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!isAuthorized) {
            guiController.sendMsgToGUI("Вы не авторизованы!");
        }
    }

    private void closeConnection() {
        try {
            dos.close();
            dis.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
