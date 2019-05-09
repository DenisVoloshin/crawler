package com.analyzary.crawler.net;

import com.analyzary.crawler.net.request.CrawlerRequest;
import okhttp3.*;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class OkHttpConnector implements Connector {

    private static OkHttpClient client;

    static {
        client = new OkHttpClient.Builder()
                .followRedirects(true)
                .followSslRedirects(true)
                .cache(null)
                .connectionPool(new ConnectionPool(50, 10, TimeUnit.SECONDS))
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(0, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS).build();
    }


    @Override
    public void executeRequest(CrawlerRequest crawlerRequest) {
        Request.Builder builder = new Request.Builder();
        builder.url(crawlerRequest.getUrl());

        //add headers
        for (Map.Entry<String, String> header : crawlerRequest.getHeaders().entrySet()) {
            builder.addHeader(header.getKey(), header.getValue());
        }

        if (crawlerRequest.getMethod() == CrawlerRequest.Method.GET) {
            Request request = builder.build();
            execute(request, crawlerRequest.getCallback());
        }
        if (crawlerRequest.getMethod() == CrawlerRequest.Method.HEAD) {
            Request request = builder.head().build();
            execute(request, crawlerRequest.getCallback());
        }
    }

    @Override
    public void close() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }


    public void execute(Request request, RequestCallback requestCallback) {

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                requestCallback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if (response.code() != OK_200 && response.code() != NOT_MODIFIED_304) {
                    requestCallback.onFailure(response.message(), response.code());
                    response.close();
                } else {
                    Map<String, String> headers =
                            response.headers().names().stream().collect(
                                    Collectors.toMap(name -> name, response::header));

                    byte[] data = new byte[0];

                    if (response.code() != NOT_MODIFIED_304 && request.method().equals(CrawlerRequest.Method.GET.toString())) {
                        data = response.body().bytes();
                    }

                    response.close();

                    requestCallback.onResponse(
                            data,
                            headers,
                            response.code());

                }
            }
        });
    }

    public static Hashtable<String, String> createIdModifiedSinceHeader(String lastModifiedData) {
        Hashtable<String, String> headers = new Hashtable<>();
        if (lastModifiedData != null && !lastModifiedData.trim().equals("")) {
            headers.put(IF_MODIFIED_SINCE, lastModifiedData);
        }
        return headers;
    }

}
