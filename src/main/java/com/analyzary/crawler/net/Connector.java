package com.analyzary.crawler.net;

import com.analyzary.crawler.net.request.CrawlerRequest;

import java.util.Map;

public interface Connector {
    public static final int OK_200 = 200;
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String PLAIN_TEXT = "text/html";
    public static final int NOT_MODIFIED_304 = 304;
    public final static String IF_MODIFIED_SINCE = "If-Modified-Since";
    public final static String LAST_MODIFIED = "Last-Modified";

    void executeRequest(CrawlerRequest crawlerRequest);
}
