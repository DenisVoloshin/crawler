package com.analyzary.crawler.executor;

import com.analyzary.crawler.analyse.HTMLPageAnalyser;
import com.analyzary.crawler.cache.CrawlerCache;
import com.analyzary.crawler.cache.PersistentCache;
import com.analyzary.crawler.config.ConfigurationManager;
import com.analyzary.crawler.net.Connector;
import com.analyzary.crawler.net.request.CrawlerRequest;
import com.analyzary.crawler.queue.CrawlerWorkersQueue;

import com.analyzary.crawler.util.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class MainControllerTest {

    @Mock
    ConfigurationManager configurationManager;
    @Mock
    Connector connector;

    CrawlerWorkersQueue crawlerQueue;

    HTMLPageAnalyser htmlPageAnalyser;

    CrawlerCache crawlerCache;

    @Before
    void setup() {
        MockitoAnnotations.initMocks(this);
        crawlerQueue = new CrawlerWorkersQueue();
        htmlPageAnalyser = new HTMLPageAnalyser();
        crawlerCache = new PersistentCache(configurationManager);

        when(configurationManager.getRootPoint()).thenReturn("firstLevel.html");

        doAnswer((Answer) invocation -> {
            CrawlerRequest crawlerRequest = (CrawlerRequest) invocation.getArguments()[0];
            crawlerRequest.getCallback().onResponse(FileUtils.readFile("firstLevel.html").getBytes(), new HashMap<>(), 200);

            return null;
        }).when(connector).executeRequest(any(CrawlerRequest.class));
    }

    @Test
    public void execute() {
        MainController mainController = new MainController(
                configurationManager,
                crawlerQueue,
                connector,
                configurationManager.getRootPoint(),
                htmlPageAnalyser,
                crawlerCache);
        mainController.execute();
    }


}