package com.analyzary.crawler.monitor;

import java.util.Hashtable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * The class is used for internal purposes to aggregate the runtime metrics and show the on the fly.
 */
public class CrawlerMonitor {

    private volatile int queueSize;
    private volatile int activeCrawlerWorkers;
    private volatile int waitingCrawlerWorkers;
    private volatile int totalProcessedPages;
    private volatile int successfullyProcessedPages;
    private volatile int processedPagesWithError;
    private volatile long elapsedTime;
    private volatile long notModifiedPages;
    private volatile long skippedPages;
    private volatile long reDownloadedPages;
    public static Hashtable<String, Integer> duplications = new Hashtable<>();


    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> monitorPinger;

    public static class SingletonHolder {
        public static final CrawlerMonitor HOLDER_INSTANCE = new CrawlerMonitor();
    }

    public static CrawlerMonitor getInstance() {
        return CrawlerMonitor.SingletonHolder.HOLDER_INSTANCE;
    }

    private void printMonitorStatus() {
        System.out.println(getFormattedMonitorStatusAsString());
    }

    public void addProcessedPage(String url) {
        synchronized (duplications) {
            if (duplications.get(url) != null) {
                Integer pageCount = duplications.get(url);
                duplications.put(url, ++pageCount);
            } else {
                duplications.put(url, 1);
            }
        }
    }

    public void start() {
        elapsedTime = System.currentTimeMillis();
        final Runnable beeper = new Runnable() {
            public void run() {
                printMonitorStatus();
            }
        };
        monitorPinger =
                scheduler.scheduleAtFixedRate(beeper, 2, 5, TimeUnit.SECONDS);
    }

    public void stop() {
        printMonitorStatus();
        monitorPinger.cancel(true);
    }

    public String getFormattedMonitorStatusAsString() {
        StringBuilder monitorState = new StringBuilder();
        monitorState.append("Queue Size:\t" + queueSize + "\n");
        monitorState.append("Active Crawler Workers:\t" + activeCrawlerWorkers + "\n");
        monitorState.append("Waiting Crawler Workers:\t" + waitingCrawlerWorkers + "\n");
        monitorState.append("Total Processed Pages:\t" + totalProcessedPages + "\n");
        monitorState.append("Successfully Processed Pages:\t" + successfullyProcessedPages + "\n");
        monitorState.append("Not Modified Pages:\t" + notModifiedPages + "\n");
        monitorState.append("ReDownloaded Pages:\t" + reDownloadedPages + "\n");
        monitorState.append("Skipped Pages:\t" + skippedPages + "\n");
        monitorState.append("Processed Pages with Error:\t" + processedPagesWithError + "\n");
        monitorState.append("Elapsed Time:\t" + (System.currentTimeMillis() - elapsedTime) + "\n");
        monitorState.append("\n");
        return monitorState.toString();
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public void setActiveCrawlerWorkers(int activeCrawlerWorkers) {
        this.activeCrawlerWorkers = activeCrawlerWorkers;
    }

    public void setWaitingCrawlerWorkers(int waitingCrawlerWorkers) {
        this.waitingCrawlerWorkers = waitingCrawlerWorkers;
    }

    public void incrementTotalProcessedPages() {
        this.totalProcessedPages++;
    }

    public void incrementSuccessfullyProcessedPages() {
        this.successfullyProcessedPages++;
    }

    public void incrementProcessedPagesWithError() {
        this.processedPagesWithError++;
    }

    public void incrementNoModifiedPages() {
        this.notModifiedPages++;
    }

    public void incrementSkippedPages() {
        this.skippedPages++;
    }

    public void incrementReDownloadedPages() {
        this.reDownloadedPages++;
    }
}
