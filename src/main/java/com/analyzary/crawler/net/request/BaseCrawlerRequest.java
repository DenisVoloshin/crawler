package com.analyzary.crawler.net.request;

import com.analyzary.crawler.net.RequestCallback;

import java.util.Map;

public class BaseCrawlerRequest implements CrawlerRequest {

    private Method method;
    private String url;
    private Map<String, String> headers;
    private RequestCallback callback;

    public BaseCrawlerRequest(String url, Method method, Map<String, String> headers, RequestCallback callback) {
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.callback = callback;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public RequestCallback getCallback() {
        return callback;
    }
}
