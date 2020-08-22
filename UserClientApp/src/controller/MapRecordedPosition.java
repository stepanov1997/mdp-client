package controller;

import com.google.gson.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import model.Location;
import util.ConfigUtil;
import util.CurrentUser;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import java.util.stream.Collectors;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class MapRecordedPosition implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(MapRecordedPosition.class.getName());

    private String LOCATION_API;

    {
        try {
            LOCATION_API = "http://" + ConfigUtil.getServerHostname() + ":" + ConfigUtil.getCentralRegisterPort() + "/api/locations/" + CurrentUser.getToken();
        } catch (IOException ioException) {
            LOGGER.log(Level.WARNING, "Config file - location api is missing", ioException);
            LOCATION_API = "http://127.0.0.1:8081/api/locations/" + CurrentUser.getToken();
        }
    }

    @FXML
    private GridPane mapa;

    private Pane[][] fields;

    private final Object _locker = new Object();

    protected Logger logger;
    protected Client klijent;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initMap();
        initMarkers();
    }

    private void initMarkers() {
        new Thread(() -> {
            try {
                Arrays.stream(fields).flatMap(Stream::of).forEach(elem -> elem.getStyleClass().remove("pane-active"));

                klijent = ClientBuilder.newClient();
                WebTarget webTarget = klijent.target(LOCATION_API);

                Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
                Response response = invocationBuilder.get();

                List<Location> locationList = new ArrayList<Location>();
                if (Response.Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
                    String jsonStr = response.readEntity(String.class);
                    JsonArray jsonArray = (JsonArray) JsonParser.parseString(jsonStr);
                    for (JsonElement jsonElement : jsonArray) {
                        if (jsonElement instanceof JsonObject) {
                            JsonObject jsonObject = (JsonObject) jsonElement;
                            locationList.add(new Location(
                                    jsonObject.get("_id").getAsString(),
                                    jsonObject.get("token").getAsString(),
                                    jsonObject.get("long").getAsInt(),
                                    jsonObject.get("lat").getAsInt(),
                                    jsonObject.get("from").getAsString(),
                                    jsonObject.get("to").getAsString(),
                                    jsonObject.get("dateTime").getAsString()
                            ));
                        }
                    }
                }
                Platform.runLater(() -> {

                    for (Location location : locationList) {
                        Pane field = fields[location.get_lat()][location.get_long()];
                        if (field.getUserData() != null) {
                            field.setUserData("Token" + location.getToken() + System.lineSeparator() + System.lineSeparator() +
                                    "Potential contact from: " + location.getFrom() + System.lineSeparator() +
                                    "Potential contact to: " + location.getTo() + System.lineSeparator() +
                                    "Datetime created: " + location.getDateTime() + System.lineSeparator() +
                                    System.lineSeparator() + System.lineSeparator() + field.getUserData()
                            );
                        } else {
                            field.setUserData(
                                    "Token" + location.getToken() + System.lineSeparator() + System.lineSeparator() +
                                            "Potential contact from: " + location.getFrom() + System.lineSeparator() +
                                            "Potential contact to: " + location.getTo() + System.lineSeparator() +
                                            "Datetime created: " + location.getDateTime() + System.lineSeparator()
                            );
                            field.getStyleClass().add("pane-active");
                        }
                        field.setOnMouseClicked(mouseEvent -> {
                            Platform.runLater(() -> {
                                new Alert(Alert.AlertType.INFORMATION,
                                        (String) field.getUserData()
                                ).showAndWait();
                            });
                        });
                    }
                });
            }
            catch (Exception e){
                LOGGER.log(Level.WARNING, "initMarkers", e.getMessage());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException interruptedException) {
                    LOGGER.log(Level.WARNING, "Thread.sleep()", interruptedException.getMessage());
                }
            }
        }).start();
    }

    private void initMap() {
        int columnsNum = mapa.getColumnConstraints().size();
        int rowsNum = mapa.getRowConstraints().size();
        fields = new Pane[rowsNum][columnsNum];
        for (int i = 0; i < rowsNum; i++) {
            for (int j = 0; j < columnsNum; j++) {
                Pane field = new Pane();
                fields[i][j] = field;
                mapa.getChildren().add(field);
                GridPane.setRowIndex(field, i);
                GridPane.setColumnIndex(field, j);
                field.toFront();
                field.setOnMouseClicked((mouseEvent) -> {
                });
            }
        }
    }
}
