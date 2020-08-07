package model;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import soap.Application_PortType;
import soap.Application_ServiceLocator;
import util.TableColumnPlus;

import javax.xml.rpc.ServiceException;
import java.rmi.RemoteException;
import java.util.function.Function;
import java.util.function.Predicate;

public class UserModel {

    private final SimpleStringProperty token;
    private final SimpleObjectProperty<GridPane> options;

    public UserModel(String token, Runnable refreshCallback) {
        this.token = new SimpleStringProperty(token);

        GridPane gridPane = new GridPane();
        for(int i=0; i<3; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(33.3);
            gridPane.getColumnConstraints().add(columnConstraints);
        }
        Button mapButton = new Button();
        mapButton.setText("Open map");
        mapButton.setOnAction(event-> {
            new Thread(()-> {
                Platform.runLater(()->new Alert(Alert.AlertType.INFORMATION, "Show map").showAndWait());
            }).start();
        });
        gridPane.getChildren().add(mapButton);
        GridPane.setColumnIndex(mapButton, 0);

        Button blockButton = new Button();
        blockButton.setText("Block user");
        blockButton.setOnAction(event-> {
            new Thread(()-> {
                Application_ServiceLocator asl = new Application_ServiceLocator();
                try {
                    Application_PortType apt = asl.getApplication();
                    if(apt.deactivateToken(token)){
                        Platform.runLater(()->new Alert(Alert.AlertType.ERROR, "Successfully blocked.").showAndWait());
                        Platform.runLater(refreshCallback);
                    }
                    else {
                        Platform.runLater(()->new Alert(Alert.AlertType.ERROR, "Error, unsuccessfully blocked.").showAndWait());
                    }
                } catch (RemoteException | ServiceException e) {
                    new Alert(Alert.AlertType.ERROR, "Error..Try again later..").showAndWait();
                    e.printStackTrace();
                }
            }).start();
        });
        gridPane.getChildren().add(blockButton);
        GridPane.setColumnIndex(blockButton, 1);

        Button markButton = new Button();
        markButton.setText("Mark user");
        markButton.setOnAction(event-> {
            new Thread(()-> {
                Platform.runLater(()->new Alert(Alert.AlertType.INFORMATION, "Mark user").showAndWait());
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