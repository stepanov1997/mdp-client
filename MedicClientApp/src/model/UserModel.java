package model;

import com.google.gson.*;
import controller.DocumentationController;
import controller.MapRecordedPosition;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import soap.Application_PortType;
import soap.Application_ServiceLocator;
import util.ConfigUtil;
import util.FXMLHelper;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;

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

    public UserModel(String token) {
        this.token = new SimpleStringProperty(token);

        GridPane gridPane = new GridPane();
        for (int i = 0; i < 4; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(25);
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
        Thread thread = new Thread(() -> {
            try {
                Client cli = ClientBuilder.newClient();
                WebTarget webTarget = cli.target(USER_TYPE_API + "/" + token);
                Invocation.Builder request1 = webTarget.request(MediaType.APPLICATION_JSON);
                Response response1 = request1.get();
                if (response1.getStatusInfo().getFamily().compareTo(Response.Status.Family.SUCCESSFUL) != 0) {
                    Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Unsuccessfully init.").showAndWait());
                    return;
                }
                JsonArray jsonArray = (JsonArray) JsonParser.parseString(response1.readEntity(String.class));
                if (jsonArray.size() != 0) {
                    JsonObject jsonObject1 = (JsonObject) jsonArray.get(0);
                    String userType = jsonObject1.get("userType").getAsString();
                    markButton.setValue(userType);
                } else {
                    markButton.setValue("Not infective");
                }
            } catch (Exception e) {
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

        Button documentationButton = new Button("Docs");
        documentationButton.setOnAction(elem -> {
            Stage stage = new Stage();
            Scene scene = FXMLHelper.getInstance().loadNewScene("/view/documentation.fxml", "/view/css/main-menu.css", new DocumentationController(token));
            stage.setScene(scene);
            stage.showAndWait();
        });
        gridPane.getChildren().add(documentationButton);
        GridPane.setColumnIndex(documentationButton, 3);

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