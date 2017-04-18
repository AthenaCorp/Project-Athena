package athena.utils;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SearchEngineUtils {
    private final static String STRING_REPLACEMENT = " ";
    private static CommonUtils commonUtils = new CommonUtils();

    public static Map<Integer, String> getQuerySet(String filename) {
        Map<Integer, String> queries = new HashMap<>();
        try {
            File file = new File(filename);
            Document content = Jsoup.parse(file, "UTF-8");
            Elements elements = content.getElementsByTag("doc");
            for (Element e : elements) {
                String[] element = e.text().split(" ", 2);
                queries.put(Integer.parseInt(element[0]), cleanDocumentContent(element[1], true, false));
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return queries;
    }


    public static String cleanDocumentContent(String content, Boolean doCaseFold, Boolean doStopping) {
        return cleanDocumentContent(content, doCaseFold, doStopping, 1);
    }

    public static String cleanDocumentContent(String content, Boolean doCaseFold, Boolean doStopping, Integer noiseFactor) {
        if (doCaseFold) {
            content = caseFoldText(content);
        }
        if (doStopping) {
            content = stoppedText(content);
        }
        for (int i = 0; i < noiseFactor; i++) {
            content = removeNoise(content);
        }
        return content;
    }

    public static String caseFoldText(String text) {
        return text.toLowerCase();
    }

    public static String stoppedText(String text) {
        File file = new File(commonUtils.getResourcePath() + "\\query\\common_words");
        ArrayList<String> stopList = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String word = bufferedReader.readLine();
            while (word != null) {
                stopList.add(word);
                word = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String stopWord : stopList) {
            text = text.replaceAll("(?i)"+stopWord, " ");
        }

        return text;
    }

    //TODO: Time is not getting preserved. Do we have to?
    //TODO: Not Preserving "," not relevant with Cacm
    public static String removeNoise(String text) {
        text = StringUtils.replaceAll(text, "(\\[[0-9]\\w{0,3}\\])", STRING_REPLACEMENT);
        text = StringUtils.replaceAll(text, "(\\.+ )|(\\-+ )|( -+)|( \\.+)|(^-)|(^\\.)", STRING_REPLACEMENT);
        text = StringUtils.replaceAll(text, "(\\.+ )", STRING_REPLACEMENT);
        text = StringUtils.replaceAll(text, "(\\-+ )", STRING_REPLACEMENT);
        text = StringUtils.replaceAll(text, "( -+)", STRING_REPLACEMENT);
        text = StringUtils.replaceAll(text, "( \\.+)", STRING_REPLACEMENT);
        text = StringUtils.replaceAll(text, "(^-)", STRING_REPLACEMENT);
        text = StringUtils.replaceAll(text, "(^\\.)", STRING_REPLACEMENT);
        text = StringUtils.replaceAll(text, "([^0-9a-zA-Z\\.\\- ])", STRING_REPLACEMENT);
        return StringUtils.replace(text, "  ", STRING_REPLACEMENT);
    }
}
