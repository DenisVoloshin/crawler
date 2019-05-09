package com.analyzary.crawler.storage;

import com.analyzary.crawler.config.ConfigurationManager;
import com.analyzary.crawler.util.FileUtils;

import javax.annotation.CheckForNull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FileSystemDBCollection implements CrawlerDBCollection {

    private String collectionName;
    private ConfigurationManager configurationManager;
    private String dbName;


    public FileSystemDBCollection(String dbName, String collectionName, ConfigurationManager configurationManager) {
        this.dbName = dbName;
        this.collectionName = collectionName;
        this.configurationManager = configurationManager;
    }


    @Override
    @CheckForNull
    public String getElementById(String id) {
        File collection = new File(configurationManager.getDBRootFolder() + File.separator
                + dbName + File.separator + collectionName);
        if (collection.exists()) {

            Optional<File> element = Arrays.asList(collection.listFiles()).stream().parallel().filter(file -> file.getName().equals(id)).findFirst();

            if (element.isPresent()) {
                File file = element.get();
                try {
                   return FileUtils.readFile(file.getAbsolutePath());
                } catch (IOException e) {
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public List<String> getAllElements() {
        ArrayList<String> elements = new ArrayList<>();
        File collection = new File(configurationManager.getDBRootFolder() + File.separator
                + dbName + File.separator + collectionName);
        Arrays.asList(collection.listFiles()).stream().forEach(file -> {
            try {
                elements.add(FileUtils.readFile(file.getAbsolutePath()));
            } catch (IOException e) {
                // TODO
            }
        });
        return elements;
    }

    @Override
    public void insertElement(String id, String element) {
        try {
            FileUtils.writeFile(element.getBytes(), configurationManager.getDBRootFolder() + File.separator
                    + dbName + File.separator + collectionName + File.separator + id);
        } catch (IOException e) {
            // TODO
        }
    }
}
