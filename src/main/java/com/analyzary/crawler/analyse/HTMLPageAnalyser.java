package com.analyzary.crawler.analyse;

import com.analyzary.crawler.dom.LinkElement;
import com.analyzary.crawler.parser.HTMLPageParser;

import java.util.List;
import java.util.stream.Collectors;

public class HTMLPageAnalyser {
    public List<String> analysePage(byte[] urlData, String baseUrl) {
        HTMLPageParser htmlPageParser = new HTMLPageParser(new String(urlData), baseUrl);
        return htmlPageParser.getElements().stream().map(element -> ((LinkElement) element).getLink()).collect(Collectors.toList());
    }
}
