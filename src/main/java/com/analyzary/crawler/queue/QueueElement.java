package com.analyzary.crawler.queue;

public class QueueElement {
    private String page;
    private int depth;

    public QueueElement(String page, int depth) {
        this.page = page;
        this.depth = depth;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
