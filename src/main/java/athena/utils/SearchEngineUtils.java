package athena.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SearchEngineUtils {
    public static Map<Integer, String> getQuerySet(String filename) {
        Map<Integer, String> queries = new HashMap<>();
        try {
            File file = new File(filename);
            Document content = Jsoup.parse(file, "UTF-8");
            Elements elements = content.getElementsByTag("doc");
            for (Element e : elements) {
                String[] element = e.text().split(" ", 2);
                queries.put(Integer.parseInt(element[0]), element[1]);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return queries;
    }
}
