package com.analyzary.crawler.dom;

import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;


/**
 *  Represents  HTML DOM link element
 */
public class LinkElement implements DOMElement {
    private Element element;

    public LinkElement(Element element) {
        this.element = element;
    }

    @Override
    public String tagName() {
        return element.tagName();
    }

    @Override
    public String tagInner() {
        return element.text();
    }

    public String getLink() {
        return element.attr("abs:href");
    }

    @Override
    public Map<String, String> getAttrs() {
        return element.attributes().asList().stream().
                collect(Collectors.toMap(Attribute::getKey, Attribute::getValue));
    }
}
