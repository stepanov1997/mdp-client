package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import util.CurrentUser;
import util.FXMLHelper;
import util.SHA1;
import util.StageUtil;

import java.net.URL;
import java.util.ResourceBundle;

public class PasswordChangingController implements Initializable {

    @FXML
    private PasswordField oldPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField newPasswordAgainField;
    @FXML
    private Button changePasswordButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        changePasswordButton.setOnAction(event-> {
            if(oldPasswordField.getText().isBlank() ||
                newPasswordField.getText().isBlank() ||
                newPasswordAgainField.getText().isBlank()){
                new Alert(Alert.AlertType.ERROR, "Enter old password and two times new password.").showAndWait();
                return;
            }
            String hash = SHA1.encryptPassword(oldPasswordField.getText());
            if(!hash.equals(CurrentUser.getPasswordHash())){
                new Alert(Alert.AlertType.ERROR, "Old password is incorrect.").showAndWait();
                return;
            }
            if(!newPasswordField.getText().matches("^.{8,}$")){
                new Alert(Alert.AlertType.ERROR, "Password have to be longer than 7 characters.").showAndWait();
                return;
            }
            if(!newPasswordField.getText().equals(newPasswordAgainField.getText())){
                new Alert(Alert.AlertType.ERROR, "New passwords do not match.").showAndWait();
                return;
            }
            CurrentUser.setPassword(newPasswordField.getText());
            new Alert(Alert.AlertType.CONFIRMATION, "You successfully changed your password. Please login again.").showAndWait();
            LocalLoginController passwordController = new LocalLoginController();
            Scene scene = FXMLHelper.getInstance().loadNewScene("/view/local-login.fxml","/view/css/sign-in.css", passwordController,300, 200);
            Stage stage = (Stage)oldPasswordField.getScene().getWindow();
            stage.setScene(scene);
            StageUtil.centerStage(stage);
        });
    }

}
