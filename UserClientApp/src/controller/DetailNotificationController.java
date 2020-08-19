package controller;

import com.google.gson.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import model.Location;
import model.Notification;
import util.ConfigUtil;
import util.CurrentUser;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
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


public class DetailNotificationController implements Initializable {
    @FXML
    private GridPane mapa;

    private Pane[][] fields;

    protected Logger logger;
    private Notification notification;


    public DetailNotificationController(Notification notification) {
        this.notification = notification;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initMap();
        initMarker();
    }

    private void initMarker() {
        fields[notification.getLat()][notification.getaLong()].getStyleClass().add("pane-active");
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
