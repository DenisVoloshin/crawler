package com.analyzary.crawler.executor;

import com.analyzary.crawler.analyse.HTMLPageAnalyser;
import com.analyzary.crawler.cache.PersistentCache;
import com.analyzary.crawler.config.ConfigurationManager;
import com.analyzary.crawler.net.OkHttpConnector;
import com.analyzary.crawler.queue.CrawlerWorkersQueue;
import com.analyzary.crawler.queue.QueueElement;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;

public class MainController {

    private static Logger logger = Logger.getLogger(MainController.class.getName());
    private ThreadPoolExecutor workersExecutor;
    private ThreadPoolExecutor queueListenersExecutor;

    private int workersExecutorSize = 200;
    private int queueListenersExecutorSize = 5;

    private List<QueueListener> queueListeners;
    private CrawlerWorkersQueue crawlerWorkersQueue;

    private OkHttpConnector connector;
    private String url;
    private HTMLPageAnalyser htmlPageAnalyser;
    private PersistentCache crawlerCache;
    private ConfigurationManager configurationManager;


    public MainController(ConfigurationManager configurationManager,
                          CrawlerWorkersQueue crawlerWorkersQueue,
                          OkHttpConnector connector,
                          String url,
                          HTMLPageAnalyser htmlPageAnalyser,
                          PersistentCache crawlerCache) {


        logger.info("MainController started");
        this.configurationManager = configurationManager;
        this.crawlerWorkersQueue = crawlerWorkersQueue;
        this.connector = connector;
        this.url = url;
        this.htmlPageAnalyser = htmlPageAnalyser;
        this.crawlerCache = crawlerCache;

        workersExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(workersExecutorSize);
        queueListenersExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(queueListenersExecutorSize);
    }

    public void execute() {
        // create queue listeners
        queueListeners = Collections.nCopies(queueListenersExecutorSize, null).stream()
                .map(o -> new QueueListener())
                .collect(toList());
        queueListeners.stream().forEach(queueListener -> queueListenersExecutor.submit(queueListener));

        workersExecutor.submit(createWorker(url, 1));

        try {
            workersExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }

        stop();
        logger.info("MainController stopped");
    }

    public void stop() {
        queueListeners.stream().forEach(queueListener -> queueListener.gracefullStop());
        synchronized (crawlerWorkersQueue) {
            crawlerWorkersQueue.notifyAll();
        }
        workersExecutor.shutdown();
        queueListenersExecutor.shutdown();
    }


    private CrawlerWorker createWorker(String url, int depth) {
        return new CrawlerWorker(connector, crawlerWorkersQueue, url, depth,
                configurationManager.getCrawlingDepth(), htmlPageAnalyser, crawlerCache);
    }

    private class QueueListener implements Runnable {

        private boolean shouldBeStopped = false;

        @Override
        public void run() {
            while (!shouldBeStopped) {
                QueueElement queueElement;
                while ((queueElement = crawlerWorkersQueue.poll()) == null) {
                    try {
                        synchronized (crawlerWorkersQueue) {
                            crawlerWorkersQueue.wait();
                        }

                        if (crawlerWorkersQueue.getSize() == 0 &&
                                workersExecutor.getQueue().size() == 0) {
                            stop();
                        }
                    } catch (InterruptedException e) {
                        logger.severe(e.getMessage());
                    }
                }

                if (queueElement.getDepth() < configurationManager.getCrawlingDepth()) {
                    logger.info("queueElement.getDepth()  [" + queueElement.getDepth() + "] queue size: "
                            + crawlerWorkersQueue.getSize() + " active threads:" + workersExecutor.getActiveCount() + " " +
                            "in queue:" + workersExecutor.getQueue().size());
                    workersExecutor.submit(createWorker(queueElement.getPage(), queueElement.getDepth()));
                } else {
                    if (crawlerWorkersQueue.getSize() == 0 &&
                            workersExecutor.getQueue().size() == 0) {
                        stop();
                    }
                }
            }
        }

        public void gracefullStop() {
            shouldBeStopped = true;
        }
    }
}
