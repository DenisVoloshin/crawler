package com.analyzary.crawler.cache;

import com.google.gson.Gson;

import javax.annotation.Nullable;
import java.util.List;

public class CrawlerCacheEntry {

    private String lastModificationDate;
    private int depth;
    @Nullable
    private byte[] data;
    private List<String> links;
    private String url;


    public CrawlerCacheEntry(String url, String lastModificationDate, List<String> links) {
        this.lastModificationDate = lastModificationDate;
        this.links = links;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Nullable
    public byte[] getData() {
        return data;
    }

    public void setData(@Nullable byte[] data) {
        this.data = data;
    }

    public String getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(String lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> urls) {
        this.links = urls;
    }

    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(new CrawlerCacheEntryJSON(lastModificationDate, depth, links, url));
    }

    private static class CrawlerCacheEntryJSON {
        private String lastModificationDate;
        private int depth;
        private List<String> links;
        private String url;

        public CrawlerCacheEntryJSON(String lastModificationDate, int depth, List<String> links, String url) {
            this.lastModificationDate = lastModificationDate;
            this.depth = depth;
            this.links = links;
            this.url = url;
        }

        public String getLastModificationDate() {
            return lastModificationDate;
        }

        public void setLastModificationDate(String lastModificationDate) {
            this.lastModificationDate = lastModificationDate;
        }

        public int getDepth() {
            return depth;
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }

        public List<String> getLinks() {
            return links;
        }

        public void setLinks(List<String> links) {
            this.links = links;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
