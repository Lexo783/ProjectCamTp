package com.packager;
import com.packager.exception.FilterException;
import org.ini4j.Wini;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * this class is used to open an ini file
 */
public class OpenIni {

    /**
     * this method is used to open an ini file given in parameter, and read all information in there. we save them in a List
     * @param filePath              String : filepath of the chosen ini file
     * @return                      List : containing the information collected from the ini file
     * @throws FilterException      FilterException : throws when the file couldn't be open
     */
    public List<String> openIni(String filePath) throws FilterException {

        File file2 = new File(filePath);
        try {
            Wini ini = new Wini(new File(filePath));
            String dirIn = ini.get("general", "inputDir");
            String dirOut = ini.get("general", "outputDir");
            String logFile = ini.get("general", "logFile");
            String filterArgs = ini.get("filters", "content");

            List<String> listIni = new ArrayList<>();
            listIni.add(dirIn);
            listIni.add(dirOut);
            listIni.add(filterArgs);
            listIni.add(logFile);

            return listIni;
        }catch (Exception e){
             throw new FilterException("can't open the selected file");
        }
    }
}
