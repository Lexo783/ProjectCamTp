package com.packager.logger;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * this class is used to write message in a log file, to register all operation we had made, with the date and time
 */
public class FilterLoggerFile implements FilterLogger{

    /**
     * this method write a message given in parameters with the date and time in a log file
     * @param message       String : the message we want to write into the log file
     * @param filePath      String : the filepath of the log file to open it
     */
    @Override
    public void log(String message, String filePath) {
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        try {
            FileWriter writer = new FileWriter(filePath, true);
            writer.write("\n" + format.format(date)+ message);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
