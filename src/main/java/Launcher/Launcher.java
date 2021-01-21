package Launcher;

import Services.FileSelector;
import Services.ImageRecognition;
import Services.Matrix;
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
    private Map<String,Float> allBestLabels;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");

        // create a textfield
        TextField txtFieldDef = new TextField();
        Label l = new Label("no text");
        final ImageView imageView = new ImageView(); //place for image

        // Create Button
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction((action) -> {
            System.out.println("Hello World!");

            File file = fileSelector.selectFile(primaryStage);
            float[][] copy = imageRecognition.executeModelFromByteArray(imageRecognition.ConvertByteToTensor(file));
            String[] allLabels = imageRecognition.getLabels();
            this.allBestLabels = matrix.getLabelsFromMaxMatrix(copy, allLabels);
            String bestLabel = imageRecognition.getImagePotentialLabel(this.allBestLabels);
            System.out.println(this.allBestLabels);
            System.out.println(bestLabel);
            l.setText(bestLabel);

            //just try if img set works, it's fine. but seems image have to be in resources dir.
            imageView.setImage(new Image(this.getClass().getResource("/inception5h/tensorPics/suncokret.jpg").toString()));


            //region check our definition with labels found
            if (allBestLabels.containsKey(txtFieldDef.getText())){
                System.out.println("IA agree");
            }
            else{
                System.out.println("IA disagree");

            }
            //endregion
        });



        // action event
        EventHandler<ActionEvent> event = (ActionEvent e) -> {
            l.setText(txtFieldDef.getText());
        };

        // when enter is pressed
        txtFieldDef.setOnAction(event);

        //imageView.setImage(); //Here the image to set
        Platform.runLater(()->{
            imageView.setImage(new Image(this.getClass().getResource("/img/jack.jpg").toString()));
        });

        imageView.setFitHeight(100);
        imageView.setFitWidth(100);

        TilePane root = new TilePane();
        root.getChildren().add(btn);
        // add textfield
        root.getChildren().add(txtFieldDef);
        root.getChildren().add(l);
        root.getChildren().add(imageView);
        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.show();
    }
}