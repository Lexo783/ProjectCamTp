package Launcher;

import Services.FileSelector;
import Services.ImageRecognition;
import Services.Matrix;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
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
import javafx.embed.swing.SwingFXUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import javafx.scene.image.*;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.global.opencv_imgproc.*;
import org.bytedeco.opencv.opencv_core.IplImage;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Launcher extends Application {

    private final FileSelector fileSelector = new FileSelector();
    private ImageRecognition imageRecognition = new ImageRecognition();
    private Matrix matrix = new Matrix();
    private Map<String,Float> allBestLabels;
    private Java2DFrameConverter converter = new Java2DFrameConverter();
    private OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);

    public static void main(String[] args) {
        launch(args);
    }


    private WritableImage frameToImage(Frame frame) {
        BufferedImage bufferedImage = converter.getBufferedImage(frame);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }
    private void launchCam(ImageView camView){

        try {
            grabber.start();
        }catch (Exception e){}
//        TimerTask task = new TimerTask() {
//            @Override
//            public void run() {
//
//                getOneCamFrame(camView);
//            }
//        };
//        Timer timer = new Timer();
//        timer.schedule( task, 0, 1000);


        Runnable helloRunnable = new Runnable() {
            public void run() {
                getOneCamFrame(camView);
            }
        };

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(helloRunnable, 0, 33, TimeUnit.MILLISECONDS);




    }

    private void getOneCamFrame(ImageView camView){
        try {
            Frame frame = grabber.grab(); // Frame frame = grabber.grabFrame();
            if (frame != null) {
                WritableImage img = frameToImage(frame);
                camView.setImage(img);
            }
        } catch (Exception e) {}

    }

    /*
    public Unit updateView(Frame frame){
        int w = frame.imageWidth();
        int h = frame.imageHeight();

        val mat = javaCVConv.convert(frame);
        cvtColor(mat, javaCVMat, COLOR_BGR2BGRA);

        val pb = PixelBuffer(w, h, buffer, formatByte);
        val wi = WritableImage(pb);
        videoView.setImage(wi);
    }

     */
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


        //region cam
        ImageView camView  = new  ImageView();
        grabber.setImageWidth(300);
        grabber.setImageHeight(300);


/* //good one
        try {
            grabber.start();
            while (true) {
//                try {
//                    BufferedImage img = converter.convert(grabber.grab());
//                    WritableImage writable = frameToImage();
//                    img = checkBayer(img);
//                    if (img != null) {
//                        this.updateCurrentImage(img);
//                    }
//                } catch (Exception e) {
//                }
                Frame frame = grabber.grab(); // Frame frame = grabber.grabFrame();

                if (frame != null) {
                    WritableImage img = frameToImage(frame);
                    camView.setImage(img);
                    //cvSaveImage((i++) + "-pic.jpg", img); // save image
                    //camView.setImage(convertToFxImage(frame.getBufferedImage())); //show image on ImageView
                }
            }
        } catch (Exception e) {}
*/

//        process();

        Runnable exe = new Runnable() {
            @Override
            public void run() {
                getOneCamFrame(camView);
            }
        };
        Executors.newSingleThreadExecutor().execute(exe);
/*
        Executors.newSingleThreadExecutor().execute{
            while (true) {
                try {
                    Frame frame = grabber.grabFrame();
                    //camView.image = frameToImage(frame);
                }
                catch (Exception e){
                    System.out.println(e);
                }
            }
        }
*/
        //Scene scene = Scene(VBox(imageView), 800.0, 800.0);
        //primaryStage.scene = scene;
        //primaryStage.show();

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

            /*
            TimerTask task = new TimerTask() {

                @Override
                public void run() {
                    launchCam(camView);
                }
            };
            Timer timer = new Timer();
            timer.schedule( task, 0, 1000);*/
            launchCam(camView);
        });
        //endregion

        //region button select source from pictures
        Button btnSourcePics = new Button();
        btnSourcePics.setText("Select picture as source");
        btnSourcePics.setOnAction((action) -> {
            //add all extension for cam (or video)
            fileSelector.setExtFilter("Images", "*.jpeg", "*.jpg");
            try{
                grabber.stop();
            }catch (Exception e){
                System.out.println(e);
            }
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
        final int rootWidth = 1200;
        final int rootheight = 700;
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

        root.setRight(camView);
        primaryStage.show();
        //endregion
    }
}