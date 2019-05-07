package com.analyzary.crawler.storage;

import com.analyzary.crawler.cache.PersistentCache;
import com.analyzary.crawler.config.ConfigurationManager;
import com.analyzary.crawler.util.FileUtils;

import javax.annotation.CheckForNull;
import java.util.Collection;

public class CrawlerDAO {

    private static final String DB_NAME = "CRAWLER_DB";
    private static final String HTML_PAGES = "HTML_PAGES";
    private static final String CRAWLER_STATE = "CRAWLER_STATE";
    private static final String HTML_PAGES_META_DATA = "HTML_PAGES_META_DATA";
    private static final String HTML_PAGE_META_DATA_SUFFIX = "_META_DATA.json";
    private static final String HTML_PAGE_DATA_SUFFIX = ".html";

    private CrawlerDB crawlerDB;
    private PersistentCache crawlerCache;


    public static class SingletonHolder {
        public static final CrawlerDAO HOLDER_INSTANCE = new CrawlerDAO();
    }

    public static CrawlerDAO getInstance() {
        return CrawlerDAO.SingletonHolder.HOLDER_INSTANCE;
    }


    private CrawlerDAO() {
        //this.crawlerCache = new PersistentCache(ConfigurationManager.getInstance());
        crawlerDB = new FileSystemCrawlerDBClient(ConfigurationManager.getInstance()).getDB(DB_NAME);
        crawlerDB.createCollection(HTML_PAGES);
        crawlerDB.createCollection(HTML_PAGES_META_DATA);
        crawlerDB.createCollection(CRAWLER_STATE);
    }


    public boolean insertHtmlPage(String url, String pageData) {
        String htmlPageId = FileUtils.filePathToHash(url);
        CrawlerDBCollection collection = crawlerDB.getCollection(HTML_PAGES);
        collection.insertElement(htmlPageId + HTML_PAGE_DATA_SUFFIX, pageData);
        return true;
    }

    @CheckForNull
    public HtmlPageMetaData getHtmlPageMetaData(String url) {
        return crawlerCache.get(url);
    }


    public boolean insertHtmlPageMetaData(String url, HtmlPageMetaData htmlPageMetaData) {
        String htmlPageMetaDataId = FileUtils.filePathToHash(url);
        CrawlerDBCollection collection = crawlerDB.getCollection(HTML_PAGES_META_DATA);
        collection.insertElement(htmlPageMetaDataId + HTML_PAGE_META_DATA_SUFFIX, htmlPageMetaData.toJSON());
        crawlerCache.put(url, htmlPageMetaData);
        return true;
    }

    public boolean setCrawlerState(String id, CrawlerState stateAsJson) {
        CrawlerDBCollection collection = crawlerDB.getCollection(CRAWLER_STATE);
        collection.insertElement(id, stateAsJson.toJSON());
        return true;
    }

    public CrawlerState getCrawlerState(String id) {
        CrawlerDBCollection collection = crawlerDB.getCollection(CRAWLER_STATE);
        return CrawlerState.fromJSON(collection.getElementById(id));

    }

    public Collection<HtmlPageMetaData> getAllHtmlPageMetaDataElements() {
        return crawlerCache.getMetaData();
    }
}
