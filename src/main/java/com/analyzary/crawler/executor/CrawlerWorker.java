package com.analyzary.crawler.executor;

import com.analyzary.crawler.analyse.HTMLPageAnalyser;
import com.analyzary.crawler.cache.CrawlerCacheEntry;
import com.analyzary.crawler.cache.PersistentCache;
import com.analyzary.crawler.net.OkHttpConnector;
import com.analyzary.crawler.net.RequestCallback;
import com.analyzary.crawler.net.request.BaseCrawlerRequest;
import com.analyzary.crawler.net.request.CrawlerRequest;
import com.analyzary.crawler.queue.Queue;
import com.analyzary.crawler.queue.QueueElement;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CrawlerWorker implements Runnable {


    private static Logger logger = Logger.getLogger(CrawlerWorker.class.getName());

    private OkHttpConnector connector;
    private Queue<QueueElement> queue;
    private String url;
    private HTMLPageAnalyser htmlPageAnalyser;
    private PersistentCache crawlerCache;
    private int depth;
    private int maxDepth;

    public CrawlerWorker(OkHttpConnector connector, Queue<QueueElement> queue, String url, int depth, int maxDepth,
                         HTMLPageAnalyser htmlPageAnalyser, PersistentCache crawlerCache) {
        this.connector = connector;
        this.queue = queue;
        this.url = url;
        this.depth = depth;
        this.maxDepth = maxDepth;
        this.htmlPageAnalyser = htmlPageAnalyser;
        this.crawlerCache = crawlerCache;
    }

    @Override
    public void run() {

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        CrawlerCacheEntry crawlerCacheEntry = crawlerCache.get(url);
        String modificationDate = crawlerCacheEntry == null ? "" : crawlerCacheEntry.getLastModificationDate();


        BaseCrawlerRequest request = new BaseCrawlerRequest(url, CrawlerRequest.Method.GET,
                OkHttpConnector.createIdModifiedSinceHeader(modificationDate), new RequestCallback() {
            @Override
            public void onFailure(Exception e) {
                countDownLatch.countDown();
                logger.severe(url + " " + e.getMessage());
            }

            @Override
            public void onFailure(String message, int code) {
                countDownLatch.countDown();
                logger.severe(url + " " + message);
            }

            @Override
            public void onResponse(byte[] data, Map<String, String> headers, int code) {
                if (code == OkHttpConnector.NOT_MODIFIED_304) {
                    logger.fine("Page  [" + url + "] loaded from cache");

                    if ((depth + 1) < maxDepth) {
                        crawlerCacheEntry.getLinks().stream().forEach(url ->
                                queue.push(new QueueElement(url, depth + 1)));
                    }
                } else {
                    if (headers.get(OkHttpConnector.CONTENT_TYPE) != null && headers.get(OkHttpConnector.CONTENT_TYPE).contains(OkHttpConnector.PLAIN_TEXT)) {

                        List<String> internalUrls = htmlPageAnalyser.analysePage(data, url);

                        if ((depth + 1) < maxDepth) {
                            internalUrls.stream().forEach(url ->
                                    queue.push(new QueueElement(url, depth + 1)));
                        }

                        synchronized (queue) {
                            queue.notifyAll();
                        }

                        String lastModoficationDate = "";
                        if (headers.get(OkHttpConnector.LAST_MODIFIED) != null) {
                            lastModoficationDate = headers.get(OkHttpConnector.LAST_MODIFIED);
                        }

                        CrawlerCacheEntry crawlerCacheEntry = new CrawlerCacheEntry(url, lastModoficationDate, internalUrls);
                        crawlerCacheEntry.setData(data);
                        crawlerCache.put(url, crawlerCacheEntry);
                    } else {
                        logger.fine("Page  [" + url + "] is not " + OkHttpConnector.CONTENT_TYPE);
                    }
                }
                countDownLatch.countDown();
            }
        });

        connector.executeRequest(request);

        try {
            countDownLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.severe(e.getMessage());
        }
    }
}
