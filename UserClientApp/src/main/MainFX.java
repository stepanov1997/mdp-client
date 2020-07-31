package main;

import controller.SignInController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = null;
        FXMLLoader loader =  new FXMLLoader(getClass().getResource("/view/sign-in.fxml"));
        SignInController controller = new SignInController();
        loader.setController(controller);
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/view/css/sign-in.css");
        primaryStage.setScene(scene);
        primaryStage.getIcons().add(new Image("/view/icons/app-icon.png"));
        primaryStage.setTitle("Hospital Application");
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
