package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.ActivityModel;
import tokenServerClient.TokenServerClient;
import util.ActivityUtil;
import util.CurrentUser;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ApplicationUsageController implements Initializable {
    @FXML
    private TableView<ActivityModel> tableView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initTable();
    }

    public void initTable() {
        tableView.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("loginTime"));
        tableView.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("logoutTime"));
        tableView.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("loginSpentTime"));
        List<String[]> rows = null;
        try {
            rows = ActivityUtil.readActivities();
        } catch (Exception ioException) {
            rows = new ArrayList<>();
        }
        List<ActivityModel> activities = rows
                .stream()
                .filter(elem -> CurrentUser.getToken().equals(elem[0]))
                .map(elem -> new ActivityModel(elem[0], elem[1], elem[2], elem[3]))
                .collect(Collectors.toList());

        tableView.setItems(FXCollections.observableArrayList(activities));
    }
}
