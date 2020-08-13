package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.UserModel;
import soap.Application_PortType;
import soap.Application_ServiceLocator;
import tokenServerClient.TokenServerClient;
import util.ConfigUtil;

import javax.xml.rpc.ServiceException;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

public class MainMenuController implements Initializable {

    public static int TCP_PORT;
    public static String IP_ADDRESS;

    static {
        try {
            IP_ADDRESS = ConfigUtil.getServerHostname();
        } catch (IOException e) {
            IP_ADDRESS = "127.0.0.1";
            e.printStackTrace();
        }
        try {
            TCP_PORT = ConfigUtil.getChatServerPort();
        } catch (IOException e) {
            TCP_PORT = 8085;
        }
    }

    Socket sock = null;
    BufferedReader in = null;
    PrintWriter out = null;

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


    private final Object _locker = new Object();

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
        });
        findButton.setOnAction(event -> new Thread(() -> importDataToTable(findTextField.getText())).start());
        new Thread(this::initChat).start();
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
            new Alert(Alert.AlertType.ERROR, "Error..Try again later..").showAndWait();
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
        try {
            InetAddress addr = InetAddress.getByName(IP_ADDRESS);
            sock = new Socket(addr, TCP_PORT);

            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sock.getOutputStream())), true);

            var ref = new Object() {
                boolean flag = true;
            };

            while (ref.flag) {
                char[] response = new char[1024];
                try {
                    in.read(response);
                    String result = String.valueOf(response).trim();
                    if (result.isBlank())
                        continue;
                    System.out.println("Read data: \"" + String.valueOf(response) + "\"");
                    String finalResult = result;
                    Platform.runLater(() -> chat.setText(chat.getText() + finalResult + System.lineSeparator()));
                    Platform.runLater(() -> chat.setScrollTop(chat.getHeight()));
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
