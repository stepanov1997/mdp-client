package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import util.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class LocalLoginController implements Initializable {
    @FXML
    private Text welcome;
    @FXML
    private TextField passwordField;

    public LocalLoginController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    public void login(ActionEvent actionEvent){
        String passwordHash = CurrentUser.getPasswordHash();
        if(passwordHash==null || !passwordHash.equals(SHA1.encryptPassword(passwordField.getText())))
        {
            new Alert(Alert.AlertType.ERROR, "Password is not OK.").showAndWait();
            return;
        }
        ActivityUtil.loginTime = LocalDateTime.now();
        Stage stage = (Stage) welcome.getScene().getWindow();
        stage.setResizable(true);
        stage.setOnCloseRequest(event -> {
            ActivityUtil.logoutTime = LocalDateTime.now();
            ActivityUtil.addActivity();
            System.exit(0);
        });
        MainMenuController mainMenuController = new MainMenuController();
        Scene scene = FXMLHelper.getInstance().loadNewScene("/view/main-menu.fxml","/view/css/main-menu.css", mainMenuController, 900, 600);
        stage.setScene(scene);
        StageUtil.centerStage(stage);
    }
}
