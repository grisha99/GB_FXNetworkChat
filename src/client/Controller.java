package client;

import javafx.event.ActionEvent;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class Controller {

    @FXML
    TextField newMessageField;
    @FXML
    TextArea messageList;
    @FXML
    Button okButton;

    public void addNewMessage(ActionEvent actionEvent) {
        String newText = newMessageField.getText().trim();
        if (!newText.equals("")) {                          // отправляем сообщение, если текст сообщения не пустой
            messageList.appendText(newText + "\n");
            newMessageField.clear();
            newMessageField.requestFocus();
            okButton.setDisable(true);                      // сообщение пустое, выключаем кнопку
        }
    }


    public void changeText(KeyEvent keyEvent) {
        okButton.setDisable(newMessageField.getText().trim().equals("")); // состояние кнопки если текст пустой
    }


}
