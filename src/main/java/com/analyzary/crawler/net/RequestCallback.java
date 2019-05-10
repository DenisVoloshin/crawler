package com.analyzary.crawler.net;

import java.io.IOException;
import java.util.Map;


/**
 * Interface defines http call back handler for {@link com.analyzary.crawler.net.request.BaseCrawlerRequest}
 */
public interface RequestCallback {

    void onFailure(Exception e);

    void onFailure(String error, int code);

    void onResponse(byte[] data, Map<String, String> headers, int code) throws IOException;
}
