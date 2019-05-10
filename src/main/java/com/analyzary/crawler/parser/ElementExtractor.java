package com.analyzary.crawler.parser;

import com.analyzary.crawler.dom.DOMElement;
import java.util.Collection;


public interface ElementExtractor {
    Collection<? extends DOMElement> getElements();
}
