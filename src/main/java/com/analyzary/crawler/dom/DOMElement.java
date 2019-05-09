package com.analyzary.crawler.dom;

import java.util.Map;


/**
 * Interface for base html page element
 */
public interface DOMElement {
    String tagName();

    String tagInner();

    Map<String, String> getAttrs();
}
