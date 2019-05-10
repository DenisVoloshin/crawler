package com.analyzary.crawler;

import com.analyzary.crawler.config.ConfigurationManager;
import com.analyzary.crawler.model.CrawlerState;
import com.analyzary.crawler.storage.CrawlerDAO;


/**
 * Class implements Crawler states automat,
 * base on the preserved previous state it determine the next state.
 * see README file where it's illustrated.
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
        if (previousState == null ||
                previousState != null && previousState.getDepth() != configurationManager.getCrawlingDepth()) {
            currentState = new CrawlerState(configurationManager.getCrawlingId(), CrawlerState.State.RUNNING, configurationManager.getCrawlingDepth());
            crawlerDAO.setCrawlerState(currentState);
        } else if (previousState.getState() == CrawlerState.State.RUNNING) {
            // the crawler was stop before it finished. should be running on recovery mode
            currentState = new CrawlerState(configurationManager.getCrawlingId(), CrawlerState.State.RECOVERY, configurationManager.getCrawlingDepth());
            crawlerDAO.setCrawlerState(currentState);
        } else if (previousState.getState() == CrawlerState.State.COMPLETE) {
            currentState = new CrawlerState(configurationManager.getCrawlingId(), CrawlerState.State.UPDATE, configurationManager.getCrawlingDepth());
            crawlerDAO.setCrawlerState(currentState);
        } else {
            currentState = previousState;
        }
        return currentState;
    }
}
