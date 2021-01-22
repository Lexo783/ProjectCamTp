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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javafx.scene.image.*;
import javafx.stage.WindowEvent;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import javax.imageio.ImageIO;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Launcher extends Application {

    //region class attributes
    private final FileSelector fileSelector = new FileSelector();
    private DirectoryChooser directoryChooser = new DirectoryChooser();
    private Java2DFrameConverter converter = new Java2DFrameConverter();
    private OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
    private BorderPane root;
    private Filter filter = new Filter();
    private String currentColorFilter;
    private Map<String, String> filterMap = new HashMap<String, String>();
    private ChoiceBox choiceBox;
    private TextField txtFieldDef;

    private ImageRecognition imageRecognition = new ImageRecognition();
    private Matrix matrix = new Matrix();
    private Map<String,Float> allBestLabels;
    private String bestLabel;
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private Boolean isExecutorLaunched =false;
    private BufferedImage currentImg;

    private String currentDirStoragePath;
    private Label labelCadre = new Label();
    private Label labelTampon = new Label();

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
                setViewColor(camView);
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
        System.out.println("save in dir : " + name);

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
        System.out.println(this.bestLabel);
        setLabelText(label, this.bestLabel);
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

    //region filters usage functions
    /**
     * Take a snapshot of an ImageView and save the image into a directory
     * @param imageView
     */
    private void viewSaveSnapshot(ImageView imageView){
        WritableImage writableImage = new WritableImage((int) imageView.getFitWidth(), (int)imageView.getFitHeight());
        imageView.snapshot(null, writableImage);

        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(writableImage, null);
        BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(), BufferedImage.OPAQUE);

        Graphics2D graphics = bufImageRGB.createGraphics();
        graphics.drawImage(bufImageARGB, 0, 0, null);

        StringBuilder filterApplied = new StringBuilder();
        for (Map.Entry mapEntry : this.filterMap.entrySet()) {
            filterApplied.append("_" + mapEntry.getKey() + "(" + mapEntry.getValue() + ")" );
        }
        String fileName = this.bestLabel + "-" +this.allBestLabels.get(this.bestLabel)*100+"%-" + filterApplied.toString() +".jpg";
        saveImageWithSelectDir(bufImageRGB, fileName);
        graphics.dispose();
        System.out.println( "Image saved at: " + fileName);
    }

    /**
     * Refresh imageView with last image loaded.
     * Useful to apply filters on image in real time.
     * @param imageView => ImageView to refresh
     */
    private void refreshImageView(ImageView imageView){
        if(this.currentImg!=null) {
            WritableImage writableImage = SwingFXUtils.toFXImage(this.currentImg, null);
            imageView.setImage(writableImage);
        }
    }

    /**
     * Refresh an imageView thanks to currentImage, and apply color filter.
     * @param imageView => ImageView to resfresh
     */
    private void setViewColorWithRefresh(ImageView imageView){
        refreshImageView(imageView);
        setViewColor(imageView);
    }

    /**
     * Set an imageView color filter depending on currentColorFilter.
     * @param imageView => ImageView to update
     */
    private void setViewColor(ImageView imageView){
        try {
            Color filterColor = this.filter.getColor(this.currentColorFilter);
            if (filterColor != null) {
                imageView.setEffect(this.filter.filterColor(filterColor));
            } else {
                imageView.setEffect(null); // ?filterColor(as null) != null ? don't have same effect
            }
        }catch (Exception e){
        }
    }

    /**
     * Apply a cadre on a label, that will be displayed over an image
     */
    public void setCadre(){
        try {
            //flo == "/Users/mac/Desktop/Cours/JavaAvance/ProjectCam/build/resources/main/img/cadre3c.png"
            InputStream stream = new FileInputStream("/Users/gwenael/Documents/cours/L2/janvier_agileTesting_javaAvancee/javaAvLexos/ProjectCamTp/build/resources/main/img/cadre3c.png");
            Image image = new Image(stream);
            ImageView imageView3 = new ImageView(image);
            imageView3.setFitWidth(100);
            imageView3.setFitHeight(100);
            labelCadre.setTranslateY(-100);
            labelCadre.setGraphic(imageView3);
            labelCadre.setVisible(false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Apply a cadre on a label, that will be displayed over an image.
     * it will change depending on path given.
     * @param resourcePath => path from resources Directory
     */
    public void setCadre(String resourcePath){
        try {
            //flo == "/Users/mac/Desktop/Cours/JavaAvance/ProjectCam/build/resources/main/img/cadre3c.png"
            InputStream stream = new FileInputStream("/Users/gwenael/Documents/cours/L2/janvier_agileTesting_javaAvancee/javaAvLexos/ProjectCamTp/build/resources/main" + resourcePath);
            Image image = new Image(stream);
            ImageView imageView3 = new ImageView(image);
            imageView3.setFitWidth(100);
            imageView3.setFitHeight(100);
            labelCadre.setTranslateY(-100);
            labelCadre.setGraphic(imageView3);
            labelCadre.setVisible(true);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * Apply a Stamp on a label, that will be displayed over an image
     */
    public void setStamp(){
        try {
            InputStream stream = new FileInputStream("/Users/gwenael/Documents/cours/L2/janvier_agileTesting_javaAvancee/javaAvLexos/ProjectCamTp/build/resources/main/img/certified.png");
            Image image = new Image(stream);
            ImageView imageView4 = new ImageView(image);
            imageView4.setFitWidth(30);
            imageView4.setFitHeight(30);

            labelTampon.setTranslateY(-145);
            labelTampon.setTranslateX(60);
            labelTampon.setGraphic(imageView4);
            labelTampon.setVisible(false);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Apply a Stamp on a label, that will be displayed over an image
     */
    public void setStamp(String resourcePath){
        try {
            InputStream stream = new FileInputStream("/Users/gwenael/Documents/cours/L2/janvier_agileTesting_javaAvancee/javaAvLexos/ProjectCamTp/build/resources/main" + resourcePath);
            Image image = new Image(stream);
            ImageView imageView4 = new ImageView(image);
            imageView4.setFitWidth(30);
            imageView4.setFitHeight(30);

            labelTampon.setTranslateY(-145);
            labelTampon.setTranslateX(60);
            labelTampon.setGraphic(imageView4);
            labelTampon.setVisible(true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
        //region primaryStage setup
        primaryStage.setTitle("Image identifier");
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });
        //endregion

        this.root = new BorderPane();

        //region create textfield & it's label for image description
        this.txtFieldDef = new TextField();
        txtFieldDef.setPromptText("dog, cat ...");

        Label defFieldLabel = new Label("image description");
        //endregion

        //region create image view & it's label
        final ImageView imageView = new ImageView(); //place for image
        Label imageLabel = new Label();
        //endregion

        //region filters boxes
        ChoiceBox choiceBoxPercent = new ChoiceBox(); // choice percentage to allow image save
        choiceBoxPercent.setValue("Percent confidence");


        ChoiceBox choiceBoxFilterColor = new ChoiceBox(); //  choice image color filter
        choiceBoxFilterColor.setValue("No Filter");

        ChoiceBox choiceBoxFilterFramework = new ChoiceBox(); // choice image cadre
        choiceBoxFilterFramework.setValue("No Cadre");

        ChoiceBox choiceBoxFilterFrameworkCertified = new ChoiceBox();
        choiceBoxFilterFrameworkCertified.setValue("No Certif");
        //endregion


        //region top panel select buttons
        Button btnSourceCam = new Button();     // launch cam
        Button selectFileBtn = new Button();    // select file to open
        this.choiceBox = new ChoiceBox();

        Button selectDirBtn = new Button();// select dir to store image
        Button btnSave = new Button();     // save image
        //endregion

        //region cam
        ImageView camView  = new ImageView();
        Label camLabel = new Label();

        grabber.setImageWidth(300);
        grabber.setImageHeight(300);
        //endregion

        //endregion


        //region select sources buttons
        //region button select source from cam
        btnSourceCam.setText("Select camera as source");
        btnSourceCam.setOnAction((action) -> {
            launchCam(camView);
            imageLabel.setText("");
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

        // region Create Button select file
        selectFileBtn.setText("Select picture as source");
        selectFileBtn.setOnAction((action) -> {
            fileSelector.setExtFilter("Images", "*.jpeg", "*.jpg");
            closeCam(camView, camLabel);
            this.isExecutorLaunched = false;

            File file = fileSelector.selectFile(primaryStage);
            recognise(file, imageLabel, imageView);

            //region apply color filter on load
            if(file != null){
                setViewColorWithRefresh(imageView);
            }
            //endregion

        });
        //endregion

        //endregion

        //region checkbox percent confidence
        choiceBoxPercent.getItems().addAll("5%", "50%", "60%", "70%","80%","90%","100%");
        choiceBoxPercent.setValue("50%");
        choiceBoxPercent.setOnAction(event1 -> {
            System.out.println(choiceBoxPercent.getValue());
        });
        //endregion


        //region filters
        //region choice box Color filter
        choiceBoxFilterColor.getItems().addAll("No Filter", "Red", "Blue", "Green");
        choiceBoxFilterColor.setOnAction(event1 -> {
            this.currentColorFilter = choiceBoxFilterColor.getValue().toString();
            this.filterMap.put("colorFilter", this.currentColorFilter);
            if (this.filterMap.containsKey("colorFilter") && this.currentColorFilter.equals("No Filter")){
                this.filterMap.remove("colorFilter");
            }
            setViewColorWithRefresh(imageView);
        });
        //endregion

        setCadre();
        setStamp();
        //region choice box framework filter
        choiceBoxFilterFramework.getItems().addAll("No Filter", "Classik", "Or");
        choiceBoxFilterFramework.setOnAction(event1 -> {
                if (this.filter.getCadre(choiceBoxFilterFramework.getValue().toString()) != null)
                {
                    setCadre(this.filter.getCadre(choiceBoxFilterFramework.getValue().toString()));
                }
                else
                {
                    this.labelCadre.setVisible(false);
                }
                this.filterMap.put("frameworkFilter", choiceBoxFilterFramework.getValue().toString());
                if (this.filterMap.containsKey("frameworkFilter") && choiceBoxFilterFramework.getValue().toString().equals("No Filter")){
                    this.filterMap.remove("frameworkFilter");
                }
        });
        //endregion

        //region choice box framework filter
        choiceBoxFilterFrameworkCertified.getItems().addAll("No Filter", "Certifié", "Approved");
        choiceBoxFilterFrameworkCertified.setOnAction(event1 -> {
            if (this.filter.getCertified(choiceBoxFilterFrameworkCertified.getValue().toString()) != null)
            {
                setStamp(this.filter.getCertified(choiceBoxFilterFrameworkCertified.getValue().toString()));
            }
            else
            {
                this.labelTampon.setVisible(false);
            }
            this.filterMap.put("frameworkFilter", choiceBoxFilterFramework.getValue().toString());
            if (this.filterMap.containsKey("frameworkFilter") && choiceBoxFilterFramework.getValue().toString().equals("No Filter")){
                this.filterMap.remove("frameworkFilter");
            }
        });

        //endregion

        //region select new dir to store image
        selectDirBtn.setText("Select output Dir");
        selectDirBtn.setOnAction((action) -> {
            this.setCurrentDirStoragePath(this.selectStorageDir().getPath());
        });
        //endregion

        //region button save
        btnSave.setText("Save image");
        btnSave.setOnAction((action) -> {
            viewSaveSnapshot(imageView);
        });
        //endregion





        //region manage display -- background could change, just used to debug for now
        //region initialize window
        final int rootWidth = 1200;
        final int rootheight = 700;
        root.setStyle("-fx-background-color: #CCCCCC;");

        primaryStage.setScene(new Scene(root, rootWidth, rootheight));
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);


        //endregion

        //region top - select source panel
        FlowPane sourceSelectPan = new FlowPane();
        root.setStyle("-fx-background-color: #000;");

        sourceSelectPan.getChildren().add(btnSourceCam);
        sourceSelectPan.getChildren().add(selectFileBtn);
        sourceSelectPan.getChildren().add(choiceBoxPercent);

        sourceSelectPan.getChildren().add(choiceBoxFilterColor);
        sourceSelectPan.getChildren().add(choiceBoxFilterFramework);
        sourceSelectPan.getChildren().add(choiceBoxFilterFrameworkCertified);

        sourceSelectPan.getChildren().add(selectDirBtn);
        sourceSelectPan.getChildren().add(btnSave);
        //endregion


        //region left - selectPic button + definition panel
        FlowPane selectionPan = new FlowPane(Orientation.VERTICAL);
        //selectionPan.setPrefWidth(rootWidth/4);
        selectionPan.getChildren().addAll( txtFieldDef, defFieldLabel);
        selectionPan.setStyle("-fx-background-color: #CD5C5C;");
        //endregion
        
        //region right - image panel
        FlowPane picsSelectionPan = new FlowPane(Orientation.VERTICAL);
        picsSelectionPan.setPrefWidth(rootWidth);
        picsSelectionPan.setStyle("-fx-background-color: #EEEEEE;");
        picsSelectionPan.getChildren().addAll( imageView, labelCadre, labelTampon, imageLabel );
        //endregion

        //region center - cam panel
        FlowPane camPan = new FlowPane(Orientation.VERTICAL);
        //camPan.setPrefWidth(rootWidth/2);
        camPan.setStyle("-fx-background-color: #EEEEEE;");
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
