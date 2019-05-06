package com.analyzary.crawler.config;


public class ConfigurationManager {

    private AppConfig appConfig;

    public static class SingletonHolder {
        public static final ConfigurationManager HOLDER_INSTANCE = new ConfigurationManager();
    }

    public static ConfigurationManager getInstance() {
        return SingletonHolder.HOLDER_INSTANCE;
    }

    public void loadAppConfiguration(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public String getRootPoint() {
        return this.appConfig.getRootUrl();
    }

    public int getCrawlingDepth() {
        return this.appConfig.getDepth();
    }

    public String getDBRootFolder() {
        return this.appConfig.getDbRootFolder();
    }

    public String toString() {
        String configAsString = "Configuration:\n" +
                "rootUrl:" + this.appConfig.getRootUrl() + "\n" +
                "depth:" + this.appConfig.getDepth() + "\n" +
                "cache path:" + this.appConfig.getDbRootFolder() + "\n";

        return configAsString;
    }
}
