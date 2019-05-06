package com.analyzary.crawler.storage;

public interface CrawlerDB {
    void createCollection(String name);

    CrawlerDBCollection getCollection(String namne);

    void deleteCollection(String name);

    void clearCollection(String name);
}
