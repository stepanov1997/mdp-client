package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.chainsaw.Main;
import server.IFileServer;
import util.CurrentUser;
import util.ImageCell;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class MainMenuController implements Initializable {

    @FXML
    private GridPane mapa;
    @FXML
    private Label coords;
    @FXML
    private Button chooserButton;

    private Pane[][] fields;
    private final Object _locker = new Object();

    public MainMenuController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initMap();
        initPicker();
    }

    private void initPicker() {
        chooserButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(chooserButton.getScene().getWindow());
            if (selectedFiles.size() >= 5) {
                new Alert(Alert.AlertType.WARNING, "Please, choose max 5 files.").showAndWait();
                return;
            }
            selectedFiles.forEach(MainMenuController::sendFileToServer);
        });
    }

    private static void sendFileToServer(File file) {
        try {
            String token = CurrentUser.getToken();
            String nm = "FileServer";
            IFileServer srv = (IFileServer) Naming.lookup("rmi://pisio.etfbl.net:1099/" + nm);
            int fileSize = (int) file.length();
            int sended = 0;
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
                while (sended < fileSize) {
                    int toSend = 10_000_000;
                    if (fileSize - sended < toSend)
                        toSend = (int) (fileSize - sended);
                    byte[] buffer = new byte[toSend];
                    randomAccessFile.seek(sended);
                    randomAccessFile.read(buffer, 0, toSend);
                    String pathOnServer = srv.uploadFileOnServer(token, file.getName(), buffer, sended);
                    if(pathOnServer==null){
                        throw new RemoteException();
                    }
                    sended += toSend;
                }
            }
        } catch (Exception e) {
            System.err.println("FileServer exception:");
            e.printStackTrace();
        }
    }

    private void initMap() {
        int columnsNum = mapa.getColumnConstraints().size();
        int rowsNum = mapa.getRowConstraints().size();
        fields = new Pane[rowsNum][columnsNum];
        for (int i = 0; i < rowsNum; i++) {
            int coords1 = i;
            for (int j = 0; j < columnsNum; j++) {
                int coords2 = j;
                Pane field = new Pane();
                fields[i][j] = field;
                mapa.getChildren().add(field);
                GridPane.setRowIndex(field, i);
                GridPane.setColumnIndex(field, j);
                field.toFront();
                field.setOnMouseClicked((mouseEvent) -> {
                    synchronized (_locker) {
                        Arrays.stream(fields).flatMap(Stream::of).forEach(elem -> elem.getStyleClass().remove("pane-active"));
                        field.getStyleClass().add("pane-active");
                        Platform.runLater(() -> {
                            coords.setText("[ " + coords1 + ", " + coords2 + " ]");
                        });
                    }
                });
            }
        }
    }

//    private static String encodeFileToBase64Binary(File file) throws IOException {
//        byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));
//        return new String(encoded, StandardCharsets.US_ASCII);
//    }
}
