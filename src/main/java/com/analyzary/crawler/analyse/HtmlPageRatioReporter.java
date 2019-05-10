package com.analyzary.crawler.analyse;

import com.analyzary.crawler.model.HtmlPageMetaData;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;


/**
 * Creates html ratio report according to the predefined format (TSV)
 */
public class HtmlPageRatioReporter {

    public static String createReport(Collection<HtmlPageMetaData> htmlPageMetaDataElements) {
        StringBuilder report = new StringBuilder();
        report.append("url\tdepth\tratio\n");
        htmlPageMetaDataElements.stream().sorted(Comparator.comparing(HtmlPageMetaData::getUrl)).forEach(element -> {
            String url = element.getUrl();
            String domainName = getDomainName(url);
            Arrays.asList(element.getDepths()).stream().forEach(depth -> {
                StringBuilder row = new StringBuilder();
                if (domainName != null) {
                    row.append(url + "\t");
                    row.append(depth + "\t");
                    long sameDomainLink = element.getLinks().stream().parallel().filter(link ->
                            getDomainName(link).contains(domainName)).count();
                    row.append((element.getLinks().size() == 0 ? 0 :
                            roundTo((double) sameDomainLink / (double) (element.getLinks().size()), 2)) + "\n");
                    ;
                    report.append(row);
                }
            });
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
            String encodedUrl = urlEncoder(url);
            if (encodedUrl == null) {
                return "N/A";
            }
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
        } catch (Exception e) {
            // not going to happen - value came from JDK's own StandardCharsets
        }
        return null;

    }


    private static double roundTo(double value, int places) {
        return ((int) (value * Math.pow(10, places)) / Math.pow(10, places));
    }
}
