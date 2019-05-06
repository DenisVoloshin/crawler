package com.analyzary.crawler.storage;

import com.google.gson.Gson;

import javax.annotation.Nullable;
import java.util.List;

public class HtmlPageMetaData {

    private String lastModificationDate;
    private int depth;
    @Nullable
    private byte[] data;
    private List<String> links;
    private String url;
    private int responseCode;

    // There is nothing in the GSON instance that makes it related to a specific instance.
    private static Gson jsonManager = new Gson();


    public HtmlPageMetaData(String json) {
        HtmlPageMetaDataJSON htmlPageMetaDataJSON = jsonManager.fromJson(json, HtmlPageMetaDataJSON.class);
        this.lastModificationDate = htmlPageMetaDataJSON.lastModificationDate;
        this.links = htmlPageMetaDataJSON.links;
        this.url = htmlPageMetaDataJSON.url;
        this.depth = htmlPageMetaDataJSON.depth;
        this.responseCode = htmlPageMetaDataJSON.responseCode;
    }

    public HtmlPageMetaData(String url, int depth, String lastModificationDate, int responseCode, List<String> links) {
        this.lastModificationDate = lastModificationDate;
        this.links = links;
        this.url = url;
        this.responseCode = responseCode;
        this.depth = depth;
    }

    public String getUrl() {
        return url;
    }

    public int getResponseCode() {
        return responseCode;
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
        return gson.toJson(new HtmlPageMetaDataJSON(lastModificationDate, depth, responseCode, links, url));
    }

    public String fromJSON(String json) {
        return jsonManager.toJson(new HtmlPageMetaDataJSON(lastModificationDate, depth, responseCode, links, url));
    }

    private static class HtmlPageMetaDataJSON {
        private String lastModificationDate;
        private int depth;
        private List<String> links;
        private String url;
        private int responseCode;

        public HtmlPageMetaDataJSON(String lastModificationDate, int depth, int responseCode, List<String> links, String url) {
            this.lastModificationDate = lastModificationDate;
            this.depth = depth;
            this.links = links;
            this.url = url;
            this.responseCode = responseCode;
        }

        public String getLastModificationDate() {
            return lastModificationDate;
        }

        public void setLastModificationDate(String lastModificationDate) {
            this.lastModificationDate = lastModificationDate;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
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
