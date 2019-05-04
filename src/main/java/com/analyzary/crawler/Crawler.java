package com.analyzary.crawler;

import com.analyzary.crawler.analyse.HTMLPageAnalyser;
import com.analyzary.crawler.cache.PersistentCache;
import com.analyzary.crawler.config.ConfigurationManager;
import com.analyzary.crawler.executor.MainController;
import com.analyzary.crawler.net.OkHttpConnector;
import com.analyzary.crawler.queue.CrawlerWorkersQueue;

import java.util.logging.Logger;

public class Crawler {

    Logger logger = Logger.getLogger(Crawler.class.getName());

    ConfigurationManager configurationManager;

    public Crawler(ConfigurationManager configurationManager){
        this.configurationManager = configurationManager;
    }

    public void start(){
        logger.info("Crawler started");
        logger.info(configurationManager.toString());

        CrawlerWorkersQueue crawlerQueue  = new CrawlerWorkersQueue();
        PersistentCache crawlerCache  = new PersistentCache(configurationManager);
        OkHttpConnector connector  = new OkHttpConnector();
        HTMLPageAnalyser htmlPageAnalyser = new HTMLPageAnalyser();

        MainController mainController = new MainController(
                configurationManager,
                crawlerQueue,
                connector,
                configurationManager.getRootPoint(),
                htmlPageAnalyser,
                crawlerCache);
        mainController.execute();
        logger.info("Crawler stopped");
        System.exit(0);
    }

    public void stop(){
        logger.info("Crawler stopped");
    }
}
