package athena.evaluation;

import athena.crawler.CrawlerUtils;
import athena.index.InvertedIndexer;
import athena.utils.CommonUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Component

public class EffectivenessEvaluation {

    @Autowired
    private CrawlerUtils crawlerUtils;
    @Autowired
    private CommonUtils commonUtils;
    @Autowired
    private InvertedIndexer invertedIndexer;

    public double meanAveragePrecision(String folderPath) {
        double count = 0.0;
        double totalPrecision = 0.0;
        double mean;
        String fs = File.separator;
        folderPath = commonUtils.getOutputPath() + fs + folderPath;
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        if (files == null) {
            System.out.println("No files present or invalid folder");
        } else {
            for (File file : files) {
                List<String> lines = getTextFromFile(folderPath + fs + file.getName());
                totalPrecision += averagePrecision(lines);
                count++;
            }
        }
        mean = totalPrecision / count;
        return mean;
    }

    public double averagePrecision(List<String> lines){
        double totalPrecision = 0.0;
        double mean;
        ArrayList<Double> lop = listOfPrecision(lines);
        for (Double d : lop) {
            totalPrecision += d;
        }
        mean = totalPrecision / lop.size();
        return mean;
    }

    public ArrayList<Double> listOfPrecision(List<String> lines){
        ArrayList<Double> listOfPrecision = new ArrayList<>();
        double count = 0.0;
        double relevantCount = 0.0;
        double totalPrecision;
        String[] tuple;
        tuple = lines.get(0).split(" ");
        ArrayList<String> relevantDocs = getRelevance(Integer.parseInt(tuple[0]));
        for (String line : lines) {
            tuple = line.split(" ");
            count += 1.0;
            if (relevantDocs.contains(tuple[2])) {
                relevantCount += 1.0;
                totalPrecision = (relevantCount / count);
                listOfPrecision.add(totalPrecision);
            }
        }
        return listOfPrecision;
    }

    public ArrayList<Double> listOfRecall(List<String> lines){
        ArrayList<Double> listOfRecall = new ArrayList<>();
        double relevantCount = 0.0;
        double totalRecall;
        String[] tuple;
        tuple = lines.get(0).split(" ");
        ArrayList<String> relevantDocs = getRelevance(Integer.parseInt(tuple[0]));
        for (String line : lines) {
            tuple = line.split(" ");
            if(relevantDocs.contains(tuple[2])){
                relevantCount += 1.0;
                totalRecall = (relevantCount / relevantDocs.size());
                listOfRecall.add(totalRecall);
            }
        }
        return listOfRecall;
    }

    public void calculatePAtK(String folderPath) {

        String fs = File.separator;
        folderPath = commonUtils.getOutputPath() + fs + folderPath;
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        if (files == null) {
            System.out.println("No files present or invalid folder");
        } else {
            for (File file : files) {
                List<String> lines = getTextFromFile(folderPath + fs + file.getName());
                String[] tuple;
                tuple = lines.get(0).split(" ");
                String queryId = tuple[0];
                ArrayList<Double> lstOfPrecision = listOfPrecision(lines);
                pAtK(lstOfPrecision, queryId);

            }
        }

    }


    public void pAtK(ArrayList<Double> precValues, String qId) {

        double pAtK5 = precValues.get(5);
//        double pAtK20 = precValues.get(20);
        String line = qId + " " + pAtK5;
        File file = new File(commonUtils.getOutputPath()+ "\\" + "pAtK.txt");
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
//
//    public void writeToFilePAtK(ArrayList<Double> precisionValues, String query) {
//        double pAtK5 = precisionValues.get(5);
//        double pAtK20 = precisionValues.get(20);
//        String line = query + pAtK5 + pAtK20;
//
//    }

    public ArrayList<String> getRelevance(int queryNumber) {
        String[] tuple;
        String filename = commonUtils.getResourcePath() + "//query//cacm.rel";
        List<String> lines = getTextFromFile(filename);
        ArrayList<String > relevantDocs = new ArrayList<>();
        for (String line : lines) {
            tuple = line.split(" ");
            if(Integer.parseInt(tuple[0]) == queryNumber){
                relevantDocs.add(tuple[2]);
            }
        }
        return relevantDocs;
    }

    public List<String> getTextFromFile(String filename){
        List<String> lines = null;
        try {
            lines = Files.readAllLines (Paths.get (filename),
                    Charset.forName ("UTF-8"));
        }
        catch (IOException e) {
            System.out.print ("IO Exception reading from file: ");
            System.out.println (filename);
            System.out.println (e);
        }
        return lines;
    }
}
