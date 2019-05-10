package com.analyzary.crawler.storage;

import java.util.List;


/**
 * Crawler data base client object abstraction
 */
public interface CrawlerDBClient {
    CrawlerDB getDB(String name);

    List<String> getDatabaseNames();
}
