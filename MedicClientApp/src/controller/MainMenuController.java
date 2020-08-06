package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import model.UserModel;
import soap.Application_PortType;
import soap.Application_ServiceLocator;

import javax.xml.rpc.ServiceException;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

public class MainMenuController implements Initializable {

    public static final int TCP_PORT = 8085;
    public static final String IP_ADDRESS = "pisio.etfbl.net";
    Socket sock = null;
    BufferedReader in = null;
    PrintWriter out = null;

    //    @FXML
//    private GridPane mapa;
//    @FXML
//    private Label coords;
//    @FXML
//    private Button chooserButton;
    @FXML
    private TextArea chat;
    @FXML
    private TextArea message;
    @FXML
    private Button sendMessageButton;
    @FXML
    private TableView<UserModel> tableView;


    //private Pane[][] fields;
    private final Object _locker = new Object();

    public MainMenuController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //initMap();
        //initPicker();
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
        new Thread(this::initChat).start();
    }

    private void initTable() {
        tableView.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("token"));
        tableView.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("options"));
        String[] tokens = {};
        Application_ServiceLocator asl = new Application_ServiceLocator();
        try {
            Application_PortType apt = asl.getApplication();
            tokens = apt.getActiveTokens();
        } catch (RemoteException | ServiceException e) {
            new Alert(Alert.AlertType.ERROR, "Error..Try again later..").showAndWait();
            e.printStackTrace();
        }
        tableView.setItems(FXCollections.observableArrayList(Arrays.stream(tokens)
                        .map(UserModel::new)
                        .collect(Collectors.toList())));
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
                    String result = String.valueOf(response);
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
//
//    private void initMap() {
//        int columnsNum = mapa.getColumnConstraints().size();
//        int rowsNum = mapa.getRowConstraints().size();
//        fields = new Pane[rowsNum][columnsNum];
//        for (int i = 0; i < rowsNum; i++) {
//            int coords1 = i;
//            for (int j = 0; j < columnsNum; j++) {
//                int coords2 = j;
//                Pane field = new Pane();
//                fields[i][j] = field;
//                mapa.getChildren().add(field);
//                GridPane.setRowIndex(field, i);
//                GridPane.setColumnIndex(field, j);
//                field.toFront();
//                field.setOnMouseClicked((mouseEvent) -> {
//                    synchronized (_locker) {
//                        Arrays.stream(fields).flatMap(Stream::of).forEach(elem -> elem.getStyleClass().remove("pane-active"));
//                        field.getStyleClass().add("pane-active");
//                        Platform.runLater(() -> {
//                            coords.setText("[ " + coords1 + ", " + coords2 + " ]");
//                        });
//                    }
//                });
//            }
//        }
//    }

//    private static String encodeFileToBase64Binary(File file) throws IOException {
//        byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));
//        return new String(encoded, StandardCharsets.US_ASCII);
//    }
}
