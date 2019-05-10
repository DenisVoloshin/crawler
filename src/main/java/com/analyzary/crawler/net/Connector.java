package com.analyzary.crawler.net;

import com.analyzary.crawler.net.request.CrawlerRequest;

import java.util.Map;

/**
 * Defines the abstract http/https client.
 */
public interface Connector {
    int OK_200 = 200;
    String CONTENT_TYPE = "Content-Type";
    String PLAIN_TEXT = "text/html";
    int NOT_MODIFIED_304 = 304;
    int NOT_FOUND = 404;
    String IF_MODIFIED_SINCE = "If-Modified-Since";
    String LAST_MODIFIED = "Last-Modified";


    void executeRequest(CrawlerRequest crawlerRequest);

    void close();
}
