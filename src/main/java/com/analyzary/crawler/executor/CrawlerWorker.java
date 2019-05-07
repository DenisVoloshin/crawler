package com.analyzary.crawler.executor;

import com.analyzary.crawler.analyse.HTMLPageAnalyser;
import com.analyzary.crawler.cache.CrawlerCache;
import com.analyzary.crawler.monitor.CrawlerMonitor;
import com.analyzary.crawler.storage.CrawlerDAO;
import com.analyzary.crawler.storage.HtmlPageMetaData;
import com.analyzary.crawler.net.Connector;
import com.analyzary.crawler.net.OkHttpConnector;
import com.analyzary.crawler.net.RequestCallback;
import com.analyzary.crawler.net.request.BaseCrawlerRequest;
import com.analyzary.crawler.net.request.CrawlerRequest;
import com.analyzary.crawler.queue.Queue;
import com.analyzary.crawler.queue.QueueElement;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CrawlerWorker implements Runnable {

    private static Logger logger = Logger.getLogger(CrawlerWorker.class.getName());
    private Connector connector;
    private Queue<QueueElement> queue;
    private String url;
    private HTMLPageAnalyser htmlPageAnalyser;
    private CrawlerDAO crawlerDAO;
    private CrawlerCache<String, HtmlPageMetaData> crawlerCache;
    private int depth;
    private int maxDepth;

    public CrawlerWorker(Connector connector, Queue<QueueElement> queue, String url, int depth, int maxDepth,
                         HTMLPageAnalyser htmlPageAnalyser, CrawlerCache crawlerCache, CrawlerDAO crawlerDAO) {
        this.connector = connector;
        this.queue = queue;
        this.url = url;
        this.depth = depth;
        this.maxDepth = maxDepth;
        this.htmlPageAnalyser = htmlPageAnalyser;
        this.crawlerCache = crawlerCache;
        this.crawlerDAO = crawlerDAO;
    }

    @Override
    public void run() {

        CrawlerMonitor.getInstance().incrementTotalProcessedPages();

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        HtmlPageMetaData htmlPageMetaData = crawlerCache.get(url);
        String modificationDate = htmlPageMetaData == null ? "" : htmlPageMetaData.getLastModificationDate();

        if (htmlPageMetaData != null) {
            if (htmlPageMetaData.getResponseCode() == Connector.OK_200 || htmlPageMetaData.getResponseCode() == Connector.NOT_MODIFIED_304) {
                if (!completed) {
                    if ((depth + 1) < maxDepth) {
                        htmlPageMetaData.getLinks().stream().forEach(url ->
                                queue.push(new QueueElement(url, depth + 1)));
                    }
                }
            }
        }

        if (completed || htmlPageMetaData == null) {

            BaseCrawlerRequest request = new BaseCrawlerRequest(url, modificationDate.isEmpty() ? CrawlerRequest.Method.GET :
                    CrawlerRequest.Method.HEAD,
                    OkHttpConnector.createIdModifiedSinceHeader(modificationDate), new RequestCallback() {
                @Override
                public void onFailure(Exception e) {
                    countDownLatch.countDown();
                    HtmlPageMetaData htmlPageMetaData = new HtmlPageMetaData(url, depth, "", 500, Collections.emptyList());
                    crawlerCache.put(url, htmlPageMetaData);

                    CrawlerMonitor.getInstance().incrementProcessedPagesWithError();
                    logger.severe(url + " " + e.getMessage());
                }

                @Override
                public void onFailure(String message, int code) {
                    countDownLatch.countDown();
                    HtmlPageMetaData htmlPageMetaData = new HtmlPageMetaData(url, depth, "", code, Collections.emptyList());
                    crawlerCache.put(url, htmlPageMetaData);
                    CrawlerMonitor.getInstance().incrementProcessedPagesWithError();
                    logger.severe(url + " " + message);
                }

                @Override
                public void onResponse(byte[] data, Map<String, String> headers, int code) {

                    CrawlerMonitor.getInstance().incrementSuccessfullyProcessedPages();

                    if (code == OkHttpConnector.NOT_MODIFIED_304) {
                        //logger.info("Page  [" + url + "] loaded from cache");

                        if (depth < maxDepth) {
                            htmlPageMetaData.getLinks().stream().forEach(url ->
                                    queue.push(new QueueElement(url, depth + 1)));
                        }
                    } else {
                        if (headers.get(OkHttpConnector.CONTENT_TYPE) != null && headers.get(OkHttpConnector.CONTENT_TYPE).contains(OkHttpConnector.PLAIN_TEXT)) {

                            List<String> internalUrls = htmlPageAnalyser.analysePage(data, url);

                            if (depth < maxDepth) {
                                internalUrls.stream().forEach(url ->
                                        queue.push(new QueueElement(url, depth + 1)));
                            }


                            String lastModificationDate = "";
                            if (headers.get(OkHttpConnector.LAST_MODIFIED) != null) {
                                lastModificationDate = headers.get(OkHttpConnector.LAST_MODIFIED);
                            }

                            HtmlPageMetaData htmlPageMetaData = new HtmlPageMetaData(url, depth, lastModificationDate, code, internalUrls);
                            htmlPageMetaData.setData(data);
                            crawlerDAO.insertHtmlPageMetaData(url, htmlPageMetaData);
                            crawlerCache.put(url, htmlPageMetaData);

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
        queue.push(new QueueElement(null, 0));
        synchronized (queue) {
            queue.notifyAll();
        }
    }
}
