package Launcher.Event;

import Services.FileSelector;
import Services.ImageRecognition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;

public class EventLauncher {

    private File currentFile;

    // event Button upload File
    public EventHandler<ActionEvent> eventLabel(TextField TextField, Label label)
    {
        EventHandler<ActionEvent> event = (ActionEvent e) -> {
            label.setText(TextField.getText());
        };
        return event;
    }
}
