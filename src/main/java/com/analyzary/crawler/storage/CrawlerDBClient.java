package com.analyzary.crawler.storage;

import java.util.List;

public interface CrawlerDBClient {
    CrawlerDB getDB(String name);

    List<String> getDatabaseNames();
}
