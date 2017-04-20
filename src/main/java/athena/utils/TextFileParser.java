package athena.utils;

import athena.crawler.CrawlerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
public class TextFileParser {

    @Autowired
    private CrawlerUtils crawlerUtils;
    @Autowired
    private CommonUtils commonUtils;

    @Value("${search.engine.steminput.folder}")
    private String inputFolder;

    public void splitTextFile() {
        String folderPath = commonUtils.getResourcePath() + inputFolder + "\\";
        commonUtils.verifyFolder(folderPath);
        String content = "";
        String filenames = "";
        try {
            List<String> lines = Files.readAllLines(Paths.get(commonUtils.getResourcePath() + "\\query\\cacm_stem.txt"),
                    Charset.forName("UTF-8"));
            for (String line : lines) {
                if (line.contains("#")) {
                    if (!filenames.equals("")) {
                        crawlerUtils.writeToFile(folderPath, filenames, content, CrawlerUtils.TEXT_FILE);
                    }
                    filenames = "CACM-" + String.format("%04d", Integer.parseInt(line.replace("# " + "", "")));
                    content = "";
                } else {
                    content = content.concat(line);
                    content = content.concat("\n");
                }
            }
            crawlerUtils.writeToFile(folderPath, filenames, content, CrawlerUtils.TEXT_FILE);
        } catch (IOException e) {
            System.out.print("IO Exception reading from file: ");
        }
    }
}

