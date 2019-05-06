package com.analyzary.crawler.storage;

import com.analyzary.crawler.config.ConfigurationManager;

import java.util.List;

public class CrawlerDAO {

    private static final String DB_NAME = "CRAWER_DB";
    private static final String HTML_PAGES = "HTML_PAGES";
    private static final String HTML_PAGES_META_DATA = "HTML_PAGES_META_DATA";

    private static CrawlerDB crawlerDB;

    public static class SingletonHolder {
        public static final CrawlerDAO HOLDER_INSTANCE = new CrawlerDAO();
    }

    public static CrawlerDAO getInstance() {
        return CrawlerDAO.SingletonHolder.HOLDER_INSTANCE;
    }


    private CrawlerDAO() {
        CrawlerDBClient crawlerDBClient = new FileSystemCrawlerDBClient(ConfigurationManager.getInstance());
        crawlerDB = crawlerDBClient.getDB(DB_NAME);
        crawlerDB.createCollection(HTML_PAGES);
        crawlerDB.createCollection(HTML_PAGES_META_DATA);
    }


    public static boolean insertHtmlPage(String url, String pageData) {
        CrawlerDBCollection collection = crawlerDB.getCollection(HTML_PAGES);
        collection.insertElement(url, pageData);
        return true;
    }

    public static boolean insertHtmlPageMetaData(String url, String pageMetaData) {
        CrawlerDBCollection collection = crawlerDB.getCollection(HTML_PAGES_META_DATA);
        collection.insertElement(url, pageMetaData);
        return true;
    }


    public static List<HtmlPageMetaData> getAllHtmlPageMetaDataElements() {
       return null;
    }
}
