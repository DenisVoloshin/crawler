package com.analyzary.crawler.storage;

import com.analyzary.crawler.cache.CrawlerCache;
import com.analyzary.crawler.cache.InMemoryCache;
import com.analyzary.crawler.config.ConfigurationManager;
import com.analyzary.crawler.model.CrawlerState;
import com.analyzary.crawler.model.HtmlPageMetaData;
import com.analyzary.crawler.util.FileUtils;

import javax.annotation.CheckForNull;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CrawlerDAO {

    private static final String DB_NAME = "CRAWLER_DB";
    private static final String HTML_PAGES = "HTML_PAGES";
    private static final String CRAWLER_STATE = "CRAWLER_STATE";
    private static final String HTML_PAGES_META_DATA = "HTML_PAGES_META_DATA";
    private static final String HTML_PAGE_META_DATA_SUFFIX = "_META_DATA.json";
    private static final String HTML_PAGE_DATA_SUFFIX = ".html";

    private CrawlerDB crawlerDB;
    private CrawlerCache<String, HtmlPageMetaData> crawlerCache;
    private ThreadPoolExecutor persistentWorkerExecutor;


    public static class SingletonHolder {
        public static final CrawlerDAO HOLDER_INSTANCE = new CrawlerDAO();
    }

    public static CrawlerDAO getInstance() {
        return CrawlerDAO.SingletonHolder.HOLDER_INSTANCE;
    }


    private CrawlerDAO() {
        this.crawlerCache = new InMemoryCache(ConfigurationManager.getInstance());
        crawlerDB = new FileSystemCrawlerDBClient(ConfigurationManager.getInstance()).getDB(DB_NAME);
        crawlerDB.createCollection(HTML_PAGES);
        crawlerDB.createCollection(HTML_PAGES_META_DATA);
        crawlerDB.createCollection(CRAWLER_STATE);
    }


    public void connect() {
        this.persistentWorkerExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        loadCache();
    }

    private void loadCache() {
        CrawlerDBCollection metaDataCollection = crawlerDB.getCollection(HTML_PAGES_META_DATA);
        metaDataCollection.getAllElements().stream().filter(metaDateAsString -> !metaDateAsString.isEmpty()).forEach(metaDateAsString -> {
            HtmlPageMetaData metaData = new HtmlPageMetaData(metaDateAsString);
            crawlerCache.put(metaData.getUrl(), metaData);
        });

    }

    public void close() {
        if (this.persistentWorkerExecutor != null) {
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


    @CheckForNull
    public HtmlPageMetaData getHtmlPageMetaData(String url) {
        return crawlerCache.get(url);
    }


    public boolean insertHtmlPageMetaData(final String url, HtmlPageMetaData htmlPageMetaData) {
        crawlerCache.put(url, htmlPageMetaData);

        this.persistentWorkerExecutor.submit(new Thread(() -> {
            String htmlPageMetaDataId = FileUtils.filePathToHash(url);
            CrawlerDBCollection metaDataCollection = crawlerDB.getCollection(HTML_PAGES_META_DATA);
            metaDataCollection.insertElement(htmlPageMetaDataId + HTML_PAGE_META_DATA_SUFFIX, htmlPageMetaData.toJSON());

            if (htmlPageMetaData.getData() != null && htmlPageMetaData.getData().length > 0) {
                CrawlerDBCollection htmlDataCollection = crawlerDB.getCollection(HTML_PAGES);
                htmlDataCollection.insertElement(htmlPageMetaDataId + HTML_PAGE_DATA_SUFFIX, new String(htmlPageMetaData.getData()));
            }
        }));

        return true;
    }

    public boolean setCrawlerState(CrawlerState state) {
        CrawlerDBCollection collection = crawlerDB.getCollection(CRAWLER_STATE);
        collection.insertElement(state.getId(), state.toJSON());
        return true;
    }

    public CrawlerState getCrawlerState(String id) {
        CrawlerDBCollection collection = crawlerDB.getCollection(CRAWLER_STATE);
        return CrawlerState.fromJSON(collection.getElementById(id));

    }

    public Collection<HtmlPageMetaData> getAllHtmlPageMetaDataElements() {
        return crawlerCache.getAllElements();
    }
}
