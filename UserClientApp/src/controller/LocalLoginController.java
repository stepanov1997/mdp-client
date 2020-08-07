package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import util.CurrentUser;
import util.FXMLHelper;
import util.SHA1;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LocalLoginController implements Initializable {
    private final String name;
    @FXML
    private Text welcome;

    @FXML
    private TextField passwordField;


    public LocalLoginController(String name) {
        this.name = name;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(()->welcome.setText(welcome.getText()+ name));
    }

    @FXML
    public void login(ActionEvent actionEvent){
        String passwordHash = CurrentUser.getPasswordHash();
        if(passwordHash==null || !passwordHash.equals(SHA1.encryptPassword(passwordField.getText())))
        {
            new Alert(Alert.AlertType.ERROR, "Password is not OK.");
            return;
        }
        Stage stage = (Stage) welcome.getScene().getWindow();
        stage.setResizable(true);
        MainMenuController mainMenuController = new MainMenuController();
        Scene scene = FXMLHelper.getInstance().loadNewScene("/view/main-menu.fxml","/view/css/main-menu.css", mainMenuController);
        stage.setScene(scene);
    }
}
