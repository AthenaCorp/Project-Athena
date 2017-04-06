package athena.crawler;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

@Component
public class CrawlerUtils {
    private static final String WEB_PAGES_FOLDER = "HW3\\WebPages";
    private static final String INDEX_FILES_FOLDER = "HW3\\IndexFiles";
    private String[] fileTypes = {"txt", "html"};

    public static final Integer TEXT_FILE = 0;

    public static final Integer HTML_FILE = 1;

    public CrawlerUtils() {
        File file = new File(WEB_PAGES_FOLDER);
        if (!file.exists()) {
            file.mkdir();
        }
        file = new File(INDEX_FILES_FOLDER);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public void writeToFile(String folderName, String filename, String content, Integer FileType) {
        FileWriter fileWriter;
        try {
            File file = new File(folderName + "/" + filename.replaceAll("[/+\"'@$%^*&<>,?]", "")
                    + "." + fileTypes[FileType]);
            fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.close();

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void createHTMLFile(String filename, String content) {
        writeToFile(WEB_PAGES_FOLDER, filename, content, HTML_FILE);
    }


    public void printURLSet(String filename, HashSet<String> hashSet) {
        FileWriter fileWriter;
        try {
            File file = new File(WEB_PAGES_FOLDER + "/" + filename + ".txt");
            fileWriter = new FileWriter(file);
            for (String link : hashSet) {
                fileWriter.write(link + "\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

    public String cleanupURLs(String url) {
        if (url.contains("#")) {
            url = url.substring(0, url.indexOf("#"));
        }
        return url;
    }
}
