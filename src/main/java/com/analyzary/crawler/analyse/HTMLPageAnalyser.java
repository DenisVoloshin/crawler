package com.analyzary.crawler.analyse;

import com.analyzary.crawler.dom.LinkElement;
import com.analyzary.crawler.parser.HTMLPageParser;

import java.util.List;
import java.util.stream.Collectors;

/**
 *  Responsible for extraction valid http links from the given html page
 */
public class HTMLPageAnalyser {

    private static String HTTP = "http";
    private static String HTTPS = "https";

    public static List<String> extractHtmlLinks(byte[] urlData, String baseUrl) {
        HTMLPageParser htmlPageParser = new HTMLPageParser(new String(urlData), baseUrl);
        return htmlPageParser.getElements().stream().map(element -> ((LinkElement) element).
                getLink()).filter(link -> (link.startsWith(HTTP) || link.startsWith(HTTPS))).collect(Collectors.toList());
    }

}
