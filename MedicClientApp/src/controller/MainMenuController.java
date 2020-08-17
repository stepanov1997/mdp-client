package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.UserModel;
import tokenServerClient.TokenServerClient;
import util.ConfigUtil;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.rpc.ServiceException;
import java.io.*;
import java.net.*;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainMenuController implements Initializable {

    private static final String KEY_STORE_PATH = "src/keystore_client.jks";
    private static final String KEY_STORE_PASSWORD = "sigurnost";
    private static final String TRUST_STORE_PATH = "src/truststore_client.jks";
    private static final String TRUST_STORE_PASSWORD = "sigurnost";
    public static int TCP_PORT;
    public static int UDP_PORT;
    public static String IP_ADDRESS;
    private static String MULTICAST_ADDRESS;

    static {
        try {
        IP_ADDRESS = ConfigUtil.getServerHostname();
        } catch (IOException e) {
            IP_ADDRESS = "127.0.0.1";
        }
        try {
            TCP_PORT = ConfigUtil.getChatServerPort();
        } catch (IOException e) {
            TCP_PORT = 8085;
        }
        try {
            UDP_PORT = ConfigUtil.getMulticastChatServerPort();
        } catch (IOException e) {
            TCP_PORT = 8086;
        }
        try {
            MULTICAST_ADDRESS = ConfigUtil.getMulticastChatServerAddress();
        } catch (IOException e) {
            MULTICAST_ADDRESS = "224.168.100.2";
        }
    }

    private final Object _locker = new Object();
    SSLSocket socketForPatient = null;
    MulticastSocket socketForMedics = null;
    BufferedReader inForPatient = null;
    PrintWriter outForPatient = null;
    BufferedReader inForMedics = null;
    PrintWriter outForMedics = null;

    String msg = null;
    @FXML
    private TextArea chat;
    @FXML
    private TextArea message;
    @FXML
    private Button sendMessageButton;
    @FXML
    private TableView<UserModel> tableView;
    @FXML
    private Button findButton;
    @FXML
    private TextField findTextField;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabPatient;
    @FXML
    private Tab tabMedics;
    @FXML
    private TextArea patientChat;
    @FXML
    private TextArea medicsChat;

    public MainMenuController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initTable();
        sendMessageButton.setOnAction(event -> {
            Platform.runLater(() -> sendMessageButton.setDisable(true));

            var context = new Object() {
                String text = message.getText();
            };

            if (tabPatient.isSelected()) {
                if (outForPatient != null) {
                    context.text = context.text.replaceAll("\\r\\n", "");
                    outForPatient.println(context.text);
                    System.out.println("Data to write: \"" + context.text + "\"");
                    Platform.runLater(() -> patientChat.setText(patientChat.getText() + "[me]: " + context.text + System.lineSeparator() + System.lineSeparator()));
                    Platform.runLater(() -> message.setText(""));
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    new Alert(Alert.AlertType.WARNING, "Connection is not estabilished yet.").showAndWait();
                }
            } else if (tabMedics.isSelected()) {
                if (socketForMedics != null) {
                    context.text = context.text.replaceAll("\\r\\n", "");
                    byte[] msg = message.getText().getBytes();
                    DatagramPacket packet = null;
                    try {
                        packet = new DatagramPacket(msg, msg.length,
                                InetAddress.getByName(MULTICAST_ADDRESS), UDP_PORT);
                        socketForMedics.send(packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Data to write: \"" + context.text + "\"");
                    Platform.runLater(() -> medicsChat.setText(medicsChat.getText() + "[me]: " + context.text + System.lineSeparator() + System.lineSeparator()));
                    Platform.runLater(() -> message.setText(""));
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    new Alert(Alert.AlertType.WARNING, "Connection is not estabilished yet.").showAndWait();
                }
            }
            Platform.runLater(() -> sendMessageButton.setDisable(false));
        });
        findButton.setOnAction(event -> new Thread(() -> importDataToTable(findTextField.getText())).start());
        initChat();
    }

    private void initTable() {
        tableView.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("token"));
        tableView.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("options"));
        new Thread(() -> importDataToTable("")).start();
    }

    private void importDataToTable(String filter) {
        String[] tokens = {};
        try {
            tokens = new TokenServerClient().getActiveTokens();
        } catch (RemoteException | ServiceException exception) {
            Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Error..Try again later..").showAndWait());
            exception.printStackTrace();
        }
        String[] finalTokens = tokens;
        Platform.runLater(() ->
                tableView.setItems(FXCollections.observableArrayList(Arrays.stream(finalTokens)
                        .filter(elem -> elem.toLowerCase().contains(filter.toLowerCase()))
                        .map(elem -> new UserModel(elem, this::initTable))
                        .collect(Collectors.toList())))
        );
    }

    private void initChat() {
        new Thread(() -> {
            try {
                InetAddress addr = InetAddress.getByName(MULTICAST_ADDRESS);
                socketForMedics = new MulticastSocket(UDP_PORT);
                socketForMedics.joinGroup(addr);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            while (true) {
                byte[] response = new byte[1024];
                DatagramPacket msgPacket = new DatagramPacket(response, response.length);

                try {
                    socketForMedics.receive(msgPacket);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                String result = new String(response).trim();
                if (result.isBlank())
                    continue;
                Platform.runLater(() -> medicsChat.setText(medicsChat.getText() + result + System.lineSeparator()));
                Platform.runLater(() -> medicsChat.setScrollTop(medicsChat.getHeight()));
            }
        }).start();

        new Thread(() -> {
            try {
                System.setProperty("javax.net.ssl.keyStore", KEY_STORE_PATH);
                System.setProperty("javax.net.ssl.keyStorePassword", KEY_STORE_PASSWORD);

                System.setProperty("javax.net.ssl.trustStore", TRUST_STORE_PATH);
                System.setProperty("javax.net.ssl.trustStorePassword", TRUST_STORE_PASSWORD);

                InetAddress addr = InetAddress.getByName(IP_ADDRESS);
                SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
                socketForPatient = (SSLSocket) sf.createSocket(addr, TCP_PORT);

                inForPatient = new BufferedReader(new InputStreamReader(socketForPatient.getInputStream()));
                outForPatient = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socketForPatient.getOutputStream())), true);
            } catch (IOException ioException) {
                ioException.printStackTrace();
                return;
            }
            while (true) {
                char[] response = new char[1024];
                try {
                    inForPatient.read(response);
                    String result = String.valueOf(response).trim();
                    if (result.isBlank())
                        continue;
                    Platform.runLater(() -> patientChat.setText(patientChat.getText() + result + System.lineSeparator()));
                    Platform.runLater(() -> patientChat.setScrollTop(patientChat.getHeight()));
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
        }).start();
    }
}
