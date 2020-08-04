package main;

import controller.MainMenuController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import util.FXMLHelper;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ResourceBundle;

public class MainFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        System.setProperty("java.security.policy", "client.policy");
        String name = null;
        Scene scene = FXMLHelper.getInstance().loadNewScene("/view/main-menu.fxml", "/view/css/main-menu.css", new MainMenuController());
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image("/view/icons/app-icon.png"));
        primaryStage.setTitle("Hospital Application");
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
