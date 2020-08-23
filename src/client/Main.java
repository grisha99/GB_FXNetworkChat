package client;

import client.interf.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private Controller controller;


    @Override
    public void start(Stage primaryStage) throws Exception{
//        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();

        controller = loader.getController();

        primaryStage.setTitle("Сетевой чат");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();


    }

    @Override
    public void stop() throws Exception {
        controller.sendMsgToServer("/exit");    // смс серверу что мы закрыли приложение (крестик)

    }


    public static void main(String[] args) {

        launch(args);

    }
}
