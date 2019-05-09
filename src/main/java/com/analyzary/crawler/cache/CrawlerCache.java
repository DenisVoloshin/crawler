package com.analyzary.crawler.cache;

import java.util.Collection;


/**
 *  Generic cache interface for crawler internal usage
 */
public interface CrawlerCache<K, V> {
    void put(K key, V value);
    V get(K key);
    Collection<V> getAllElements();
}
