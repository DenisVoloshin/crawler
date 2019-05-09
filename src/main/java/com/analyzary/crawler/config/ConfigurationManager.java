package com.analyzary.crawler.config;


public class ConfigurationManager {

    private static String DEFAULT_UUID = "d626f506-715f-11e9-a923-1681be663d3e";
    private static String UUID_PARAM = "UUID_PARAM";

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

    public String  getCrawlingId() {
       return System.getenv(UUID_PARAM) == null ? DEFAULT_UUID : System.getenv(UUID_PARAM);
    }

    public String getDBRootFolder() {
        return this.appConfig.getDbRootFolder();
    }

    public String toString() {
        String configAsString = "Configuration:\n" +
                "rootUrl:\t" + this.appConfig.getRootUrl() + "\n" +
                "depth:\t" + this.appConfig.getDepth() + "\n" +
                "db path:\t" + this.appConfig.getDbRootFolder() + "\n"+
                "crawler instance id:\t" + getCrawlingId() + "\n";

        return configAsString;
    }
}
