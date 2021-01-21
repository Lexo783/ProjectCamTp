package Launcher.Event;

import Services.FileSelector;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;

public class EventLauncher {
    FileSelector fileSelector = new FileSelector();

    private File currentFile;

    // event Button upload File
    public EventHandler<ActionEvent> eventLabel(TextField TextField, Label label)
    {
        EventHandler<ActionEvent> event = (ActionEvent e) -> {
            label.setText(TextField.getText());
        };
        return event;
    }

    public EventHandler<ActionEvent> applyFilterOnImageWithoutIA(Stage primaryStage)
    {
        EventHandler<ActionEvent> event = (ActionEvent e) -> {
            File file = this.fileSelector.selectFile(primaryStage);
        };
        return event;
    }
}
