package Launcher;

import Services.FileSelector;
import Services.Filter;
import Services.ImageRecognition;
import Services.Matrix;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.Lighting;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import java.io.*;
import javafx.scene.image.*;
import javafx.stage.WindowEvent;
import org.bytedeco.javacv.*;
import javax.imageio.ImageIO;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Launcher extends Application {

    //region class attributes
    private final FileSelector fileSelector = new FileSelector();
    private DirectoryChooser directoryChooser;
    private Java2DFrameConverter converter = new Java2DFrameConverter();
    private OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
    private BorderPane root;
    Filter filter = new Filter();
    private final FileSelector fileSelectorFilter = new FileSelector();
    private ChoiceBox choiceBox;
    private String currentFilter;
    private TextField txtFieldDef;
    private String bestLabel;
    private ImageRecognition imageRecognition = new ImageRecognition();
    private Matrix matrix = new Matrix();
    private Map<String,Float> allBestLabels;
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private Boolean isExecutorLaunched =false;
    private BufferedImage currentImg;
    private String currentDirStoragePath;
    //endregion

    public static void main(String[] args) {
        launch(args);
    }

    //region storage dir attribute functions
    /**
     * Use to select storage Directory.
     * We set initial directory from the last selected one, if there is one.
     * @return the directory File
     */
    public File selectStorageDir() {
        if(this.getCurrentDirStoragePath()!=null){
            directoryChooser.setInitialDirectory(new File(this.currentDirStoragePath));
        }
        return directoryChooser.showDialog(new Stage());
    }


    /**
     * Get current directory where we want to store image.
     * @return current directory path as string.
     */
    public String getCurrentDirStoragePath() {
        return currentDirStoragePath;
    }

    /**
     * Set current directory where we want to store image.
     * @param currentDirStoragePath => the new storage directory path
     */
    public void setCurrentDirStoragePath(String currentDirStoragePath) {
        this.currentDirStoragePath = currentDirStoragePath;
    }
    //endregion

    //region camera functions
    /**
     * Launch the camera, and show video from cam into an ImageView.
     * @param camView ImageView to output the video
     */
    private void launchCam(ImageView camView){
        startCamera();
        Runnable getFrameRunnable = new Runnable() {
            public void run() {
                updateViewImage(camView);
            }
        };
        executor.scheduleAtFixedRate(getFrameRunnable, 0, 33, TimeUnit.MILLISECONDS);//30fps
    }

    /**
     * Update an Image View image with the current frame get from grabber.
     * @param camView ImageView to output the Image
     */
    private void updateViewImage(ImageView camView){
        try {
            Frame frame = grabber.grab(); // Frame frame = grabber.grabFrame();
            if (frame != null) {
                WritableImage img = frameToImage(frame);
                camView.setImage(img);
            }
        } catch (Exception e) {}
    }

    /**
     * Start the camera capture (by launching grabber).
     */
    private void startCamera(){
        try {
            grabber.start();
        }catch (Exception e){}
    }

    /**
     * Close camera by stopping grabber.
     */
    private void closeCam(){
        try{
            grabber.stop();
        }catch (Exception e){
            System.out.println(e);
        }
    }

    /**
     * Close camera by stopping grabber, erase cam ImageView and label
     * @param camView   => ImageView displaying camera capture video
     * @param camLabel  => Label corresponding to the camera capture
     */
    private void closeCam(ImageView camView, Label camLabel){
        closeCam();
        camView.setImage(null);
        camLabel.setText("");
    }
    //endregion

    //region save image functions
    /**
     * Save an image File with it's current name into a predefined directory.
     * @param fileToSave the image file we have to save.
     */
    private void saveImageFile(File fileToSave){
        try {
            BufferedImage bufferedImage = ImageIO.read(fileToSave);
            saveImageWithSelectDir(bufferedImage, fileToSave.getName());
        } catch (Exception ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * Save an image File with given name (don't forget extension) into a predefined directory.
     * @param fileToSave the image file we have to save.
     */
    private void saveImageFile(File fileToSave, String fileName){
        try {
            BufferedImage bufferedImage = ImageIO.read(fileToSave);
            saveImageWithSelectDir(bufferedImage, fileName);
        } catch (Exception ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * Save an image as file with a name into a predefined directory.
     * @param image the image we have to save
     * @param name  image file's name
     */
    private void saveImageWithSelectDir(BufferedImage image, String name){
        System.out.println("save in dir : " + this.bestLabel + "-" +this.allBestLabels.get(this.bestLabel)*100+"%-filter"+this.currentFilter);

        if (this.getCurrentDirStoragePath()==null){
            File selectedDirectory = selectStorageDir();
            if (selectedDirectory != null) {
                this.setCurrentDirStoragePath(selectedDirectory.getPath());
                saveImage(image, this.getCurrentDirStoragePath() + "/" + name);
            }
        }
        else{
            saveImage(image, this.getCurrentDirStoragePath() + "/" + name);
        }
        System.out.println(this.getCurrentDirStoragePath() + "/" + name);
    }

    /**
     * Save an image as jpg thanks to a path.
     * @param image the image we have to save
     * @param path  the path where to save image
     */
    private void saveImage(BufferedImage image, String path){
        try {
            ImageIO.write(image, "jpg", new File(path));
        } catch (Exception ioException) {
            ioException.printStackTrace();
        }
    }
    //endregion

    //region recognise image and run save
    /**
     * Update a label depending on the label with better probability.
     * @param copy  a float matrix copy of Tensor flow response
     * @param label Java FX Label that will be set
     */
    private void updateGetLabels(float[][] copy, Label label){
        this.allBestLabels = matrix.getLabelsFromMaxMatrix(copy, imageRecognition.getLabels());
        System.out.println(allBestLabels);
        this.bestLabel = imageRecognition.getImagePotentialLabel(this.allBestLabels);
        System.out.println(bestLabel);
        setLabelText(label, bestLabel);
    }

    /**
     * Run a function if best labels check with definition TextField value.
     * Supposed to be used to save an image into a directory.
     * @param callback a runnable function to run
     */
    private void runCheckSaveFunction(Runnable callback){
        //region check our definition with labels found
        if (allBestLabels.containsKey(txtFieldDef.getText())){
            System.out.println("IA agree");
            if(choiceBox.getValue()!=null) {
                for (Map.Entry mapEntry : allBestLabels.entrySet()) {
                    float probaTF = (Float) mapEntry.getValue() * 100;
                    int choiceProba = Integer.parseInt(choiceBox.getValue().toString().replaceAll("%", ""));
                    if (probaTF > choiceProba) {
                        callback.run();
                    }
                }
            }
        }
        else{
            System.out.println("IA disagree");
        }
        //endregion
    }

    /**
     * Use Tensor Flow to get some labels corresponding to an image, get from file.
     * An image view is set here, to see on the window the selected picture.
     * After this, we try to save this image into a directory.
     * @param file          => an image File
     * @param imageLabel    => a Label associated to an image
     * @param imageView     => an ImageView that will contains the selected image
     */
    private void recognise(File file, Label imageLabel, ImageView imageView){
        float[][] copy = imageRecognition.executeModelFromByteArray(imageRecognition.ConvertByteToTensor(file));
        updateGetLabels(copy, imageLabel);
        //just try if img set works, it's fine. but seems image have to be in resources dir.
        String[] pathArr = file.getAbsolutePath().split("/resources");
        imageView.setImage(new Image(this.getClass().getResource(pathArr[pathArr.length-1]).toString()));
        try {
            this.currentImg = ImageIO.read(file);
        } catch (Exception e) {
        }

        runCheckSaveFunction(()->saveImageFile(file,txtFieldDef.getText() + ".jpg"));
    }

    /**
     * Use Tensor Flow to get some labels corresponding to an image, get from camera
     * with OpenCVFrameGrabber.
     * After this, we try to save this image into a directory.
     * @param camLabel
     */
    private void recognise(Label camLabel) {
        byte [] data = null;
        try {
            this.currentImg = converter.convert(grabber.grab());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            ImageIO.write( this.currentImg, "jpg", bos );
            data= bos.toByteArray();
        }catch (Exception e){
        }

        try {
            float[][] copy = imageRecognition.executeModelFromByteArray(imageRecognition.setByteFile(data));
            updateGetLabels(copy, camLabel);
            runCheckSaveFunction(() -> saveImageWithSelectDir(this.currentImg, txtFieldDef.getText() + ".jpg"));
        }catch (Exception e){
            setLabelText(camLabel, "");
        }
    }
    //endregion

    /**
     * Convert a Frame into Writable image.
     * @param frame => a frame, probably get thanks to a camera or video.
     * @return an image usable into java FX ImageView
     */
    private WritableImage frameToImage(Frame frame) {
        BufferedImage bufferedImage = converter.getBufferedImage(frame);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    /**
     * Set label text value in FX thread.
     * @param label => a label to update
     * @param text  => text to insert in label
     */
    private void setLabelText(Label label, String text){
        Platform.runLater(()->{ // be in FX application thread
            label.setText(text);
        });
    }

    @Override
    public void start(Stage primaryStage) {
        //region initialise all elements
        primaryStage.setTitle("Hello World!");
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
        this.root = new BorderPane();

        //region create textfield & it's label for image description
        Button selectFileBtn = new Button();// select button
        this.txtFieldDef = new TextField();
        txtFieldDef.setPromptText("dog, cat ...");

        Label defFieldLabel = new Label("image description");
        //endregion

        //region create image view & it's label
        final ImageView imageView = new ImageView(); //place for image
        Label imageLabel = new Label("no text");
        //endregion

        ChoiceBox choiceBox = new ChoiceBox();
        choiceBox.setValue("Percent confidence");

        ChoiceBox choiceBoxFilter = new ChoiceBox();
        choiceBoxFilter.setValue("No Filter");

        ChoiceBox choiceBoxFilter2 = new ChoiceBox();
        choiceBoxFilter2.setValue("No Filter");

        ChoiceBox choiceBoxFilter3 = new ChoiceBox();
        choiceBoxFilter3.setValue("No Cadre");

        Button btn = new Button();// select button
        this.directoryChooser = new DirectoryChooser();

        Button btnSourcePicsNoIA = new Button();// select button
        btnSourcePicsNoIA.setText("Image Filter");

        //region top panel select buttons
        Button btnSourceCam = new Button();
        Button btnSourcePics = new Button();

        this.choiceBox = new ChoiceBox();
        this.directoryChooser = new DirectoryChooser();
        Button selectDirBtn = new Button();// select button
        //endregion

        //region cam
        ImageView camView  = new ImageView();
        Label camLabel = new Label();

        grabber.setImageWidth(300);
        grabber.setImageHeight(300);
        //endregion

        //endregion

        // region Create Button select file
        selectFileBtn.setText("Select a file");
        selectFileBtn.setOnAction((action) -> {
            File file = fileSelector.selectFile(primaryStage);
            recognise(file, imageLabel, imageView);
            Lighting effectColor = this.filter.filterColor(choiceBoxFilter);
            if (effectColor != null)
                imageView.setEffect(effectColor);
            BufferedImage backImg = SwingFXUtils.fromFXImage(imageView.getImage(), null);
            saveImageWithSelectDir(backImg,this.bestLabel + "-" +this.allBestLabels.get(this.bestLabel)*100+"%-filter"+this.currentFilter+".jpg");
        });
        //endregion


        //region select new dir
        selectDirBtn.setText("Select Dir");
        selectDirBtn.setOnAction((action) -> {
            this.setCurrentDirStoragePath(this.selectStorageDir().getPath());

        });
        //endregion

        //region select sources buttons
        //region button select source from cam
        btnSourceCam.setText("Select camera as source");
        btnSourceCam.setOnAction((action) -> {
            //add all extension for cam (or video)
            //fileSelector.setExtFilter("video", "*.mp4");

            launchCam(camView);

            //region repeat image recognition function
            if (!this.isExecutorLaunched) {
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        recognise(camLabel);
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 0, 1000);
                this.isExecutorLaunched = true;
            }
            //endregion
        });
        //endregion

        //region button select source from pictures
        btnSourcePics.setText("Select picture as source");
        btnSourcePics.setOnAction((action) -> {
            //add all extension for cam (or video)
            fileSelector.setExtFilter("Images", "*.jpeg", "*.jpg");
            closeCam(camView, camLabel);
            this.isExecutorLaunched = false;

        });
        //endregion

        //region checkbox percent confidence
        choiceBox.getItems().addAll("5%", "50%", "60%", "70%","80%","90%","100%");
        choiceBox.setValue("50%");
        choiceBox.setOnAction(event1 -> {
            System.out.println(choiceBox.getValue());
        });
        //endregion

        choiceBoxFilter.getItems().addAll("No Filter", "Red", "Blue", "Green");
        choiceBoxFilter.setOnAction(event -> {
            this.currentFilter = choiceBoxFilter.getValue().toString();
        });

        choiceBoxFilter3.getItems().addAll("No Filter", "Classik");

        choiceBoxFilter2.getItems().addAll("No Filter", "Red", "Blue", "Green");
        ImageView imageView2 = new ImageView();

        btnSourcePicsNoIA.setOnAction(event1 -> {
            //Get path to Image
            File file = this.fileSelector.selectFile(primaryStage);
            String[] pathArr = file.getAbsolutePath().split("/resources");
            Image resBottom = new Image(this.getClass().getResource(pathArr[pathArr.length-1]).toString());

            imageView2.setImage(resBottom);
            Lighting effectColor = this.filter.filterColor(choiceBoxFilter2);
            if (effectColor != null)
                imageView2.setEffect(effectColor);
        });
        Label labelCadre = new Label();
        choiceBoxFilter3.setOnAction(event1 -> {
            try {
                if (this.filter.getCadre(choiceBoxFilter3.getValue().toString()) != null)
                {
                    InputStream stream = new FileInputStream("/Users/mac/Desktop/Cours/JavaAvance/ProjectCam/build/resources/main/img/cadre3c.png");
                    Image image = new Image(stream);
                    ImageView imageView3 = new ImageView(image);
                    imageView3.setFitWidth(100);
                    imageView3.setFitHeight(100);
                    labelCadre.setTranslateY(-100);
                    labelCadre.setGraphic(imageView3);
                    labelCadre.setVisible(true);
                }
                else
                {
                    labelCadre.setVisible(false);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

        //region manage display -- background could change, just used to debug for now
        //region initialize window
        final int rootWidth = 1200;
        final int rootheight = 700;
        root.setStyle("-fx-background-color: #CCCCCC;");

        primaryStage.setScene(new Scene(root, rootWidth, rootheight));
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);

        imageView2.setFitHeight(100);
        imageView2.setFitWidth(100);

        //endregion

        //region top - select source panel
        FlowPane sourceSelectPan = new FlowPane();
        root.setStyle("-fx-background-color: #CDCD5C;");

        sourceSelectPan.getChildren().add(btnSourceCam);
        sourceSelectPan.getChildren().add(btnSourcePics);
        sourceSelectPan.getChildren().add(choiceBox);
        sourceSelectPan.getChildren().add(choiceBoxFilter);

        sourceSelectPan.getChildren().add(btnSourcePicsNoIA);
        sourceSelectPan.getChildren().add(choiceBoxFilter2);
        sourceSelectPan.getChildren().add(choiceBoxFilter3);
        sourceSelectPan.getChildren().add(selectDirBtn);
        //endregion


        //region left - selectPic button + definition panel
        FlowPane selectionPan = new FlowPane(Orientation.VERTICAL);
        //selectionPan.setPrefWidth(rootWidth/4);
        selectionPan.getChildren().addAll(selectFileBtn, txtFieldDef, defFieldLabel);
        selectionPan.setStyle("-fx-background-color: #CD5C5C;");
        //endregion
        
        //region center - image panel
        FlowPane picsSelectionPan = new FlowPane(Orientation.VERTICAL);
        picsSelectionPan.setPrefWidth(rootWidth);
        picsSelectionPan.setStyle("-fx-background-color: #CD5CCD;");
        picsSelectionPan.getChildren().addAll( imageView, imageLabel);

        picsSelectionPan.getChildren().addAll(imageView2);
        picsSelectionPan.getChildren().addAll(labelCadre);
        //endregion

        //region center - cam panel
        FlowPane camPan = new FlowPane(Orientation.VERTICAL);
        //camPan.setPrefWidth(rootWidth/2);
        camPan.setStyle("-fx-background-color: #CD5CCD;");
        camPan.getChildren().addAll( camView, camLabel);
        //endregion

        //region add panel into Root panel
        root.setTop(sourceSelectPan);
        root.setLeft(selectionPan);
        root.setRight(picsSelectionPan);
        root.setCenter(camPan);
        //endregion

        primaryStage.show();
        //endregion
    }
}
