package com.analyzary.crawler.storage;

import java.util.List;

public interface CrawlerDBCollection {
    String getElementById(String id);

    List<String> getAllElements();

    void insertElement(String id, String element);
}
