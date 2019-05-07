package com.analyzary.crawler.analyse;

import com.analyzary.crawler.dom.LinkElement;
import com.analyzary.crawler.parser.HTMLPageParser;
import com.analyzary.crawler.storage.HtmlPageMetaData;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
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
                long sameDomainLink = element.getLinks().stream().parallel().filter(link ->
                        getDomainName(link).contains(domainName)).count();
                row.append((element.getLinks().size() == 0 ? 0 :
                        roundTo((double) sameDomainLink / (double) (element.getLinks().size()), 2)) + "\n");
                ;
                report.append(row);
            }
        });
        return report.toString();
    }

    public static String getDomainName(String url) {

        String encoderURL = urlEncoder(url);
        if (encoderURL == null) {
            return "N/A";
        }
        URI uri;
        try {
            uri = new URI(urlEncoder(url));
        } catch (URISyntaxException e) {
            return "N/A";
        }
        String domain = uri.getHost();
        return domain == null ? "N/A" : (domain.startsWith("www.") ? domain.substring(4) : domain);
    }


    public static String urlEncoder(String url) {
        try {
            return java.net.URLDecoder.decode(url, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // not going to happen - value came from JDK's own StandardCharsets
        }
        return null;

    }


    private static double roundTo(double value, int places) {
        return ((int) (value * Math.pow(10, places)) / Math.pow(10, places));
    }
}
