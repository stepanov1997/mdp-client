package controller;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import util.*;
import view.datetime.DateTimePicker;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

import static javafx.geometry.Pos.CENTER;
import static javafx.geometry.Pos.CENTER_LEFT;
import static view.datetime.DateTimePicker.DEFAULT_FORMAT;

public class MainMenuController implements Initializable {

    public static String IP_ADDRESS;
    public static int TCP_PORT;
    private static String LOCATION_API;

    static {

    }

    public static Socket sock = null;
    public static BufferedReader in = null;
    public static PrintWriter out = null;

    private Queue<File> listFiles = new LinkedList<>();

    static {
        try {
            IP_ADDRESS = ConfigUtil.getServerHostname();
        } catch (IOException e) {
            IP_ADDRESS = "127.0.0.1";
        }

        try {
            TCP_PORT = ConfigUtil.getChatServerPort();
        } catch (IOException e) {
            TCP_PORT = 8084;
        }

        try {
            LOCATION_API = "http://"+ConfigUtil.getServerHostname()+":"+ConfigUtil.getCentralRegisterPort()+"/api/locations";
        } catch (IOException e) {
            LOCATION_API = "http://127.0.0.1:8081/api/locations";
        }
    }

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
    private Button sendDocumentsButton;
    @FXML
    private MenuItem mapOfRecordedPositions;
    @FXML
    private MenuItem applicationUsage;
    @FXML
    private MenuItem unsubscribeFromRegister;
    @FXML
    private MenuItem closeApplication;
    @FXML
    private MenuItem changePasswordItem;
    @FXML
    private HBox fromDateTime;
    @FXML
    private HBox toDateTime;

    private Pane[][] fields;
    private final Object _locker = new Object();

    public MainMenuController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initMenu();
        initMap();
        initPicker();
        initDateTimePicker();
        initSendLocationButton();
        initSendMessageButton();
        initSendDocumentsButton();
        new Thread(this::initChat).start();
    }

    private void initDateTimePicker() {
        DateTimePicker fromDateTimePicker = createDateTimePicker();
        DateTimePicker toDateTimePicker = createDateTimePicker();
        fromDateTime.getChildren().add(fromDateTimePicker);
        fromDateTime.setAlignment(CENTER);
        toDateTime.getChildren().add(toDateTimePicker);
        toDateTime.setAlignment(CENTER);
    }

    private void initSendDocumentsButton() {
        sendDocumentsButton.setOnAction(event -> {
            if (listFiles == null || listFiles.size() == 0) {
                new Alert(Alert.AlertType.WARNING, "Please, choose some files (max 5).").showAndWait();
                return;
            }
            try {
                while (!listFiles.isEmpty()) {
                    File file = listFiles.poll();
                    new Thread(() -> RmiClient.sendFileToServer(file)).start();
                }
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Unsuccessfully sending files to file server.").showAndWait();
                return;
            }
            new Alert(Alert.AlertType.INFORMATION, "Successfully sending files to file server.").showAndWait();
        });
    }

    private void initSendLocationButton() {
        sendLocationButton.setOnAction(event -> {
            Pair<Integer, Integer> pair = (Pair<Integer, Integer>) coords.getUserData();
            LocalDateTime fromDateTimeValue = ((DateTimePicker) fromDateTime.getChildren().get(0)).getDateTimeValue();
            LocalDateTime toDateTimeValue = ((DateTimePicker) toDateTime.getChildren().get(0)).getDateTimeValue();
            if (pair == null || fromDateTimeValue.isAfter(LocalDateTime.now()) || toDateTimeValue.isBefore(fromDateTimeValue)) {
                String message = "";
                if (fromDateTimeValue.isAfter(LocalDateTime.now())) {
                    message += "\nTake datetime before this moment";
                }
                if (toDateTimeValue.isBefore(fromDateTimeValue)) {
                    message += "\nTake end datetime before start datetime";
                }
                if (pair == null) {
                    message += "\nSelect location on map.";
                }
                new Alert(Alert.AlertType.ERROR, message).showAndWait();
                return;
            }
            int _lat = pair.getKey();
            int _long = pair.getValue();
            Client klijent = ClientBuilder.newClient();
            WebTarget webTarget = klijent.target(LOCATION_API);

            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("token", CurrentUser.getToken());
            jsonObject.addProperty("lat", _lat);
            jsonObject.addProperty("long", _long);
            jsonObject.addProperty("from", DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss").format(fromDateTimeValue));
            jsonObject.addProperty("to", DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss").format(toDateTimeValue));

            Response response = invocationBuilder.post(Entity.entity(jsonObject.toString(), MediaType.APPLICATION_JSON));
            if (response.getStatus() != 200) {
                new Alert(Alert.AlertType.ERROR, "Location is unsuccessfully sent..").showAndWait();
                return;
            }
            new Alert(Alert.AlertType.INFORMATION, "Location is successfully sent..").showAndWait();
        });
    }

    private void initMenu() {
        mapOfRecordedPositions.setOnAction(actionEvent -> {
            Stage stage = new Stage();
            Scene scene = FXMLHelper.getInstance().loadNewScene("/view/map-recorded-position.fxml", "/view/css/main-menu.css", new MapRecordedPosition(), 900, 600);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        });
        applicationUsage.setOnAction(actionEvent -> {
            Stage stage = new Stage();
            Scene scene = FXMLHelper.getInstance().loadNewScene("/view/application-usage.fxml", "/view/css/main-menu.css", new ApplicationUsageController(), 900, 600);
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        });
        unsubscribeFromRegister.setOnAction(actionEvent -> {
            CurrentUser.setToken("");
            CurrentUser.setPassword("");
            Scene scene = FXMLHelper.getInstance().loadNewScene("/view/sign-in.fxml", "/view/css/sign-in.css", new SignInController(), 400, 300);
            Stage stage = (Stage) sendMessageButton.getScene().getWindow();
            stage.setScene(scene);
            StageUtil.centerStage(stage);
        });
        changePasswordItem.setOnAction(actionEvent -> {
            Scene scene = FXMLHelper.getInstance().loadNewScene("/view/passwordChanging.fxml", "/view/css/sign-in.css", new PasswordChangingController(), 400, 300);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
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
            if (selectedFiles.size() > 5) {
                new Alert(Alert.AlertType.WARNING, "Please, choose max 5 files.").showAndWait();
                return;
            }
            listFiles.addAll(selectedFiles);
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

    private DateTimePicker createDateTimePicker() {
        DateTimePicker dateTimePicker = new DateTimePicker();

        dateTimePicker.minutesSelectorProperty().set(true);

        dateTimePicker.setFormat("dd.MM.yyyy. HH:mm:ss");
        //Label valueLabel = new Label();
        //CustomBinding.bind(dateTimePicker.dateTimeValueProperty(), valueLabel.textProperty(),
        //        dt -> dt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss")));

        CustomBinding.bindBidirectional(dateTimePicker.dateTimeValueProperty(), dateTimePicker.dateTimeValueProperty(),
                dt -> dt, dt -> dt);

        dateTimePicker.dateTimeValueProperty().addListener(((observable, value, newValue) -> {
            dateTimePicker.getEditor().setText(newValue.format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss")));
        }));

        return dateTimePicker;
    }
}
