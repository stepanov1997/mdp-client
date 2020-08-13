package view.datetime;

import view.datetime.DateTimePicker;
import util.CustomBinding;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

import static view.datetime.DateTimePicker.DEFAULT_FORMAT;
import static javafx.geometry.Pos.CENTER;
import static javafx.geometry.Pos.CENTER_LEFT;

public class DateTimePickerDemo extends Application {

    public void start(Stage stage) {

        stage.setTitle("DateTimePicker - Demo");
        stage.setResizable(false);
        //stage.setScene(new Scene(hBox, 250, 200));
        stage.centerOnScreen();
        stage.show();
        toFront(stage);
    }

    private void toFront(Stage stage) {
        stage.setAlwaysOnTop(true);
        stage.toFront();
        stage.setAlwaysOnTop(false);
    }

    private RadioButton buildRadioButton(ToggleGroup group, DateTimePicker.TimeSelector timeSelector) {
        RadioButton radioButton = new RadioButton("TimeSelector: " + timeSelector.name());
        radioButton.setToggleGroup(group);
        radioButton.setUserData(timeSelector);
        return radioButton;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
