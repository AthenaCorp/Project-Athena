package crawler;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CommonUtils;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class CrawlerBFS {
    private LinkedHashMap<String, Integer> frontier = new LinkedHashMap<>();
    private LinkedHashSet<String> visited = new LinkedHashSet<>();

    private static final Integer MAX_PAGES = 1000;
    private static final Integer MAX_DEPTH = 5;
    private static final Integer POLITENESS_DELAY = 1;
    private static final Integer LIMIT_FRONTIER_SIZE = 10 * MAX_PAGES;  // Limiting Frontier Size to keep the memory usage low

    public void startCrawler(String seed) {
        startCrawler(seed, null);
    }

    public void startCrawler(String seed, String keyword) {
        CommonUtils commonUtils = new CommonUtils();
        long startTime = commonUtils.printTimeStamp("Crawling Started");

        frontier.put(seed, 1);
        while ((frontier.size() > 0) && (visited.size() < MAX_PAGES)) {
            Map.Entry<String, Integer> nextURL = frontier.entrySet().iterator().next();
            focusedCrawler(nextURL.getKey(), nextURL.getValue(), keyword);
        }
        CrawlerUtils crawlerUtils = new CrawlerUtils();
        crawlerUtils.printURLSet("0000_Visited URLs", visited);

        long stopTime = commonUtils.printTimeStamp("Crawling Completed");
        commonUtils.printTotalTime(startTime, stopTime);
        //System.out.println("Total Time : " + (new Date().getTime() - startTime) / 1000 + " seconds");
    }

    private void focusedCrawler(String stringURL, Integer depth, String keyword) {
        try {
            CrawlerUtils crawlerUtils = new CrawlerUtils();
            crawlerUtils.cleanupURLs(stringURL);
            if (!visited.contains(stringURL) && depth < MAX_DEPTH) {
                Thread.sleep(POLITENESS_DELAY);
                Document document = Jsoup.connect(URLDecoder.decode(stringURL, "UTF-8")).get();
                //canonicalLink is used to identify whether a URL is redirected or not.
                // Canonical Link or Actual URL is used to keep track of the visited pages
                String canonicalLink = document.head().getElementsByAttributeValue("rel", "canonical").attr("href");
                if (canonicalLink.equals(stringURL)) {
                    if (frontier.size() < LIMIT_FRONTIER_SIZE) {
                        parseAndCollectURLs(document, depth, keyword);
                    }
                    crawlerUtils.createHTMLFile(document.title(), document.toString());
                    visited.add(stringURL);
                    frontier.remove(stringURL);
                } else {
                    frontier.remove(stringURL);
                    focusedCrawler(canonicalLink, depth, keyword);
                }
            } else {
                frontier.remove(stringURL);
            }
        } catch (HttpStatusException e) {
            System.err.println(e.getMessage() + " Status Code: " + e.getStatusCode() + " " + stringURL);
            frontier.remove(stringURL);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }


    private void parseAndCollectURLs(Document document, Integer depth, String keyword) {
        String elementUrl;
        String anchorText;

        Element content = document.getElementById("mw-content-text");
        content.getElementsByAttributeValue("class", "references").remove();
        content.getElementsByAttributeValue("class", "citation book").remove();
        content.getElementsByAttributeValue("class", "navbox").remove();
        Elements elements = content.select("a[href]");
        for (Element element : elements) {
            elementUrl = element.attributes().get("href");
            // Considering on wiki pages and removing Main and Template Pages
            if (elementUrl.startsWith("/wiki/") && !(elementUrl.contains("/wiki/Main_Page"))
                    && !(elementUrl.contains(":"))) {
                if (elementUrl.contains("#")) {
                    elementUrl = elementUrl.substring(0, elementUrl.indexOf("#"));
                }
                // anchorText variable collects the Anchor Text, URL of the Page and Title of the Page.
                // This is used to determine whether the page is relevant
                anchorText = element.ownText() + " " + elementUrl + " " + element.attributes().get("title");
                elementUrl = "https://en.wikipedia.org" + elementUrl;
                if (keyword == null || anchorText.toLowerCase().contains(keyword.toLowerCase())) {
                    if (!frontier.containsKey(elementUrl) && !visited.contains(elementUrl)) {
                        frontier.put(elementUrl, depth + 1);
                    }
                }
            }
        }
    }
}
