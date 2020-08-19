package client.service;

import client.ControllerImpl;
import client.interf.Controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientService {


    private Socket socket;

    private DataInputStream dis;
    private DataOutputStream dos;
    private Controller guiController;

    public ClientService(Controller guiController) {
        this.guiController = guiController;
        try {
            socket = new Socket("localhost", 55555);

            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
    //        setAuthorized(false);
            Thread readThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String strFromServer = dis.readUTF();
                            if (strFromServer.startsWith("/authOK")) {
                                String myNick = strFromServer.split("\\s")[1];
    //                            setAuthorized(true);
                                guiController.sendMsgToGUI("Вы авторизованы под ником: " + myNick);
                                break;
                            }
                            guiController.sendMsgToGUI(strFromServer);
                        }

                        while (true) {
                            String strFromServer = dis.readUTF();
                            if (strFromServer.startsWith("/exit")) {
                                break;
                            }
                            guiController.sendMsgToGUI(strFromServer);
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
        if (!socket.isClosed()) {
            try {
                dos.writeUTF(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            guiController.sendMsgToGUI("Вы отключены!");
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
