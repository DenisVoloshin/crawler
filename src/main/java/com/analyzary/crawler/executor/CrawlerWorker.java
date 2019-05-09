package com.analyzary.crawler.executor;

import com.analyzary.crawler.analyse.HTMLPageAnalyser;
import com.analyzary.crawler.monitor.CrawlerMonitor;
import com.analyzary.crawler.storage.CrawlerDAO;
import com.analyzary.crawler.model.CrawlerState;
import com.analyzary.crawler.model.HtmlPageMetaData;
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
    private int depth;
    private int maxDepth;
    private CrawlerState currentState;

    public CrawlerWorker(Connector connector, Queue<QueueElement> queue, String url, int depth, int maxDepth,
                         HTMLPageAnalyser htmlPageAnalyser, CrawlerState previousState, CrawlerDAO crawlerDAO) {
        this.connector = connector;
        this.queue = queue;
        this.url = url;
        this.currentState = previousState;
        this.depth = depth;
        this.maxDepth = maxDepth;
        this.htmlPageAnalyser = htmlPageAnalyser;
        this.crawlerDAO = crawlerDAO;

    }

    @Override
    public void run() {


        CrawlerMonitor.getInstance().incrementTotalProcessedPages();

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        HtmlPageMetaData htmlPageMetaData = CrawlerDAO.getInstance().getHtmlPageMetaData(url);

        String modificationDate = htmlPageMetaData == null ? "" : htmlPageMetaData.getLastModificationDate();


        if (htmlPageMetaData != null) {
            // the page already processed before
            if (htmlPageMetaData.getResponseCode() == Connector.OK_200 || htmlPageMetaData.getResponseCode() == Connector.NOT_MODIFIED_304) {
                if (currentState.getState() != CrawlerState.State.COMPLETE) {
                    CrawlerMonitor.getInstance().incrementSuccessfullyProcessedPages();
                    CrawlerMonitor.getInstance().incrementSkippedPages();
                    htmlPageMetaData.addDepth(depth);
                    crawlerDAO.insertHtmlPageMetaData(url, htmlPageMetaData);

                    if (currentState.getState() == CrawlerState.State.RECOVERY) {
                        if (depth < maxDepth) {
                            htmlPageMetaData.getLinks().stream().forEach(url ->
                                    queue.push(new QueueElement(url, depth + 1)));
                        }
                    }
                }
            } else if (depth != 1) {
                CrawlerMonitor.getInstance().incrementSkippedPages();
                queue.push(new QueueElement(null, 0));
                return;
            }
        }

        // the page never was processed or has to be updated.
        if (currentState.getState() == CrawlerState.State.COMPLETE || htmlPageMetaData == null) {

            final CrawlerRequest.Method method = modificationDate.isEmpty() ? CrawlerRequest.Method.GET :
                    CrawlerRequest.Method.HEAD;

            final BaseCrawlerRequest request = new BaseCrawlerRequest(url, method,
                    OkHttpConnector.createIdModifiedSinceHeader(modificationDate), new RequestCallback() {
                @Override
                public void onFailure(Exception e) {
                    CrawlerMonitor.getInstance().incrementProcessedPagesWithError();
                    HtmlPageMetaData htmlPageMetaData = new HtmlPageMetaData(url, depth, "", 500, Collections.emptyList());
                    crawlerDAO.insertHtmlPageMetaData(url, htmlPageMetaData);
                    logger.severe(url + " " + e.getMessage());
                    countDownLatch.countDown();
                }

                @Override
                public void onFailure(String message, int code) {
                    CrawlerMonitor.getInstance().incrementProcessedPagesWithError();
                    HtmlPageMetaData htmlPageMetaData = new HtmlPageMetaData(url, depth, "", code, Collections.emptyList());
                    crawlerDAO.insertHtmlPageMetaData(url, htmlPageMetaData);
                    logger.severe(url + " " + message);
                    countDownLatch.countDown();
                }

                @Override
                public void onResponse(byte[] data, Map<String, String> headers, int code) {

                    CrawlerMonitor.getInstance().incrementSuccessfullyProcessedPages();

                    if (code == OkHttpConnector.NOT_MODIFIED_304) {
                        CrawlerMonitor.getInstance().incrementNoModifiedPage();
                        if (depth < maxDepth) {
                            htmlPageMetaData.getLinks().stream().forEach(url ->
                                    queue.push(new QueueElement(url, depth + 1)));
                        }
                    } else {
                        if (headers.get(OkHttpConnector.CONTENT_TYPE) != null && headers.get(OkHttpConnector.CONTENT_TYPE).contains(OkHttpConnector.PLAIN_TEXT)) {
                            List<String> internalUrls = htmlPageAnalyser.extractHtmlLinks(data, url);

                            if (depth < maxDepth) {
                                internalUrls.stream().forEach(url ->
                                        queue.push(new QueueElement(url, depth + 1)));
                            }

                            String lastModificationDate = "";
                            if (headers.get(OkHttpConnector.LAST_MODIFIED) != null) {
                                lastModificationDate = headers.get(OkHttpConnector.LAST_MODIFIED);
                            }


                            if (htmlPageMetaData == null) {
                                HtmlPageMetaData newHtmlPageMetaData = new HtmlPageMetaData(url, depth, lastModificationDate, code, internalUrls);
                                newHtmlPageMetaData.setData(data);
                                crawlerDAO.insertHtmlPageMetaData(url, newHtmlPageMetaData);
                            } else {
                                if(data.length>0 && currentState.getState() == CrawlerState.State.COMPLETE){
                                    CrawlerMonitor.getInstance().incrementReDownloadedPages();
                                }
                                htmlPageMetaData.setData(data);
                                htmlPageMetaData.setLinks(internalUrls);
                                htmlPageMetaData.setLastModificationDate(lastModificationDate);
                                htmlPageMetaData.addDepth(depth);
                                crawlerDAO.insertHtmlPageMetaData(url, htmlPageMetaData);
                            }

                        } else {
                            logger.fine("Page  [" + url + "] is not " + OkHttpConnector.CONTENT_TYPE);
                        }
                    }
                    countDownLatch.countDown();
                }
            });

            connector.executeRequest(request);

            try {
                countDownLatch.await(20, TimeUnit.SECONDS);
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
