package model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import controller.MapRecordedPosition;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import soap.Application_PortType;
import soap.Application_ServiceLocator;
import util.ConfigUtil;
import util.FXMLHelper;
import util.TableColumnPlus;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Predicate;

public class UserModel {

    private String USER_TYPE_API;

    {
        try {
            USER_TYPE_API = "http://" + ConfigUtil.getServerHostname() + ":" + ConfigUtil.getCentralRegisterPort() + "/api/usertypes/";
        } catch (IOException ioException) {
            USER_TYPE_API = "http://127.0.0.1:8081/api/usertypes/";
        }
    }

    private final SimpleStringProperty token;
    private final SimpleObjectProperty<GridPane> options;

    public UserModel(String token, Runnable refreshCallback) {
        this.token = new SimpleStringProperty(token);

        GridPane gridPane = new GridPane();
        for (int i = 0; i < 3; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(33.3);
            gridPane.getColumnConstraints().add(columnConstraints);
        }
        Button mapButton = new Button();
        mapButton.setText("Open map");
        mapButton.setOnAction(event -> {
            new Thread(() -> {
                Platform.runLater(() -> {
                    Scene scene = FXMLHelper.getInstance().loadNewScene("/view/map-recorded-position.fxml", "/view/css/main-menu.css", new MapRecordedPosition(token));
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.showAndWait();
                });
            }).start();
        });
        gridPane.getChildren().add(mapButton);
        GridPane.setColumnIndex(mapButton, 0);

        Button blockButton = new Button();
        blockButton.setText("Block user");
        blockButton.setOnAction(event -> {
            new Thread(() -> {
                Application_ServiceLocator asl = new Application_ServiceLocator();
                try {
                    Application_PortType apt = asl.getApplication();
                    if (apt.deactivateToken(token)) {
                        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Successfully blocked.").showAndWait());
                        Platform.runLater(refreshCallback);
                    } else {
                        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Error, unsuccessfully blocked.").showAndWait());
                    }
                } catch (RemoteException | ServiceException e) {
                    new Alert(Alert.AlertType.ERROR, "Error..Try again later..").showAndWait();
                    e.printStackTrace();
                }
            }).start();
        });
        gridPane.getChildren().add(blockButton);
        GridPane.setColumnIndex(blockButton, 1);

        ComboBox<String> markButton = new ComboBox<>();
        markButton.setItems(FXCollections.observableList(Arrays.asList(
                "Not infected",
                "Potential infected",
                "Infected"
        )));
        markButton.setPromptText("Mark user");
        markButton.valueProperty().addListener((obs, oldItem, newItem) -> {
            new Thread(() -> {
                Client client = ClientBuilder.newClient();
                WebTarget target = client.target(USER_TYPE_API);
                Invocation.Builder request = target.request(MediaType.APPLICATION_JSON);
                Response response = null;
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("token", token);
                jsonObject.addProperty("userType", markButton.getSelectionModel().getSelectedItem());
                response = request.post(Entity.entity(jsonObject.toString(), MediaType.APPLICATION_JSON));
                if (response.getStatusInfo().getFamily().compareTo(Response.Status.Family.SUCCESSFUL) != 0) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Unsuccessfully marking person.").showAndWait());
                    return;
                }
                Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "Successfully marking person").showAndWait());
            }).start();
        });
        gridPane.getChildren().add(markButton);
        GridPane.setColumnIndex(markButton, 2);

        this.options = new SimpleObjectProperty<>(gridPane);
    }

    public String getToken() {
        return token.get();
    }

    public SimpleStringProperty tokenProperty() {
        return token;
    }

    public void setToken(String token) {
        this.token.set(token);
    }

    public GridPane getOptions() {
        return options.get();
    }

    public SimpleObjectProperty<GridPane> optionsProperty() {
        return options;
    }

    public void setOptions(GridPane options) {
        this.options.set(options);
    }
}