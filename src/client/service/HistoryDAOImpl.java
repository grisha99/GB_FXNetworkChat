package client.service;

import client.interf.HistoryDAO;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class HistoryDAOImpl implements HistoryDAO {

    private final int LOAD_MESSAGE_COUNT = 100;

    private File historyFile;

    public HistoryDAOImpl(String login){
        historyFile = new File(historyDirPath + "\\" + "history_" + login + ".txt");
        File dir = new File(historyFile.getParent());
        if (!dir.exists()) {    // есть ли каталог с историей, если нет, создаем
            dir.mkdir();
        }
        if (!historyFile.exists()) {    // есть ли файл с историей, если нет, создаем
            try {
                historyFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public List<String> getMessageList() {
        try (FileReader fr = new FileReader(historyFile);
             BufferedReader br = new BufferedReader(fr);){

            List<String> msgList = new LinkedList<>();
            String message = br.readLine();
            while (message != null) {   // чтение всей записанной истории
                msgList.add(message);
                message = br.readLine();
            }

            if (LOAD_MESSAGE_COUNT < msgList.size()) {  // обрезаем историю до нужного кол-во сообщений
                msgList = msgList.subList(msgList.size() - LOAD_MESSAGE_COUNT, msgList.size());

            }
            return msgList;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void addMessage(String message) {
        try (FileWriter fw = new FileWriter(historyFile, true)){
            fw.write(message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
