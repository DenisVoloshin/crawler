package com.analyzary.crawler.storage;

import com.analyzary.crawler.config.ConfigurationManager;

import javax.annotation.CheckForNull;
import java.io.File;
import java.util.Arrays;
import java.util.Hashtable;

public class FileSystemCrawlerDB implements CrawlerDB {

    String dbName;
    Hashtable<String, FileSystemDBCollection> collections;
    ConfigurationManager configurationManager;


    public FileSystemCrawlerDB(String dbName, ConfigurationManager configurationManager) {
        this.dbName = dbName;
        this.collections = new Hashtable<>();
        this.configurationManager = configurationManager;
        File dbNameAsFile = new File(configurationManager.getDBRootFolder() + File.separator + dbName);
        if (!dbNameAsFile.exists()) {
            dbNameAsFile.mkdir();
        } else {
            Arrays.asList(dbNameAsFile.listFiles()).stream().parallel().forEach(file -> {
                collections.put(file.getName(), new FileSystemDBCollection(dbName, file.getName(), configurationManager));
            });
        }
    }

    @Override
    public void createCollection(String name) {
        File collection = new File(dbName + File.separator + name);
        if (!collection.exists()) {
            collection.mkdir();
            collections.put(name, new FileSystemDBCollection(dbName, name, configurationManager));
        }
    }

    @Override
    @CheckForNull
    public CrawlerDBCollection getCollection(String name) {
        File collection = new File(dbName + File.separator + name);
        if (collections.get(name) != null) {
            return collections.get(name);
        } else if (!collection.exists()) {
            createCollection(name);
            return collections.get(name);
        } else {
            return collections.get(name);
        }
    }

    @Override
    public void deleteCollection(String name) {
        File collection = new File(dbName + File.separator + name);
        clearCollection(name);
        collection.delete();
    }

    @Override
    public void clearCollection(String name) {
        File collection = new File(dbName + File.separator + name);
        if (collection.exists()) {
            Arrays.asList(collection.listFiles()).stream().forEach(file -> file.delete());
        }
    }
}
