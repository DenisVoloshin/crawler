package com.analyzary.crawler;

import com.analyzary.crawler.config.AppConfig;
import com.analyzary.crawler.config.ConfigurationManager;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class CrawlerApplication {

    private static String usage = "Wrong or missing required argument: \n" +
            "\n" +
            "usage: com.analyzary.crawler.CrawlerApplication \n" +
            " -r,--root <arg>    URL of the root page \n" +
            " -d,--depth <arg>   depth limit";


    public static void main(String[] args) {

        // parse app input arguments
        // if arguments struct would be more complex, some third party lib could be used
        if (args.length != 4) {
            System.out.println(usage);
            System.exit(0);
        }

        List<String> argumentsAsList = Arrays.asList(args);
        String rootUrl = readArgument(argumentsAsList, "-r", "--root");
        String depth = readArgument(argumentsAsList, "-d", "--depth");
        if (rootUrl == null || depth == null) {
            System.out.println(usage);
            System.exit(0);
        }

        // parse --depth should be int

        int depthAsInt = -1;

        try {
            depthAsInt = Integer.parseInt(depth);
        } catch (NumberFormatException ex) {
            System.out.println(usage);
            System.exit(0);
        }

        AppConfig appConfig = new AppConfig();
        appConfig.setDepth(depthAsInt);
        appConfig.setCachePath(Paths.get("").toAbsolutePath().toString() + File.separator + "cache");
        appConfig.setRootUrl(rootUrl);

        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        configurationManager.loadAppConfiguration(appConfig);
        Crawler crawler = new Crawler(configurationManager);
        crawler.start();
    }


    private static String readArgument(List<String> args, String shortName, String fullName) {
        Iterator<String> arguments = args.iterator();
        String argValue = null;
        while (arguments.hasNext()) {
            String option = arguments.next();
            if (option.equals(shortName) || option.equals(fullName) && arguments.hasNext()) {
                argValue = arguments.next();
                break;
            }
        }
        return argValue;
    }
}
