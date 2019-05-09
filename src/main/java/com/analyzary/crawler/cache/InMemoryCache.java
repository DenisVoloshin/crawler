package com.analyzary.crawler.cache;

import com.analyzary.crawler.config.ConfigurationManager;
import com.analyzary.crawler.model.HtmlPageMetaData;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCache implements CrawlerCache<String, HtmlPageMetaData> {

    private ConcurrentHashMap<String, HtmlPageMetaData> cache;
    private ConfigurationManager configurationManager;


    public InMemoryCache(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
        this.cache = new ConcurrentHashMap();
    }

    @Override
    public void put(String key, final HtmlPageMetaData value) {
        cache.put(key, value);
    }

    @Override
    public HtmlPageMetaData get(String key) {
        return cache.get(key);
    }

    @Override
    public Collection<HtmlPageMetaData> getAllElements() {
        return cache.values();
    }
}
