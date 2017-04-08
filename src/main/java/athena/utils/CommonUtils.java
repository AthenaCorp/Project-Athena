package athena.utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class CommonUtils {
    private final String[] unitList = new String[]{"ms", "secs", "mins", "hrs"};
    private static final int MILLI_TIME_FACTOR = 1000;
    private static final int TIME_FACTOR = 60;
    private String projectPath;
    private String resourcePath;

    public CommonUtils() {
        setProjectPath();
        setResourcePath();
    }

    public List<Element> extractLinks(Document document) {
        String elementUrl;
        List<Element> elementList = new ArrayList<>();
        Elements elements = document.select("a[href]");
        for (Element element : elements) {
            elementUrl = element.attributes().get("href");

            if (elementUrl.startsWith("/wiki/") && !(elementUrl.contains("/wiki/Main_Page"))
                    && !(elementUrl.contains(":"))) {
                elementList.add(element);
            }
        }
        return elementList;
    }

    public long printTimeStamp() {
        return printTimeStamp("Timestamp");
    }

    public long printTimeStamp(String eventName) {
        Date date = new Date();
        System.out.println(eventName + " : " + date);
        return date.getTime();
    }

    public long getTotalTimeInSeconds(long startTime, long stopTime) {
        return (stopTime - startTime) / 1000;
    }

    public void printTotalTime(long startTime, long stopTime) {
        long totalTime = stopTime - startTime;
        int index = 0;
        String timeString;
        if (totalTime < MILLI_TIME_FACTOR) {
            timeString = totalTime + " " + unitList[index];
        } else {
            long newTime = totalTime / MILLI_TIME_FACTOR;
            totalTime = totalTime % MILLI_TIME_FACTOR;
            timeString = getTotalTime(newTime, index + 1) + " " + totalTime + " " + unitList[index];
        }
        System.out.println("Total Time: " + timeString);
    }

    private String getTotalTime(long totalTime, int index) {
        if (totalTime < TIME_FACTOR && index < 4) {
            return totalTime + " " + unitList[index];
        } else {
            long newTime = totalTime / TIME_FACTOR;
            totalTime = totalTime % TIME_FACTOR;
            return getTotalTime(newTime, index + 1) + " " + totalTime + " " + unitList[index];
        }
    }

    public void debugFile(String fileName, String content) {
        try {
            FileWriter fileWriter = new FileWriter(new File(fileName));
            fileWriter.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setProjectPath() {
        File file = new File("");
        projectPath = file.getAbsolutePath() + File.separator;
    }

    private void setResourcePath() {
        String fs = File.separator;
        resourcePath = projectPath + "src" + fs + "main" + fs + "resources" + fs;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void verifyFolder(String folder) {
        File file = new File(folder);
        if(!file.exists()) {
            if(file.mkdir()) {
                System.out.println("Folder created : " +  folder);
            } else {
                System.err.println("Error creating folder : " +  folder);
            }

        }
    }
}
