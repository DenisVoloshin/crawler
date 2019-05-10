package com.analyzary.crawler;

import com.analyzary.crawler.analyse.HTMLPageAnalyser;
import com.analyzary.crawler.analyse.HtmlPageRatioReporter;
import com.analyzary.crawler.config.ConfigurationManager;
import com.analyzary.crawler.executor.MainController;
import com.analyzary.crawler.monitor.CrawlerMonitor;
import com.analyzary.crawler.net.OkHttpConnector;
import com.analyzary.crawler.queue.CrawlerWorkersQueue;
import com.analyzary.crawler.storage.CrawlerDAO;
import com.analyzary.crawler.model.CrawlerState;
import com.analyzary.crawler.util.FileUtils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;


/**
 * The main class responsible for initialization
 * all Crawler components and determines the running mode
 */
public class Crawler {

    Logger logger = Logger.getLogger(Crawler.class.getName());
    ConfigurationManager configurationManager;

    public Crawler(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public void start() {
        logger.info("Crawler started");
        CrawlerMonitor.getInstance().start();
        logger.info(configurationManager.toString());

        CrawlerWorkersQueue crawlerQueue = new CrawlerWorkersQueue();
        OkHttpConnector connector = new OkHttpConnector();
        HTMLPageAnalyser htmlPageAnalyser = new HTMLPageAnalyser();
        CrawlerDAO.getInstance().connect();

        // determine if the crawler previous launch is no completed
        // if yes the crawler will be running in the recovery mode.
        CrawlerState currentState =
                new CrawlerStateManager(CrawlerDAO.getInstance(), configurationManager).getNextState();


        MainController mainController = new MainController(
                configurationManager,
                crawlerQueue,
                connector,
                configurationManager.getRootPoint(),
                currentState,
                htmlPageAnalyser,
                CrawlerDAO.getInstance());
        mainController.execute();
        String report = HtmlPageRatioReporter.createReport(CrawlerDAO.getInstance().getAllHtmlPageMetaDataElements());
        System.out.println(report);

        try {
            FileUtils.writeFile(report.getBytes(),"domain-ratio-report.tsv");
        } catch (IOException e) {
           //TODO
        }

        stop();
        System.exit(0);
    }

    public void stop() {
        CrawlerDAO.getInstance().setCrawlerState(new CrawlerState(configurationManager.getCrawlingId(), CrawlerState.State.COMPLETE, configurationManager.getCrawlingDepth()));
        logger.info("Crawler stopped");
        CrawlerMonitor.getInstance().stop();
    }
}
