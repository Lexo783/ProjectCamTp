package Launcher;

import Services.FileSelector;
import Services.ImageRecognition;
import Services.Matrix;
import Services.NeuralNetwork;
import TFUtils.TFUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import java.io.File;
import java.util.Map;

public class Launcher extends Application {

    private final FileSelector fileSelector = new FileSelector();
    private ImageRecognition imageRecognition = new ImageRecognition();
    private Matrix matrix = new Matrix();


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");

        // Create Button
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction((action) -> {
            System.out.println("Hello World!");

            File file = fileSelector.selectFile(primaryStage);
            float[][] copy = imageRecognition.executeModelFromByteArray(imageRecognition.ConvertByteToTensor(file));
            Map<Integer,Float> bestLabels = matrix.getIndexFromMaxMatrix(copy);
            System.out.println(bestLabels);
        });

        // create a textfield
        TextField b = new TextField();
        Label l = new Label("no text");

        // action event
        EventHandler<ActionEvent> event = (ActionEvent e) -> {
            l.setText(b.getText());
        };

        // when enter is pressed
        b.setOnAction(event);

        final ImageView imageView = new ImageView();
        //imageView.setImage(); //Here the image to set
        Platform.runLater(()->{
            imageView.setImage(new Image(this.getClass().getResource("/img/jack.jpg").toString()));
        });

        imageView.setFitHeight(100);
        imageView.setFitWidth(100);

        TilePane root = new TilePane();
        root.getChildren().add(btn);
        // add textfield
        root.getChildren().add(b);
        root.getChildren().add(l);
        root.getChildren().add(imageView);
        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.show();
    }
}