package controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import util.CurrentUser;
import util.FXMLHelper;
import util.RmiClient;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.stream.Stream;

public class MainMenuController implements Initializable {

    public static final int TCP_PORT = 8084;
    //public static final String IP_ADDRESS = "pisio.etfbl.net";
    public static final String IP_ADDRESS = "127.0.0.1";
    private static final String LOCATION_API = "http://localhost:8081/api/locations";
    public static Socket sock = null;
    public static BufferedReader in = null;
    public static PrintWriter out = null;

    @FXML
    private GridPane mapa;
    @FXML
    private Label coords;
    @FXML
    private Button chooserButton;
    @FXML
    private TextArea chat;
    @FXML
    private TextArea message;
    @FXML
    private Button sendMessageButton;
    @FXML
    private Button sendLocationButton;
    @FXML
    private MenuItem mapOfRecordedPositions;
    @FXML
    private MenuItem applicationUsage;
    @FXML
    private MenuItem unsubscribeFromRegister;
    @FXML
    private MenuItem closeApplication;

    private Pane[][] fields;
    private final Object _locker = new Object();

    public MainMenuController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initMenu();
        initMap();
        initPicker();
        initSendLocationButton();
        initSendMessageButton();
        new Thread(this::initChat).start();
    }

    private void initSendLocationButton() {
        sendLocationButton.setOnAction(event -> {
            Pair<Integer, Integer> pair = (Pair<Integer, Integer>)coords.getUserData();
            int _lat = pair.getKey();
            int _long = pair.getValue();
            Client klijent = ClientBuilder.newClient();
            WebTarget webTarget = klijent.target(LOCATION_API);

            Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("token", CurrentUser.getToken());
            jsonObject.addProperty("lat", _lat);
            jsonObject.addProperty("long", _long);

            Response response = invocationBuilder.post(Entity.entity(jsonObject.toString(), MediaType.APPLICATION_JSON));
            if(response.getStatus()!=200){
                new Alert(Alert.AlertType.ERROR, "Location is unsuccessfully sent..").showAndWait();
                return;
            }
            new Alert(Alert.AlertType.INFORMATION, "Location is successfully sent..").showAndWait();
        });
    }

    private void initMenu() {
        mapOfRecordedPositions.setOnAction(actionEvent -> {
            Stage stage = new Stage();
            Scene scene = FXMLHelper.getInstance().loadNewScene("/view/map-recorded-position.fxml", "/view/css/main-menu.css",new MapRecordedPosition());
            stage.setScene(scene);
            stage.showAndWait();
        });
        applicationUsage.setOnAction(actionEvent -> {
            Stage stage = new Stage();
            Scene scene = FXMLHelper.getInstance().loadNewScene("/view/application-usage.fxml", "/view/css/main-menu.css",new ApplicationUsageController());
            stage.setScene(scene);
            stage.showAndWait();
        });
        unsubscribeFromRegister.setOnAction(actionEvent -> {
            CurrentUser.setToken("");
            CurrentUser.setPassword("");
            Scene scene = FXMLHelper.getInstance().loadNewScene("/view/sign-in.fxml", "/view/css/sign-in.css",new SignInController());
            Stage stage = (Stage) sendMessageButton.getScene().getWindow();
            stage.setScene(scene);
        });
        closeApplication.setOnAction(actionEvent -> {
            System.exit(0);
        });
    }

    private void initSendMessageButton() {
        sendMessageButton.setOnAction(event -> {
            synchronized (_locker) {
                Platform.runLater(() -> sendMessageButton.setDisable(true));

                var context = new Object() {
                    String text = message.getText();
                };

                if (out != null) {
                    context.text = context.text.replaceAll("\\n", "").replaceAll("\\r", "");
                    out.println(context.text);
                    System.out.println("Data to write: \"" + context.text + "\"");
                    Platform.runLater(() -> chat.setText(chat.getText() + "[me]: " + context.text + System.lineSeparator() + System.lineSeparator()));
                    Platform.runLater(() -> message.setText(""));
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    new Alert(Alert.AlertType.WARNING, "Connection is not estabilished yet.").showAndWait();
                }
                Platform.runLater(() -> sendMessageButton.setDisable(false));
            }
        });
    }

    private void initChat() {
        try {
            InetAddress addr = InetAddress.getByName(IP_ADDRESS);
            sock = new Socket(addr, TCP_PORT);

            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())), true);

            var ref = new Object() {
                final boolean flag = true;
            };

            out.println("Token: " + CurrentUser.getToken());

            // prikaz poruka
            while (ref.flag) {
                char[] response = new char[1024];
                try {
                    in.read(response);
                    String result = String.valueOf(response).trim();
                    if (result.isBlank())
                        continue;
                    System.out.println("Read data: \"" + String.valueOf(response) + "\"");
                    Platform.runLater(() -> chat.setText(chat.getText() + result + System.lineSeparator()));
                    Platform.runLater(() -> chat.setScrollTop(chat.getHeight()));
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void initPicker() {
        chooserButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(chooserButton.getScene().getWindow());
            if (selectedFiles.size() >= 5) {
                new Alert(Alert.AlertType.WARNING, "Please, choose max 5 files.").showAndWait();
                return;
            }
            selectedFiles.forEach(RmiClient::sendFileToServer);
        });
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
                            coords.setUserData(new Pair<Integer, Integer>(coords1, coords2));
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
