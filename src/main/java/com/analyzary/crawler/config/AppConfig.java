package com.analyzary.crawler.config;

public class AppConfig {
    private String rootUrl;
    private int depth;
    private String dbRootFolder;

    public String getRootUrl() {
        return rootUrl;
    }

    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getDbRootFolder() {
        return dbRootFolder;

    }

    public void setDbRootFolder(String dbRootFolder) {
        this.dbRootFolder = dbRootFolder;
    }
}
