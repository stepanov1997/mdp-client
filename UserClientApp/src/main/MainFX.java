package main;

import controller.LocalLoginController;
import controller.SignInController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import soap.Application_PortType;
import soap.Application_ServiceLocator;
import util.CurrentUser;
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
        String token = CurrentUser.getToken();
        String passwordHash = CurrentUser.getPasswordHash();
        if(token==null || passwordHash==null || token.isBlank() || passwordHash.isBlank())
        {
            Scene scene = FXMLHelper.getInstance().loadNewScene("/view/sign-in.fxml", "/view/css/sign-in.css", new SignInController());
            primaryStage.setScene(scene);
            primaryStage.getIcons().add(new Image("/view/icons/app-icon.png"));
            primaryStage.setTitle("Hospital Application");
            primaryStage.setResizable(false);
            primaryStage.show();
            return;
        }
        String name = null;
        Scene scene = FXMLHelper.getInstance().loadNewScene("/view/local-login.fxml", "/view/css/sign-in.css", new LocalLoginController(name));
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image("/view/icons/app-icon.png"));
        primaryStage.setTitle("Hospital Application");
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
