package com.analyzary.crawler;

import com.analyzary.crawler.config.ConfigurationManager;
import com.analyzary.crawler.model.CrawlerState;
import com.analyzary.crawler.storage.CrawlerDAO;


/**
 * Class implements Crawler states automat
 */
public class CrawlerStateManager {

    private CrawlerDAO crawlerDAO;
    private ConfigurationManager configurationManager;

    public CrawlerStateManager(CrawlerDAO crawlerDAO, ConfigurationManager configurationManager) {
        this.crawlerDAO = crawlerDAO;
        this.configurationManager = configurationManager;
    }

    public CrawlerState getNextState() {
        CrawlerState previousState = crawlerDAO.getCrawlerState(configurationManager.getCrawlingId());
        CrawlerState currentState;
        if (previousState == null) {
            currentState = new CrawlerState(configurationManager.getCrawlingId(), CrawlerState.State.RUNNING);
            crawlerDAO.setCrawlerState(currentState);
        }else if(previousState.getState() == CrawlerState.State.RUNNING){
            // the crawler was stop before it finished. should be running on recovery mode
            currentState = new CrawlerState(configurationManager.getCrawlingId(), CrawlerState.State.RECOVERY);
            crawlerDAO.setCrawlerState(currentState);
        }else{
            currentState = previousState;
        }
        return currentState;
    }
}
