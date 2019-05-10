package com.analyzary.crawler.storage;

import javax.annotation.CheckForNull;
import java.util.List;


/**
 * Crawler data base collection (table) object abstraction
 */
public interface CrawlerDBCollection {
    @CheckForNull
    String getElementById(String id);

    List<String> getAllElements();

    void insertElement(String id, String element);
}
