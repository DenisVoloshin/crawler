package com.analyzary.crawler.analyse;

import com.analyzary.crawler.dom.LinkElement;
import com.analyzary.crawler.parser.HTMLPageParser;
import com.analyzary.crawler.storage.HtmlPageMetaData;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class HTMLPageAnalyser {

    public static List<String> analysePage(byte[] urlData, String baseUrl) {
        HTMLPageParser htmlPageParser = new HTMLPageParser(new String(urlData), baseUrl);
        return htmlPageParser.getElements().stream().map(element -> ((LinkElement) element).getLink()).collect(Collectors.toList());
    }

    public static String createReport(Collection<HtmlPageMetaData> htmlPageMetaDataElements) {
        StringBuilder report = new StringBuilder();
        report.append("url\tdepth\tratio\n");
        htmlPageMetaDataElements.stream().parallel().forEach(element -> {
            StringBuilder row = new StringBuilder();
            String url = element.getUrl();
            String domainName = getDomainName(url);
            if (domainName != null) {
                row.append(url + "\t");
                row.append(element.getDepth() + "\t");
                long sameDamainLink = element.getLinks().stream().parallel().filter(link -> link.contains(domainName)).count();
                row.append((element.getLinks().size() == 0 ? 0 :
                        roundTo((double) sameDamainLink / (double) (element.getLinks().size()), 2)) + "\n");
                ;
                report.append(row);
            }
        });
        return report.toString();
    }

    public static String getDomainName(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            return null;
        }
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    private static double roundTo(double value, int places) {
        return ((int)(value * Math.pow(10, places)) / Math.pow(10, places));
    }
}
