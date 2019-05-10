package com.analyzary.crawler.storage;

import com.analyzary.crawler.config.ConfigurationManager;

import java.util.Arrays;
import java.util.List;

/**
 * Concrete {@link com.analyzary.crawler.storage.CrawlerDBClient} implementation base on locale file system.
 */
public class FileSystemCrawlerDBClient implements CrawlerDBClient {

    ConfigurationManager configurationManager;
    private String name;

    FileSystemCrawlerDBClient(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    @Override
    public CrawlerDB getDB(String name) {
        this.name = name;
        return new FileSystemCrawlerDB(name, configurationManager);
    }

    @Override
    public List<String> getDatabaseNames() {
        return Arrays.asList(new String[]{name});
    }
}
