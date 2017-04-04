package crawler;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class CrawlerDFS {

    private LinkedHashSet<String> visited = new LinkedHashSet<>();

    private static final Integer MAX_PAGES = 1000;
    private static final Integer MAX_DEPTH = 5;
    private static final Integer POLITENESS_DELAY = 1000;
    private static final Integer LIMIT_FRONTIER_SIZE = 10 * MAX_PAGES; // Limiting Frontier Size to keep the memory usage low

    public void startCrawlerDFS(String seed, String keyword) {
        System.out.println("Crawling Started : " + new Date());
        long startTime = new Date().getTime();

        startCrawlerDFS(seed, 1, keyword);
        CrawlerUtils crawlerUtils = new CrawlerUtils();
        crawlerUtils.printURLSet("0000_Visited URLs", visited);

        System.out.println("Crawling Completed : " + new Date());
        System.out.println("Total Time : " + (new Date().getTime() - startTime) / 1000 + " seconds");
    }

    private void startCrawlerDFS(String seed, Integer depth, String keyword) {
        LinkedHashMap<String, Integer> localFrontier = new LinkedHashMap<>();
        localFrontier.put(seed, depth);
        Map.Entry<String, Integer> nextURL = localFrontier.entrySet().iterator().next();
        while ((localFrontier.size() > 0) && (visited.size() < MAX_PAGES)) {
            localFrontier.putAll(focusedCrawler(nextURL.getKey(), nextURL.getValue(), keyword));
            localFrontier.remove(nextURL.getKey());
            if (depth < MAX_DEPTH && localFrontier.size() > 0) {
                nextURL = localFrontier.entrySet().iterator().next();

                // Calling self recursively with the left most child and so on

                startCrawlerDFS(nextURL.getKey(), depth + 1, keyword);
            }
        }
    }


    private LinkedHashMap<String, Integer> focusedCrawler(String stringURL, Integer depth, String keyword) {
        LinkedHashMap<String, Integer> frontier = new LinkedHashMap<>();
        CrawlerUtils crawlerUtils = new CrawlerUtils();
        try {
            crawlerUtils.cleanupURLs(stringURL);
            stringURL = URLDecoder.decode(stringURL, "UTF-8");
            if (!visited.contains(stringURL) && depth < MAX_DEPTH) {
                Thread.sleep(POLITENESS_DELAY);
                Document document = Jsoup.connect(stringURL).get();
                String canonicalLink = document.head().getElementsByAttributeValue("rel", "canonical").attr("href");

                //canonicalLink is used to identify whether a URL is redirected or not.
                // Canonical Link or Actual URL is used to keep track of the visited pages

                canonicalLink = URLDecoder.decode(canonicalLink, "UTF-8");
                if (canonicalLink.equals(stringURL)) {
                    if (frontier.size() < LIMIT_FRONTIER_SIZE) {
                        frontier = parseAndCollectURLs(document, depth, keyword);
                    }
                    crawlerUtils.createHTMLFile(document.title(), document.toString());
                    visited.add(stringURL);
                    frontier.remove(stringURL);
                } else {
                    //System.out.println(stringURL + " redirects to " + canonicalLink);
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
        return frontier;
    }

    private LinkedHashMap<String, Integer> parseAndCollectURLs(Document document, Integer depth, String keyword) {
        LinkedHashMap<String, Integer> frontier = new LinkedHashMap<>();
        String elementUrl;
        String anchorText;
        CrawlerUtils crawlerUtils = new CrawlerUtils();
        Elements elements = document.select("a[href]");
        for (Element element : elements) {
            elementUrl = element.attributes().get("href");

            // Considering on wiki pages and removing Main and Template Pages
            if (elementUrl.startsWith("/wiki/") && !(elementUrl.contains("/wiki/Main_Page"))
                    && !(elementUrl.contains(":"))) {
                elementUrl = crawlerUtils.cleanupURLs(elementUrl);

                // anchorText variable collects the Anchor Text, URL of the Page and Title of the Page.
                // This is used to determine whether the page is relevant

                anchorText = element.ownText() + " " + elementUrl + " " + element.attributes().get("title");
                elementUrl = "https://en.wikipedia.org" + elementUrl;
                if (keyword == null || anchorText.toLowerCase().contains(keyword.toLowerCase())) {
                    if (!frontier.containsKey(elementUrl) && !visited.contains(elementUrl)) {
                        try {
                            frontier.put(URLDecoder.decode(elementUrl, "UTF-8"), depth + 1);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //System.out.println(elementUrl);
            }
            //System.out.println(element.ownText());
        }
        return frontier;
    }
}
