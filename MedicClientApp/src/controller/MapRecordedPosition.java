package controller;

import com.google.gson.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import model.Location;
import util.ConfigUtil;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class MapRecordedPosition implements Initializable {
    private String LOCATION_API;

    {
        try {
            LOCATION_API = "http://"+ ConfigUtil.getServerHostname() +":"+ConfigUtil.getCentralRegisterPort()+"/api/locations/";
        } catch (IOException ioException) {
            LOCATION_API = "http://127.0.0.1:8081/api/locations/";
        }
    }

    @FXML
    private GridPane mapa;

    private Pane[][] fields;

    private final Object _locker = new Object();

    protected Logger logger;
    protected Client klijent;

    public MapRecordedPosition(String token) {
        LOCATION_API += token;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initMap();
        initMarkers();
    }

    private void initMarkers() {
        Arrays.stream(fields).flatMap(Stream::of).forEach(elem -> elem.getStyleClass().remove("pane-active"));

        klijent = ClientBuilder.newClient();
        WebTarget webTarget = klijent.target(LOCATION_API);

        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
        Response response = invocationBuilder.get();

        List<Location> locationList = new ArrayList<Location>();
        if(Response.Status.Family.SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
            String jsonStr = response.readEntity(String.class);
            Gson gson = new Gson();
            JsonArray jsonArray = (JsonArray) JsonParser.parseString(jsonStr);
            for (JsonElement jsonElement : jsonArray) {
                if(jsonElement instanceof JsonObject){
                    JsonObject jsonObject = (JsonObject)jsonElement;
                    locationList.add(new Location(
                            jsonObject.get("_id").getAsString(),
                            jsonObject.get("token").getAsString(),
                            jsonObject.get("long").getAsInt(),
                            jsonObject.get("lat").getAsInt()
                    ));
                }
            }
        }
        for (Location location: locationList) {
            fields[location.get_lat()][location.get_long()].getStyleClass().add("pane-active");
        }
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
