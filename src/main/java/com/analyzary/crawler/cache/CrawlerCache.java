package com.analyzary.crawler.cache;

public interface CrawlerCache<K, V> {
    void put(K key, V value);
    V get(K key);
    void load();
    void store();
    void stop();
}
