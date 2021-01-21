package Launcher;

import Services.FileSelector;
import Services.ImageRecognition;
import Services.Matrix;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
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

        //region create textfield & it's label for image description
        TextField txtFieldDef = new TextField();
        Label defFieldLabel = new Label("image description");
        //endregion

        //region create image view & it's label
        final ImageView imageView = new ImageView(); //place for image
        Label imageLabel = new Label("no text");
        //endregion

        // region Create Button select file
        Button btn = new Button();
        btn.setText("Select a file");
        btn.setOnAction((action) -> {
            File file = fileSelector.selectFile(primaryStage);
            float[][] copy = imageRecognition.executeModelFromByteArray(imageRecognition.ConvertByteToTensor(file));
            this.allBestLabels = matrix.getLabelsFromMaxMatrix(copy, imageRecognition.getLabels());
            String bestLabel = imageRecognition.getImagePotentialLabel(this.allBestLabels);
            System.out.println(this.allBestLabels);
            System.out.println(bestLabel); // #Story 1 - display label in console
            imageLabel.setText(bestLabel); // #Story 2 - display found label for image

            //just try if img set works, it's fine. but seems image have to be in resources dir.
            String[] pathArr = file.getAbsolutePath().split("/resources");
            imageView.setImage(new Image(this.getClass().getResource(pathArr[pathArr.length-1]).toString()));

            //region check our definition with labels found
            if (allBestLabels.containsKey(txtFieldDef.getText())){
                System.out.println("IA agree");
            }
            else{
                System.out.println("IA disagree");

            }
            //endregion
        });
        //endregion



        //region select sources buttons
        //region button select source from cam
        Button btnSourceCam = new Button();
        btnSourceCam.setText("Select camera as source");
        btnSourceCam.setOnAction((action) -> {
            //add all extension for cam (or video)
            fileSelector.setExtFilter("video", "*.mp4", "*.cam");
        });
        //endregion

        //region button select source from pictures
        Button btnSourcePics = new Button();
        btnSourcePics.setText("Select picture as source");
        btnSourcePics.setOnAction((action) -> {
            //add all extension for cam (or video)
            fileSelector.setExtFilter("Images", "*.jpeg", "*.jpg");
        });
        //endregion
        //endregion

        //region action event - set image label for now
        EventHandler<ActionEvent> event = (ActionEvent e) -> {
            imageLabel.setText(txtFieldDef.getText());
        };
        //endregion

        // when enter is pressed
        txtFieldDef.setOnAction(event);

        //imageView.setImage(); //Here the image to set
        Platform.runLater(()->{
            imageView.setImage(new Image(this.getClass().getResource("/img/jack.jpg").toString()));
        });



        //region manage display -- background could change, just used to debug for now
        //region initialize window
        BorderPane root = new BorderPane();
        final int rootWidth = 600;
        final int rootheight = 600;
        root.setStyle("-fx-background-color: #CCCCCC;");

        primaryStage.setScene(new Scene(root, rootWidth, rootheight));
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        //endregion

        //region top - select source panel
        FlowPane sourceSelectPan = new FlowPane();
        root.setStyle("-fx-background-color: #CDCD5C;");

        sourceSelectPan.getChildren().add(btnSourceCam);
        sourceSelectPan.getChildren().add(btnSourcePics);
        //endregion


        //region left - selectPic button + definition panel
        FlowPane leftPan = new FlowPane(Orientation.VERTICAL);
        leftPan.getChildren().addAll(btn, txtFieldDef, defFieldLabel);
        leftPan.setStyle("-fx-background-color: #CD5C5C;");
        //endregion

        //region center - image panel
        FlowPane picsSelectionPan = new FlowPane(Orientation.VERTICAL);
        picsSelectionPan.setPrefWidth(rootWidth);
        picsSelectionPan.setStyle("-fx-background-color: #CD5CCD;");
        picsSelectionPan.getChildren().addAll( imageView, imageLabel);
        //endregion


        root.setCenter(picsSelectionPan);
        root.setTop(sourceSelectPan);
        root.setLeft(leftPan);

        primaryStage.show();
        //endregion
    }
}