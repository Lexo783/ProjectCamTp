package Services;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;

public class FileSelector {

    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private final FileChooser fileChooser = new FileChooser();
    private FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg");;


    public FileChooser.ExtensionFilter getExtFilter() {
        return extFilter;
    }

    public void setExtFilter(final String description, final String... extensions){
        System.out.println("try set ext");
        for (String ext : extensions){
            System.out.println(ext);
        }
        this.extFilter = new FileChooser.ExtensionFilter(description, extensions);
        System.out.println("try set ext in fileChooser");

        fileChooser.getExtensionFilters().add(this.getExtFilter());
    }


    public File selectFile(Stage primaryStage){
        fileChooser.setTitle("Select");
        fileChooser.getExtensionFilters().add(this.getExtFilter());
        return fileChooser.showOpenDialog(primaryStage);
    }

    public void selectDirectoryFiles()
    {
        configuringDirectoryChooser(directoryChooser);
    }

    private void configuringDirectoryChooser(DirectoryChooser directoryChooser) {
        // Set title for DirectoryChooser
        directoryChooser.setTitle("Select Some Directories");

        // Set Initial Directory
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    }
}
