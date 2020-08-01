package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class LocalLoginController implements Initializable {
    private final String name;
    @FXML
    private Text welcome;


    public LocalLoginController(String name) {
        this.name = name;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Platform.runLater(()->welcome.setText(welcome.getText()+ name));
    }

    @FXML
    public void login(ActionEvent actionEvent){

    }
}
