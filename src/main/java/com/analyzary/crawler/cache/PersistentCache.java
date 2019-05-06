package com.analyzary.crawler.cache;


import com.analyzary.crawler.analyse.HTMLPageAnalyser;
import com.analyzary.crawler.config.ConfigurationManager;
import com.analyzary.crawler.storage.HtmlPageMetaData;
import com.analyzary.crawler.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PersistentCache implements CrawlerCache<String, HtmlPageMetaData> {

    private ConcurrentHashMap<String, HtmlPageMetaData> cache;
    private ConfigurationManager configurationManager;
    private HTMLPageAnalyser htmlPageAnalyser;
    private ThreadPoolExecutor persistentWorkerExecutor;

    public PersistentCache(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
        this.cache = new ConcurrentHashMap();
        this.htmlPageAnalyser = new HTMLPageAnalyser();
        this.persistentWorkerExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
        load();
    }

    @Override
    public void put(String key, final HtmlPageMetaData value) {
        cache.put(key, value);
        this.persistentWorkerExecutor.submit(new Thread(() -> persistCachedEntry(value)));
    }


    private void persistCachedEntry(HtmlPageMetaData value) {
        String entryPath = FileUtils.filePathToHash(value.getUrl());

        try {
            if (value.getData() != null && value.getData().length > 0) {
                FileUtils.writeFile(value.getData(), configurationManager.getDBRootFolder() + File.separator + entryPath + ".html");
            }
            FileUtils.writeFile(value.toJSON().getBytes(), configurationManager.getDBRootFolder() + File.separator + entryPath + "_META_DATA.json");
        } catch (IOException e) {
            // TODO
        }

    }


    public Collection<HtmlPageMetaData> getMetaData(){
        return cache.values();
    }

    @Override
    public HtmlPageMetaData get(String key) {
        return cache.get(key);
    }

    @Override
    public void load() {
        File cachePath = new File(configurationManager.getDBRootFolder());
        if (!cachePath.exists()) {
            cachePath.mkdir();
        }

        Arrays.stream(cachePath.listFiles()).filter(file -> file.getName().contains("_META_DATA")).forEach(file -> {
            try {
                HtmlPageMetaData htmlPageMetaData = new HtmlPageMetaData(FileUtils.readFile(file.getAbsolutePath()));
                cache.put(htmlPageMetaData.getUrl(), htmlPageMetaData);
            } catch (Exception e) {
                // TODO
            }
        });
    }

    @Override
    public void store() {

    }

    @Override
    public void stop() {
        persistentWorkerExecutor.shutdown();
        try {
            if (!persistentWorkerExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                persistentWorkerExecutor.shutdownNow();
            }
        } catch (InterruptedException ex) {
            persistentWorkerExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
