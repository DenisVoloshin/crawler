package com.analyzary.crawler.dom;

import java.util.Map;

public interface DOMElement {
    public String tagName();
    public String tagInner();
    public Map<String,String> getAttrs();
}
