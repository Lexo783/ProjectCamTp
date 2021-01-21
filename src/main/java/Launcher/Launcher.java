package Launcher;

import Launcher.Event.EventLauncher;
import Services.FileSelector;
import Services.ImageRecognition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import java.io.File;

// on prends une image on la passe au net IA il compare au label du text field et si c'est egeaux il sauvegarde l'image
public class Launcher extends Application {

    private EventLauncher event = new EventLauncher();
    private final FileSelector fileSelector = new FileSelector();
    private ImageRecognition imageRecognition = new ImageRecognition();


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");

        // Create Button
        Button btn = new Button("Selectionner une image");

        // Set Action
        btn.setOnAction(event1 -> {
            File file = fileSelector.selectFile(primaryStage);
            imageRecognition.executeModelFromByteArray(imageRecognition.ConvertByteToTensor(file));
        });

        // create a textfield
        TextField textField = new TextField();
        Label label = new Label("no text");

        // when enter is pressed
        textField.setOnAction(event.eventLabel(textField,label));

        final ImageView imageView = new ImageView();
        //imageView.setImage(); //Here the image to set
        Platform.runLater(()->{
            imageView.setImage(new Image(this.getClass().getResource("/img/jack.jpg").toString()));
        });

        imageView.setFitHeight(100);
        imageView.setFitWidth(100);

        TilePane root = new TilePane();
        root.getChildren().add(btn);
        root.getChildren().add(textField);
        root.getChildren().add(label);
        root.getChildren().add(imageView);

        settingLauncher(primaryStage,root);
    }

    public void settingLauncher(Stage primaryStage,TilePane root){
        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.show();
    }
}