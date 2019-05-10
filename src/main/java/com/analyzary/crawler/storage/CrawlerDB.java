package com.analyzary.crawler.storage;


/**
 * Crawler data base object abstraction
 */
public interface CrawlerDB {
    void createCollection(String name);

    CrawlerDBCollection getCollection(String namne);

    void deleteCollection(String name);

    void clearCollection(String name);
}
