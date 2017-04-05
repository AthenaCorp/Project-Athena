package athena.crawler;

public class Crawler {
    public static final Integer DFS_MODE = 1;
    public static final Integer BFS_MODE = 2;

    public void startCrawler(String seed, String keyword, Integer mode) {
        if(mode.equals(DFS_MODE)) {
            CrawlerDFS crawlerDFS = new CrawlerDFS();
            crawlerDFS.startCrawlerDFS(seed, keyword);
        } else {
            CrawlerBFS crawlerBFS = new CrawlerBFS();
            if(keyword != null) {
                crawlerBFS.startCrawler(seed, keyword);
            } else {
                crawlerBFS.startCrawler(seed);
            }
        }
    }

    public void startCrawler(String seed) {
        CrawlerBFS crawlerBFS = new CrawlerBFS();
        crawlerBFS.startCrawler(seed);
    }
}

