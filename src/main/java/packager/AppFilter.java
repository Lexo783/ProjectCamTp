package com.packager;

import com.packager.exception.FilterException;
import com.packager.filters.*;
import com.packager.logger.FilterLogger;
import com.packager.logger.FilterLoggerFile;
import org.apache.commons.cli.*;
import org.bytedeco.opencv.opencv_core.Mat;
import org.reflections.Reflections;

import java.io.File;
import java.util.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

/**
 * this is the main class of our app
 */
public abstract class AppFilter {

    /**
     * this is the main method, hope you're surprised
     * @param args      the program arguments, you can defined them by editing Configuration
     */
    public static void main(String[] args) {
        System.out.println("application has started");
        //program args:     -help -i src/main/java/imageIn -o src/main/java/imageOut -f blur:29|dilate:10|grayscale
        //                  -h -i src/main/java/imageIn  -o src/main/java/imageOut -f blur:15 -c src/config.ini -lf -l src/AppFilter.log

        try {
            List<String> listCommandArgs = createCLI(args);
            Map<IFilter, Integer> filtersOptions = whichFilters( listCommandArgs );
            usingFilters(filtersOptions , listCommandArgs);

            dumpLog(listCommandArgs.get(3), listCommandArgs.get(4));
        }catch (Exception e){
            System.out.println(e);
        }

    }


    /**
     * this method generate the CLI, get all the information we need from the program arguments and give them thanks to a List containing all
     * important information related to the entered arguments.
     * need to have input dir, output dir, filter args, log filename
     * @param args  the program arguments, you can defined them by editing Configuration
     * @return      List : a String list used to have the in directory , out directory, all the filters arguments, information if we want
     * to see the log file
     */
    private static List<String> createCLI(String[] args) throws FilterException {
        String dirIn=null;
        String dirOut=null;
        String filterArg=null;
        boolean useConfig = false;

        String logFile =null;
        String iniFile = null;

        HelpFormatter formatter = new HelpFormatter();
        List<String> listCommandArgs = new ArrayList<>();
        String printLogger = "false";

        //region filters list generator
        Reflections reflections = new Reflections(IFilter.class);
        List<String> filterListRaw = new ArrayList<>();
        List<String> filterList = new ArrayList<>();

        Set<Class<? extends IFilter>> subTypes = reflections.getSubTypesOf(IFilter.class);
        String[] subTypesString = subTypes.toString().split("\\,");
        for(String s : subTypesString){
            String [] splited = s.split("\\.");
            filterListRaw.add(splited[splited.length-1]);

        }
        for(String s : filterListRaw){
            String [] splited = s.split("]");
            filterList.add(splited[0]);
        }
        //endregion


        //region CLI generator
        Options options = new Options();
        options.addOption("h", "--help", false, "")
                .addOption("i", "input-dir", true, "<directory>")
                .addOption("o", "output-dir", true, "<directory>")
                .addOption("f", "filters", true, "filters options")
                .addOption("l", "log-file", true, "<File> *.log")
                .addOption("c", "config-file", true, "<File> *.ini")
                .addOption("lf", "list-filters", false, "show filters list")
        ;

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if(cmd.hasOption("h")){
                formatter.printHelp("imageFilter", options);
            }
            if(cmd.hasOption("i")){
                dirIn = cmd.getOptionValue("i");
            }
            if(cmd.hasOption("o")){
                dirOut = cmd.getOptionValue("o");
            }
            if(cmd.hasOption("f")){
                filterArg = cmd.getOptionValue(("f"));
            }
            if(cmd.hasOption("l")){
                printLogger = "true";
                logFile = cmd.getOptionValue("l");
            }
            if(cmd.hasOption("c")){
                useConfig = true;
                iniFile = cmd.getOptionValue("c");
            }
            if(cmd.hasOption("lf")){
                for(String s : filterList){
                    System.out.println("=> " + s);
                }
            }

        }catch (Exception e ){
            System.out.println(e + "\nhere the command you can use:");
            formatter.printHelp("AppFilter", options);
        }
        //endregion


        if (useConfig) {
            OpenIni open = new OpenIni();
            try {
                List<String> listIni = open.openIni(iniFile);
                dirIn = listIni.get(0);
                dirOut = listIni.get(1);
                filterArg = listIni.get(2);
                logFile = listIni.get(3);
            } catch (Exception e) {
                System.out.println(e);
            }
        }


        listCommandArgs.add(dirIn);
        listCommandArgs.add(dirOut);
        listCommandArgs.add( filterArg);
        listCommandArgs.add( printLogger);
        listCommandArgs.add(logFile);

        if (listCommandArgs.contains(null))
        {
            throw new FilterException("you don't have specified enough argument to use AppFilter");
        }
        else{
            return listCommandArgs;
        }

    }



    /**
     * this method analyse the filters arguments from a List given in parameter, and create an HashMap with all the selected filters and their power
     * @param listCommandArgs   List : a String list used here to have all the filters arguments
     * @return                  Map : containing all filter's type and their effect's power
     */
    private static Map<IFilter, Integer> whichFilters(List<String> listCommandArgs) throws FilterException {
        int size = 0;
        Map<IFilter, Integer> filtersOptions = new HashMap<>();
        FilterLogger logger = new FilterLoggerFile();
        String[] splitFilters = listCommandArgs.get(2).split("\\|");
        String logfile = listCommandArgs.get(4);

        for (String s : splitFilters){
            String[] splitValue = s.split(":");

            switch (splitValue[0]) {
                case "blur":
                    try {
                        size = Integer.parseInt(splitValue[1]);
                        filtersOptions.put(new BlurFilter(), size);
                        logger.log(" you want to apply " + splitValue[0] + ". value: " + splitValue[1], logfile);
                    }catch (Exception e) {
                        System.out.println("you don't give valid value for filters: " + splitValue[0]);
                        }
                    break;

                case "grayscale":
                    filtersOptions.put(new BnWFilter(), 0);
                    logger.log(" you want to apply " + splitValue[0], logfile);
                    break;

                case "dilate":
                    try {
                        size = Integer.parseInt(splitValue[1]);
                        filtersOptions.put(new DilateFilter(), size);
                        logger.log(" you want to apply " + splitValue[0] + ". value: " + splitValue[1], logfile);
                    }catch (Exception e){
                        System.out.println("you don't give valid value for filters: " + splitValue[0]);
                    }
                    break;

                case "zeteam":
                    try {
                        size = Integer.parseInt(splitValue[1]);
                        filtersOptions.put(new FilterZeTeam(), size);
                        logger.log(" you want to apply " + splitValue[0] + ". value: " + splitValue[1], logfile);
                    }catch (Exception e){
                        System.out.println("you don't give valid value for filters: " + splitValue[0]);
                    }
                    break;
            }
        }


        if(filtersOptions.isEmpty()){
            throw new FilterException(" you don't have entered valid filter options");
        }
        else {
            return filtersOptions;
        }
    }



    /**
     * this method apply filters - containing in the Map in parameters - to all Files from a specified directory and save them in an other.
     * directories are in the listCommandArgs (0 for input directory, 1 for output directory)
     * @param filtersOptions    Map : containing an IFilter as key, and Integer as value for each filters selected from the CLI
     * @param listCommandArgs   List : a String list used here to have the in directory and the out directory
     */
    private static void usingFilters(Map<IFilter, Integer> filtersOptions, List<String> listCommandArgs){
        String dirIn = listCommandArgs.get(0);
        String[] dirInTab = dirIn.split("/");
        String dirInRac = dirInTab[dirInTab.length-1];

        String dirOut = listCommandArgs.get(1);
        String[] dirOutTab = dirOut.split("/");
        String dirOutRac = dirOutTab[dirOutTab.length-1];

        File rep = new File(dirIn);
        String[] liste = rep.list();

        FilterLogger logger = new FilterLoggerFile();


        if (liste != null){

            for(int i =0; i<liste.length;i++) {
                String pathIn = dirIn + "/" + liste[i];
                String pathOut = dirOut + "/" + liste[i];

                try {
                    Mat img = imread(pathIn);
                    for (Map.Entry<IFilter, Integer> entry : filtersOptions.entrySet()) {
                        img = entry.getKey().filter(img, entry.getValue());
                        String beginMessage = " => " + entry.getKey().logDescription(entry.getValue()) + " on " + liste[i];
                        String endMessage = " from Directory " + dirInRac + " \t(" + dirIn + "). \t\t\tsave in directory " + dirOutRac + " \t(" +  dirOut + ").";
                        logger.log(beginMessage + endMessage , listCommandArgs.get(4));
                    }
                    imwrite(pathOut, img);

                } catch (Exception e) {
                    System.out.println("can't open the file: " + pathIn + "  => " + e);
                }
            }
        }
    }


    /**
     * this method print the contents from a file with .log extension
     * @param printLogger   String : used to know if the user want to print the content or not (yes , as a boolean)
     */
    private static void dumpLog(String printLogger, String pathName) {
        File f = new File(pathName);
        f.exists();

        if (printLogger.equals("true")) {
            try {
                Scanner myReader = new Scanner(f);
                System.out.println("opening file ...");
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    System.out.println(data);
                }
                myReader.close();

            } catch (Exception e) {
                System.out.println("An error occurred when opening file.");
                e.printStackTrace();
            }
        }
    }
}

