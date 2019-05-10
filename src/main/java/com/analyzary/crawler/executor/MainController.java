package com.analyzary.crawler.executor;

import com.analyzary.crawler.analyse.HTMLPageAnalyser;
import com.analyzary.crawler.config.ConfigurationManager;
import com.analyzary.crawler.model.CrawlerState;
import com.analyzary.crawler.monitor.CrawlerMonitor;
import com.analyzary.crawler.net.Connector;
import com.analyzary.crawler.queue.CrawlerWorkersQueue;
import com.analyzary.crawler.queue.QueueElement;
import com.analyzary.crawler.storage.CrawlerDAO;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toList;


/**
 * The Crawler coordinator, initializes all Crawler components
 * 1. Queue holds links which should be processed
 * 2. Queue listeners, poll from queue available link for processing,
 *    creates a new Crawler worker {@link com.analyzary.crawler.executor.CrawlerWorker}
 *    and submits it into ThreadPoolExecutor, the ThreadPoolExecutor has a pool with predefined threads size.
 * 3. Each Crawler processes the given link and feeds the queue with all link from the next level.
 * 4. When the Queue is empty and ThreadPoolExecutor has no active or waiting worker
 *    the Crawler coordinator stops the process.
 */
public class MainController {

    private static Logger logger = Logger.getLogger(MainController.class.getName());
    private ThreadPoolExecutor workersExecutor;
    private ThreadPoolExecutor queueListenersExecutor;

    private int workersExecutorSize = 200;
    private int queueListenersExecutorSize = 1;

    private List<QueueListener> queueListeners;
    private CrawlerWorkersQueue crawlerWorkersQueue;

    private Connector connector;
    private String url;
    private HTMLPageAnalyser htmlPageAnalyser;
    private ConfigurationManager configurationManager;
    private CrawlerDAO crawlerDAO;
    private CrawlerState previousState;


    public MainController(ConfigurationManager configurationManager,
                          CrawlerWorkersQueue crawlerWorkersQueue,
                          Connector connector,
                          String url,
                          CrawlerState previousState,
                          HTMLPageAnalyser htmlPageAnalyser,
                          CrawlerDAO crawlerDAO) {


        logger.info("MainController started");
        this.configurationManager = configurationManager;
        this.crawlerWorkersQueue = crawlerWorkersQueue;
        this.connector = connector;
        this.url = url;
        this.previousState = previousState;
        this.htmlPageAnalyser = htmlPageAnalyser;
        this.crawlerDAO = crawlerDAO;

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

        logger.info("MainController stopped");
    }

    public void stop() {
        workersExecutor.shutdown();
        queueListenersExecutor.shutdown();

        try {
            workersExecutor.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }

        crawlerDAO.close();
        connector.close();
    }


    private CrawlerWorker createWorker(String url, int depth) {
        return new CrawlerWorker(connector, crawlerWorkersQueue, url, depth,
                configurationManager.getCrawlingDepth(), htmlPageAnalyser, previousState, crawlerDAO);
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
                    } catch (InterruptedException e) {
                        logger.severe(e.getMessage());
                    }
                }
                CrawlerMonitor.getInstance().setQueueSize(crawlerWorkersQueue.getSize());
                CrawlerMonitor.getInstance().setActiveCrawlerWorkers(workersExecutor.getActiveCount());
                CrawlerMonitor.getInstance().setWaitingCrawlerWorkers(workersExecutor.getQueue().size());

                if (queueElement.getDepth() < (configurationManager.getCrawlingDepth() + 1) && queueElement.getUrl() != null) {
                    //if (crawlerWorkersQueue.getSize() % 20 == 0) {
                    logger.fine("queueElement.getDepth()  [" + queueElement.getDepth() + "] queue size: "
                            + crawlerWorkersQueue.getSize() + " active threads:" + workersExecutor.getActiveCount() + " " +
                            "in queue:" + workersExecutor.getQueue().size());
                    // }
                    workersExecutor.submit(createWorker(queueElement.getUrl(), queueElement.getDepth()));
                } else {
                    if (crawlerWorkersQueue.getSize() == 0 &&
                            workersExecutor.getActiveCount() == 0 &&
                            workersExecutor.getQueue().size() == 0) {
                        gracefullStop();
                        MainController.this.stop();
                    }
                }
            }
        }

        public void gracefullStop() {
            shouldBeStopped = true;
        }
    }
}
