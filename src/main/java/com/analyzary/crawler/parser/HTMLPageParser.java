package com.analyzary.crawler.parser;

import com.analyzary.crawler.dom.DOMElement;
import com.analyzary.crawler.dom.LinkElement;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Collection;
import java.util.stream.Collectors;


/**
 * Concrete implementation of {@link com.analyzary.crawler.parser.ElementExtractor}
 * is based on https://jsoup.org/ third party
 */
public class HTMLPageParser implements ElementExtractor {
    private Document document;
    private Collection<? extends DOMElement> linkElements;

    public HTMLPageParser(String html, String baseUrl) {
        this.document = Jsoup.parse(html);
        this.document.setBaseUri(baseUrl);
        this.linkElements = document.select("a[href]").stream()
                .map(element -> new LinkElement(element))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends DOMElement> getElements() {
        return linkElements;
    }
}
