package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import server.IFileServer;
import util.ConfigUtil;
import util.RmiClient;
import util.SizeUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Stream;

public class DocumentationController implements Initializable {
    private String token;

    @FXML
    private CheckBox check1;
    @FXML
    private CheckBox check2;
    @FXML
    private CheckBox check3;
    @FXML
    private CheckBox check4;
    @FXML
    private CheckBox check5;

    @FXML
    private Label name1;
    @FXML
    private Label name2;
    @FXML
    private Label name3;
    @FXML
    private Label name4;
    @FXML
    private Label name5;

    @FXML
    private Label size1;
    @FXML
    private Label size2;
    @FXML
    private Label size3;
    @FXML
    private Label size4;
    @FXML
    private Label size5;

    @FXML
    private Button open1;
    @FXML
    private Button open2;
    @FXML
    private Button open3;
    @FXML
    private Button open4;
    @FXML
    private Button open5;

    @FXML
    private Label tokenLabel;
    @FXML
    private Button downloadButton;
    @FXML
    private Button openFolderButton;

    CheckBox[] checkBoxes;
    Label[] names;
    Label[] sizes;
    Button[] opens;

    public DocumentationController() {

    }

    public DocumentationController(String token) {
        this.token = token;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        checkBoxes = new CheckBox[]{check1, check2, check3, check4, check5};
        names = new Label[]{name1, name2, name3, name4, name5};
        sizes = new Label[]{size1, size2, size3, size4, size5};
        opens = new Button[]{open1, open2, open3, open4, open5};
        initLabels();
        initButtons();
        initFileWatcher();
    }

    private void initFileWatcher() {
        new Thread(() -> {
            new File("files" + File.separator + token).mkdirs();
//            Path path = Paths.get("files" + File.separator + token);
//            try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
//                final WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
//                while (true) {
//                    final WatchKey wk = watchService.take();
//                    for (WatchEvent<?> event : wk.pollEvents()) {
//                        //we only register "ENTRY_MODIFY" so the context is always a Path.
//                        final Path changed = (Path) event.context();
//                        System.out.println(changed);
//                        for (int i = 0; i < 5; i++) {
//
//                            if (changed.toString().contains(names[i].getText())) {
//                                int finalI1 = i;
//                                Platform.runLater(() -> opens[finalI1].setVisible(true));
//                                int finalI = i;
//                            }
//                        }
//                    }
//                    // reset the key
//                    boolean valid = wk.reset();
//                }
//            } catch (IOException | InterruptedException ioException) {
//                ioException.printStackTrace();
//            }
        }).start();

    }

    private void initLabels() {
        new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                checkBoxes[i].setSelected(false);
                checkBoxes[i].setVisible(false);
                names[i].setText("");
                sizes[i].setText("");
                sizes[i].setUserData(null);
                opens[i].setVisible(false);
            }
            String nm = "FileServer";
            String hostname = "";
            int port = 0;
            try {
                hostname = ConfigUtil.getServerHostname();
                port = ConfigUtil.getFileServerPort();
            } catch (IOException ioException) {
                hostname = "127.0.0.1";
                port = 1099;
            }
            try {
                IFileServer srv = (IFileServer) Naming.lookup("rmi://" + hostname + ":" + port + "/" + nm);
                HashMap<String, Long> filePathsForToken = srv.getFilePathsForToken(token);
                Queue<Button> openQueue = new LinkedList<>(Arrays.asList(opens));
                Queue<CheckBox> checkBoxQueue = new LinkedList<>(Arrays.asList(checkBoxes));
                Queue<Label> nameQueue = new LinkedList<>(Arrays.asList(names));
                Queue<Label> sizeQueue = new LinkedList<>(Arrays.asList(sizes));
                filePathsForToken.forEach((name, size) -> {
                    Button openButton = openQueue.remove();
                    CheckBox checkBox = checkBoxQueue.remove();
                    Label nameLabel = nameQueue.remove();
                    Label sizeLabel = sizeQueue.remove();

                    Platform.runLater(() -> {
                        checkBox.setVisible(true);
                        nameLabel.setText(name);
                        sizeLabel.setUserData(size);
                        sizeLabel.setText(SizeUtil.toNumInUnits(size));
                    });

                    if (Stream.of(new File("files" + File.separator + token + File.separator).listFiles()).anyMatch(elem -> elem.getName().equals(name))) {
                        Platform.runLater(() -> {
                            checkBox.setSelected(true);
                            openButton.setVisible(true);
                            openButton.setOnAction(elem -> openFile(name));
                            checkBox.setDisable(true);
                        });
                    }
                });
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void initButtons() {
        tokenLabel.setText(tokenLabel.getText() + token);

        downloadButton.setOnAction(event -> {
            new Thread(() -> {
                for (int i = 0; i < checkBoxes.length; i++) {
                    CheckBox checkBox = checkBoxes[i];
                    if (checkBox.isSelected() && !checkBox.isDisabled()) {
                        Label label = sizes[i];
                        Long number = (Long) label.getUserData();
                        String name = names[i].getText();
                        RmiClient.downloadFilesFromServer(token, name, number);
                        opens[i].setVisible(true);
                    }
                }
            }).start();
        });
        openFolderButton.setOnAction(event -> {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().open(new File("files/" + token).getAbsoluteFile());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }).start();
        });
        for(int i=0; i<5; i++){
            int finalI = i;
            opens[i].setOnAction(event -> {
                new Thread(()-> {
                    File file = new File("files" + File.separator + token + File.separator+ names[finalI].getText());
                    Long length = file.length();
                    Long userData = (Long) sizes[finalI].getUserData();
                    if(Math.abs(length-userData)>0){
                        Platform.runLater(()->new Alert(Alert.AlertType.INFORMATION, "File is not transfered yet... Wait...").showAndWait());
                    }
                    else openFile(names[finalI].getText());
                }).start();
            });
        }
    }

    private void openFile(String name) {
        new Thread(() -> {
            if (!Desktop.isDesktopSupported()) {
                new Alert(Alert.AlertType.INFORMATION, "Desktop is not supported.").showAndWait();
                return;
            }
            Desktop desktop = Desktop.getDesktop();
            File file = new File("files" + File.separator + token + File.separator + name);
            if (file.exists()) {
                try {
                    desktop.open(file);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } else {
                new Alert(Alert.AlertType.INFORMATION, "File doesn't exist").showAndWait();
            }
        }).start();
    }
}
