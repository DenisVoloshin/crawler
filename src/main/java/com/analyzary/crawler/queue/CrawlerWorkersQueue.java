package com.analyzary.crawler.queue;


import javax.annotation.CheckForNull;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Concrete Crawler Queue in-memory implementation.
 */
public class CrawlerWorkersQueue implements Queue<QueueElement> {

    ConcurrentLinkedQueue<QueueElement> queue;

    public CrawlerWorkersQueue(){
        queue = new ConcurrentLinkedQueue();
    }

    @Override
    public void push(QueueElement queueElement) {
        queue.add(queueElement);
    }

    public int getSize(){
        return queue.size();
    }

    @Override
    @CheckForNull
    public QueueElement poll() {
        return queue.poll();
    }
}
