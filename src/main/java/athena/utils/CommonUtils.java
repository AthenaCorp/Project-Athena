package athena.utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommonUtils {
    private final String[] unitList = new String[]{"ms", "secs", "mins", "hrs"};
    private static final int MILLI_TIME_FACTOR = 1000;
    private static final int TIME_FACTOR = 60;
    private String projectPath;
    private String resourcePath;
    private String outputPath;

    public CommonUtils() {
        setProjectPath();
        setResourcePath();
        setOutputPath();
        verifyFolder(getOutputPath());
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

    private void setOutputPath() {
        String fs = File.separator;
        outputPath = projectPath + "target" + fs + "output" + fs;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void verifyFolder(String folder) {
        File file = new File(folder);
        if (!file.exists()) {
            if (!file.mkdir()) {
                System.err.println("Error creating folder : " + folder);
            }
        }
    }

    public List<String> getLinesFromFile(String filename) {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(filename),
                    Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.out.print("IO Exception reading from file: ");
            System.out.println(filename);
            System.out.println(e);
        }
        return lines;
    }

    public String getTextFromFile(String filename) {
        String content = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
            String c = br.readLine();
            while (c != null) {
                content += c;
                c = br.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public void clearFolder(String folderPath) {
        File file = new File(folderPath);
        if (file.exists()) {
            deleteFolder(file);
        }
        verifyFolder(folderPath);
    }

    public void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
