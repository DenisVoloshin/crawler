package com.analyzary.crawler.model;

import com.google.gson.Gson;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents html meda-data model,
 * the element has JSON format which is easy to store in document based DB like MonogoDB
 */
public class HtmlPageMetaData {

    private String lastModificationDate;
    private Integer[] depths;
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
        this.depths = htmlPageMetaDataJSON.depths;
        this.responseCode = htmlPageMetaDataJSON.responseCode;
    }

    public HtmlPageMetaData(String url, int depth, String lastModificationDate, int responseCode, List<String> links) {
        this.lastModificationDate = lastModificationDate;
        this.links = links;
        this.url = url;
        this.responseCode = responseCode;
        this.depths = new Integer[1];
        this.depths[0] = depth;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public Integer[] getDepths() {
        return depths;
    }

    public void setDepths(Integer[] depths) {
        this.depths = depths;
    }

    public void addDepth(int depth) {
        synchronized (this.depths) {
            ArrayList<Integer> depthsList = new ArrayList(Arrays.asList(this.depths));
            if (!depthsList.contains(depth)) {
                depthsList.add(depth);
            }
            this.depths = depthsList.toArray(new Integer[0]);
        }
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> urls) {
        this.links = urls;
    }

    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(new HtmlPageMetaDataJSON(lastModificationDate, depths, responseCode, links, url));
    }

    public String fromJSON(String json) {
        return jsonManager.toJson(new HtmlPageMetaDataJSON(lastModificationDate, depths, responseCode, links, url));
    }

    private static class HtmlPageMetaDataJSON {
        private String lastModificationDate;
        private Integer[] depths;
        private List<String> links;
        private String url;
        private int responseCode;

        public HtmlPageMetaDataJSON(String lastModificationDate, Integer[] depths, int responseCode, List<String> links, String url) {
            this.lastModificationDate = lastModificationDate;
            this.depths = depths;
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

        public Integer[] getDepth() {
            return depths;
        }

        public void setDepth(Integer[] depth) {
            this.depths = depth;
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
