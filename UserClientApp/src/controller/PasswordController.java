package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import util.CurrentUser;
import util.FXMLHelper;
import util.StageUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class PasswordController implements Initializable {
    private String token = null;

    @FXML
    private TextField passwordField;

    @FXML
    private TextField passwordAgainField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public PasswordController() {

    }

    public PasswordController(String token) {
        this.token = token;
    }

    @FXML
    public void register(ActionEvent event) {
        if (passwordField.getText().isBlank() || passwordAgainField.getText().isBlank()) {
            new Alert(Alert.AlertType.WARNING, "Fill in both fields").showAndWait();
        } else if (!passwordField.getText().equals(passwordAgainField.getText())) {
            new Alert(Alert.AlertType.WARNING, "Passwords do not match.").showAndWait();
        } else {
            CurrentUser.setToken(token);
            CurrentUser.setPassword(passwordField.getText());
            new Alert(Alert.AlertType.INFORMATION, "Successfully adding password.").showAndWait();
            Stage stage = (Stage) passwordField.getScene().getWindow();

            Scene scene = FXMLHelper.getInstance().loadNewScene("/view/local-login.fxml","/view/css/sign-in.css", new LocalLoginController(),400, 300);
            stage.setScene(scene);
            StageUtil.centerStage(stage);
        }
    }
}
