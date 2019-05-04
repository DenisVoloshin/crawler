package com.analyzary.crawler.cache;


import com.analyzary.crawler.analyse.HTMLPageAnalyser;
import com.analyzary.crawler.config.ConfigurationManager;
import com.analyzary.crawler.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PersistentCache implements CrawlerCache<String, CrawlerCacheEntry> {

    private ConcurrentHashMap<String, CrawlerCacheEntry> cache;
    private ConfigurationManager configurationManager;
    private HTMLPageAnalyser htmlPageAnalyser;


    public PersistentCache(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
        this.cache = new ConcurrentHashMap();
        this.htmlPageAnalyser = new HTMLPageAnalyser();
        load();
    }

    @Override
    public void put(String key, CrawlerCacheEntry value) {
        cache.put(key, value);
        new Thread(() -> persistCachedEntry(value)).start();
    }


    private void persistCachedEntry(CrawlerCacheEntry value) {
        String entryPath = FileUtils.filePathToHash(value.getUrl());

        try {
            FileUtils.writeFile(value.getData(), configurationManager.getCachePath() + File.separator + entryPath + ".html");
            FileUtils.writeFile(value.toJSON().getBytes(), configurationManager.getCachePath() + File.separator + entryPath + "_META_DATA.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public CrawlerCacheEntry get(String key) {
        return cache.get(key);
    }

    @Override
    public void load() {
        File cachePath = new File(configurationManager.getCachePath());
        if (!cachePath.exists()) {
            cachePath.mkdir();
        } else {
            Arrays.stream(cachePath.listFiles()).forEach(file -> {
                file.delete();
            });
        }

//        Arrays.stream(cachePath.listFiles()).forEach(file -> {
//            String url = file.getName().split("_")[0];
//            String modificationDate = file.getName().split("_")[1];
//            try {
//                String decodedUrl = FileUtils.filePathToHash(url);
//                List<String> links = this.htmlPageAnalyser.analysePage(FileUtils.readFile(file.getAbsolutePath()).getBytes(),
//                        decodedUrl);
//
//                cache.put(decodedUrl, new CrawlerCacheEntry("", modificationDate, links));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
    }

    @Override
    public void store() {

    }
}
