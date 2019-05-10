package com.analyzary.crawler.queue;

import javax.annotation.Nullable;


/**
 * Defines {@link com.analyzary.crawler.queue.CrawlerWorkersQueue} message model
 */
public class QueueElement {
    @Nullable
    private String url;

    private int depth;

    public QueueElement(String url, int depth) {
        this.url = url;
        this.depth = depth;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String page) {
        this.url = page;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
