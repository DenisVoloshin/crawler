package com.analyzary.crawler.queue;

public interface Queue<T> {
    void push(T message);
    T poll();
}
