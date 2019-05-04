package com.analyzary.crawler.net.request;

import com.analyzary.crawler.net.RequestCallback;

import java.util.Map;

public interface CrawlerRequest {

    String getUrl();

    Map<String, String> getHeaders();

    RequestCallback getCallback();

    Method getMethod();

    enum Method {
        GET,
        HEAD
    }
}
