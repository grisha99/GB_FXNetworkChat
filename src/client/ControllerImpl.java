package client;

import client.interf.Controller;
import client.service.ClientService;
import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class ControllerImpl implements Controller {

    @FXML
    TextField newMessageField;
    @FXML
    TextArea messageList;
    @FXML
    Button okButton;

    private ClientService cService;

    public ControllerImpl() {
        cService = new ClientService(this);
    }


    public void addNewMessage(ActionEvent actionEvent) {
        String newText = newMessageField.getText().trim();
        if (!newText.equals("")) {                          // отправляем сообщение, если текст сообщения не пустой
//            messageList.appendText(newText + "\n");
            sendMsgToServer(newText);
            newMessageField.clear();
            newMessageField.requestFocus();
            okButton.setDisable(true);                      // сообщение пустое, выключаем кнопку
        }
    }


    public void changeText(KeyEvent keyEvent) {
        okButton.setDisable(newMessageField.getText().trim().equals("")); // состояние кнопки если текст пустой
    }

    @Override
    public void sendMsgToGUI(String msg) {

        messageList.appendText(msg + "\n");
    }

    @Override
    public void sendMsgToServer(String msg) {

        cService.sendMsg(msg);
    }

}
