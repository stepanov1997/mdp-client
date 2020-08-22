package controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.Location;
import model.Notification;
import util.*;
import view.datetime.DateTimePicker;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.Buffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static javafx.geometry.Pos.CENTER;
import static javafx.geometry.Pos.CENTER_LEFT;
import static view.datetime.DateTimePicker.DEFAULT_FORMAT;

public class MainMenuController implements Initializable {
    private static final Logger LOGGER = Logger.getLogger(MainMenuController.class.getName());


    private static String KEY_STORE_PATH = null;

    static {
        try {
            KEY_STORE_PATH = ConfigUtil.getKeystorePath();
        } catch (IOException ioException) {
            LOGGER.log(Level.FINE, "Config file - Keystore path is missing.", ioException);
            KEY_STORE_PATH = "src/keystore_client.jks";
        }
    }

    private static String KEY_STORE_PASSWORD = null;

    static {
        try {
            KEY_STORE_PASSWORD = ConfigUtil.getKeystorePassword();
        } catch (IOException ioException) {
            LOGGER.log(Level.FINE, "Config file - Keystore password is missing.", ioException);
            KEY_STORE_PASSWORD = "sigurnost";
        }
    }

    private static String TRUST_STORE_PATH = null;

    static {
        try {
            TRUST_STORE_PATH = ConfigUtil.getTruststorePath();
        } catch (IOException ioException) {
            LOGGER.log(Level.FINE, "Config file - Truststore path is missing.", ioException);
            TRUST_STORE_PATH = "src/truststore_client.jks";
        }
    }

    private static String TRUST_STORE_PASSWORD = null;

    static {
        try {
            TRUST_STORE_PASSWORD = ConfigUtil.getTruststorePassword();
        } catch (IOException ioException) {
            LOGGER.log(Level.FINE, "Config file - Truststore password is missing.", ioException);
            TRUST_STORE_PASSWORD = "sigurnost";
        }
    }

    public static String IP_ADDRESS;
    public static int TCP_PORT;
    private static String LOCATION_API;
    private static String NOTIFICATION_API;

    public static SSLSocket sock = null;
    public static BufferedReader in = null;
    public static PrintWriter out = null;
    private boolean isOpened = false;

    private Queue<File> listFiles = new LinkedList<>();

    static {
        try {
            IP_ADDRESS = ConfigUtil.getServerHostname();
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "Config file - server address is missing.", e);
            IP_ADDRESS = "127.0.0.1";
        }

        try {
            TCP_PORT = ConfigUtil.getChatServerPort();
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "Config file - rest port is missing.", e);
            TCP_PORT = 8084;
        }

        try {
            LOCATION_API = "http://" + ConfigUtil.getServerHostname() + ":" + ConfigUtil.getCentralRegisterPort() + "/api/locations";
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "Config file - location api is missing.", e);
            LOCATION_API = "http://127.0.0.1:8081/api/locations";
        }

        try {
            NOTIFICATION_API = "http://" + ConfigUtil.getServerHostname() + ":" + ConfigUtil.getCentralRegisterPort() + "/api/notifications/" + CurrentUser.getToken();
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "Config file - notification api is missing.", e);
            NOTIFICATION_API = "http://127.0.0.1:8081/api/notifications/" + CurrentUser.getToken();
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
    @FXML
    private Button reconnectButton;
    @FXML
    private Text notificationText;
    @FXML
    private Button previousButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button detailButton;

    private Pane[][] fields;
    private final Object _locker = new Object();

    int currentNotification = 0;
    List<Notification> notifications = new ArrayList<>();

    public MainMenuController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initNotifications();
        initPreviousAndNext();
        initMenu();
        initMap();
        initPicker();
        initDateTimePicker();
        initSendLocationButton();
        initSendMessageButton();
        initSendDocumentsButton();
        initReconnectButton();
        new Thread(this::initChat).start();
    }

    private void initPreviousAndNext() {
        previousButton.setOnMouseClicked(mouseEvent -> {
            if (currentNotification - 1 < 0) {
                currentNotification = notifications.size() + currentNotification - 1;
            } else
                currentNotification = (currentNotification - 1) % notifications.size();
            showNotification();
        });
        nextButton.setOnMouseClicked(mouseEvent -> {
            currentNotification = (currentNotification + 1) % notifications.size();
            showNotification();
        });
    }

    private void showNotification() {
        try {
            if (currentNotification > notifications.size() - 1) {
                Platform.runLater(() -> {
                    notificationText.setText("No notifications.");
                    detailButton.setVisible(false);
                    previousButton.setVisible(false);
                    nextButton.setVisible(false);
                });
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.WARNING, "Thread.sleep()", e);
                    e.printStackTrace();
                }
            } else {
                Platform.runLater(() -> {
                    detailButton.setVisible(true);
                    previousButton.setVisible(true);
                    nextButton.setVisible(true);
                });

                Notification latest = notifications.get(currentNotification);
                Platform.runLater(() -> notificationText.setText((currentNotification + 1) + ". Infection: " + latest.getInfection()));
                detailButton.setOnAction(event -> {
                    if ("medic".equals(latest.getTypeOfNotification()))
                        Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION,
                                "Medic marked you as " + latest.getInfection().toLowerCase() + " after chatting with you.").showAndWait());
                    else {
                        Platform.runLater(() -> {
                            DetailNotificationController detailNotificationController = new DetailNotificationController(latest);
                            Stage stage = new Stage();
                            Scene scene = FXMLHelper.getInstance().loadNewScene("/view/map-recorded-position.fxml", "/view/css/main-menu.css", detailNotificationController, 900, 600);
                            stage.setScene(scene);
                            stage.initModality(Modality.APPLICATION_MODAL);
                            stage.showAndWait();
                        });
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Token server is offline. Unsuccessfully login.", e);
        }
    }

    private void initNotifications() {
        new Thread(() -> {
            while (true) {
                try {
                    Client client = ClientBuilder.newClient();
                    WebTarget target = client.target(NOTIFICATION_API);
                    Invocation.Builder request = target.request(MediaType.APPLICATION_JSON);
                    Response response = request.get();
                    String entity = response.readEntity(String.class);
                    JsonElement jsonElement = JsonParser.parseString(entity);
                    if (jsonElement.isJsonNull()) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException interruptedException) {
                            LOGGER.log(Level.WARNING, "Thread.sleep().", interruptedException);
                        }
                    }
                    JsonObject jsonObject = null;
                    if (jsonElement.isJsonArray()) {
                        if (jsonElement.getAsJsonArray().size() != 0) {
                            jsonObject = jsonElement.getAsJsonArray().get(0).getAsJsonObject();
                        } else {
                            throw new Exception();
                        }
                    } else if (jsonElement.isJsonObject()) {
                        jsonObject = jsonElement.getAsJsonObject();
                    } else throw new Exception();
                    String typeOfNotification = jsonObject.get("typeOfNotification").getAsString();
                    Notification notification = null;
                    if ("medic".equals(typeOfNotification)) {
                        notification = new Notification(
                                jsonObject.get("token").getAsString(),
                                jsonObject.get("infection").getAsString(),
                                jsonObject.get("typeOfNotification").getAsString()
                        );
                    } else {
                        notification = new Notification(
                                jsonObject.get("token").getAsString(),
                                jsonObject.get("potential_contact_from").getAsString(),
                                jsonObject.get("potential_contact_to").getAsString(),
                                jsonObject.get("interval").getAsInt(),
                                jsonObject.get("lat").getAsInt(),
                                jsonObject.get("long").getAsInt(),
                                jsonObject.get("distance").getAsInt(),
                                jsonObject.get("infection").getAsString(),
                                jsonObject.get("typeOfNotification").getAsString()
                        );
                    }
                    notifications.add(notification);
                    currentNotification = notifications.size() - 1;
                    showNotification();
                    SerializationUtil.serializeNotification(notification);
                } catch (Exception e) {
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException interruptedException) {
                    LOGGER.log(Level.WARNING, "Thread.sleep()", interruptedException);
                }
            }
        }).start();
    }

    private void initReconnectButton() {
        reconnectButton.setVisible(false);
        reconnectButton.setOnAction(event -> {
            message.setVisible(true);
            sendMessageButton.setVisible(true);
            reconnectButton.setVisible(false);
            new Thread(this::initChat).start();
        });
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
                new Thread(() -> {
                    Platform.runLater(() -> sendDocumentsButton.setDisable(true));
                    while (!listFiles.isEmpty()) {
                        File file = listFiles.poll();
                        RmiClient.sendFileToServer(file);
                        Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "Successfully sended " + file.getName()).showAndWait());
                    }
                    Platform.runLater(() -> {
                        new Alert(Alert.AlertType.INFORMATION, "Successfully sending files to file server.").showAndWait();
                        sendDocumentsButton.setDisable(false);
                    });
                }).start();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Cannot send files to file server.", e);
                new Alert(Alert.AlertType.ERROR, "Unsuccessfully sending files to file server.").showAndWait();
                return;
            }
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
            Stage stage = (Stage) sendDocumentsButton.getScene().getWindow();
            Scene scene = FXMLHelper.getInstance().loadNewScene("/view/passwordChanging.fxml", "/view/css/sign-in.css", new PasswordChangingController(stage), 400, 300);
            Stage newStage = new Stage();
            newStage.setScene(scene);
            newStage.showAndWait();
        });
        closeApplication.setOnAction(actionEvent -> System.exit(0));
    }

    private void initSendMessageButton() {
        sendMessageButton.setOnAction(event -> {
            synchronized (_locker) {
                Platform.runLater(() -> sendMessageButton.setDisable(true));

                var context = new Object() {
                    String text = message.getText();
                };
                if (isOpened) {
                    if (out != null) {
                        context.text = context.text.replaceAll("\\n", "").replaceAll("\\r", "");
                        out.println(context.text);
                        System.out.println("Data to write: \"" + context.text + "\"");
                        Platform.runLater(() -> chat.setText("[me]: " + context.text + System.lineSeparator() + System.lineSeparator() +chat.getText() ));
                        Platform.runLater(() -> message.setText(""));
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            LOGGER.log(Level.WARNING, "Thread.sleep()", e);
                        }
                    } else {
                        new Alert(Alert.AlertType.WARNING, "Connection is not estabilished yet.").showAndWait();
                    }
                } else {
                    new Alert(Alert.AlertType.WARNING, "Connection is closed.").showAndWait();
                }
                Platform.runLater(() -> sendMessageButton.setDisable(false));
            }
        });
    }

    private void initChat() {
        while (true) {
            boolean first = true;
            while (true) {
                try {
                    System.setProperty("javax.net.ssl.keyStore", KEY_STORE_PATH);
                    System.setProperty("javax.net.ssl.keyStorePassword", KEY_STORE_PASSWORD);

                    System.setProperty("javax.net.ssl.trustStore", TRUST_STORE_PATH);
                    System.setProperty("javax.net.ssl.trustStorePassword", TRUST_STORE_PASSWORD);

                    InetAddress addr = InetAddress.getByName(IP_ADDRESS);
                    SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
                    sock = (SSLSocket) sf.createSocket(addr, TCP_PORT);

                    in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())), true);

                    isOpened = true;
                    out.println("Token: " + CurrentUser.getToken());
                    break;
                } catch (Exception exception) {
                    if (first) {
                        LOGGER.log(Level.WARNING, "Chat server is offline.", exception);
                        Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "Chat server is offline.").showAndWait());
                    }
                    first = false;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.WARNING, "Thread.sleep()", e);
                        Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "Chat server is offline.").showAndWait());
                    }
                }
            }
            // prikaz poruka
            while (true) {
                char[] response = new char[1024];
                try {
                    int read = in.read(response);
                    String result = String.valueOf(response).trim();
                    if (read == 0 || result.isBlank())
                        continue;
                    if (result.startsWith("END")) {
                        Platform.runLater(() -> {
                            chat.setText("---MEDIC ENDED SESSION---" + System.lineSeparator() + chat.getText());
                            sendMessageButton.setVisible(false);
                            message.setVisible(false);
                            reconnectButton.setVisible(true);
                        });
                        isOpened = false;
                        break;
                    }
                    System.out.println("Read data: \"" + String.valueOf(response) + "\"");
                    Platform.runLater(() -> chat.setText(result + System.lineSeparator() + chat.getText()));
                    Platform.runLater(() -> chat.setScrollTop(chat.getHeight()));
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.WARNING, "Thread.sleep()", e);
                        e.printStackTrace();
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Closed session.", ex);
                    isOpened = false;
                    break;
                }
            }
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
                            coords.setUserData(new Pair<>(coords1, coords2));
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

        dateTimePicker.dateTimeValueProperty().addListener(((observable, value, newValue) -> dateTimePicker.getEditor().setText(newValue.format(DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm:ss")))));

        return dateTimePicker;
    }
}
