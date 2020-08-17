package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tokenServerClient.TokenServerClient;
import util.FXMLHelper;

import javax.xml.rpc.ServiceException;
import java.net.URL;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.ResourceBundle;


public class SignInController implements Initializable {
    @FXML
    private TextField nameField;
    @FXML
    private TextField surnameField;
    @FXML
    private TextField jmbgField;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    public void signIn(ActionEvent event) {
        new Thread(()-> {
            Button button = ((Button) event.getSource());
            Platform.runLater(()->button.setDisable(true));
            String name = nameField.getText();
            String surname = surnameField.getText();
            String jmbg = jmbgField.getText();

            String token = "";
            try {
                token = new TokenServerClient().signIn(name,surname,jmbg);
            } catch (Exception e) {
                Platform.runLater(()->{
                    new Alert(Alert.AlertType.ERROR, "Error..Try again later..").showAndWait();
                    button.setDisable(false);
                });
                return;
            }
            if ("".equals(token) || token == null) {
                Platform.runLater(()->new Alert(Alert.AlertType.ERROR, "Error..Try again later..").showAndWait());
            } else {
                Platform.runLater(()->new Alert(Alert.AlertType.INFORMATION, "You successfully registered.").showAndWait());
                Stage stage = (Stage) button.getScene().getWindow();

                PasswordController passwordController = new PasswordController(token);
                Scene scene = FXMLHelper.getInstance().loadNewScene("/view/password.fxml","/view/css/sign-in.css", passwordController,400, 300);
                Platform.runLater(()->stage.setScene(scene));
            }
            Platform.runLater(()->button.setDisable(false));
        }).start();
    }
}