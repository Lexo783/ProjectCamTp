package Services;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;

public class FileSelector {

    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private final FileChooser fileChooser = new FileChooser();

    public File selectFile(Stage primaryStage){
        fileChooser.setTitle("Select");
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
