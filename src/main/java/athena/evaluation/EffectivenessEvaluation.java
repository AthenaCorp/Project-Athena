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

    public void evaluation(String folderPath) {
        double count = 0.0;
        double totalPrecision = 0.0;
        double totalReciprocal = 0.0;
        double mean;
        String fs = File.separator;
        folderPath = commonUtils.getOutputPath() + fs + folderPath;
        calculatePAtK(folderPath);
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        if (files == null) {
            System.out.println("No files present or invalid folder");
        } else {
            for (File file : files) {
                List<String> lines = getTextFromFile(folderPath + fs + file.getName());
                if((lines.size() != 0) && (!file.getName().equals("pAtK.txt"))){
                    System.out.println(lines.size());
                    totalPrecision += averagePrecision(lines);
                    totalReciprocal += reciprocalRank(lines);
                    precisionRecallValues(lines, folderPath);
                }
                else {
                    totalPrecision += 0;
                    totalReciprocal += 0;
                }

                count++;
            }
        }
        mean = totalPrecision / count;
        System.out.println("Mean average precision: " + mean);
        mean = totalReciprocal / count;
        System.out.println("Mean Reciprocal Rank: " + mean);
        System.out.println("P @ K values for 5, 20 printed in pAtK.txt");
        //calculatePAtK(folderPath);
        System.out.println("Query-by-query precision/recall values printed " +
                "in [qid]_prvalues.txt");


    }

    public double reciprocalRank(List<String> lines){
        double count = 0.0;
        String[] tuple;
        tuple = lines.get(0).split(" ");
        ArrayList<String> relevantDocs = getRelevance(Integer.parseInt(tuple[0]));
        for (String line : lines) {
            tuple = line.split(" ");
            count += 1.0;
            if (relevantDocs.contains(tuple[2])) {
                return 1.0/count;
            }
        }
        return 0.0;
    }

    public double averagePrecision(List<String> lines){
        double mean = 0.0;
        double count = 0.0;
        double relevantCount = 0.0;
        double totalPrecision = 0.0;
        String[] tuple;
        tuple = lines.get(0).split(" ");
        ArrayList<String> relevantDocs = getRelevance(Integer.parseInt(tuple[0]));
        for (String line : lines) {
            tuple = line.split(" ");
            count += 1.0;
            if (relevantDocs.contains(tuple[2])) {
                relevantCount += 1.0;
                totalPrecision += (relevantCount / count);
            }

        }
        if(relevantCount != 0.0){
            mean = totalPrecision / relevantCount;
        }

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
            }
            totalPrecision = (relevantCount / count);
            listOfPrecision.add(totalPrecision);
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
            }
            totalRecall = (relevantCount / relevantDocs.size());
            listOfRecall.add(totalRecall);
        }
        return listOfRecall;
    }

    public void precisionRecallValues(List<String> lines, String folderPath){
        ArrayList<Double> lop = listOfPrecision(lines);
        ArrayList<Double> lor = listOfRecall(lines);
        String print = "";
        for (int i = 0; i < lop.size(); i++){
            print = print.concat(lop.get(i)+" "+lor.get(i)+"\n");
        }

        String[] tuple;
        tuple = lines.get(0).split(" ");

        File file = new File(folderPath+ "\\" +
                tuple[0]+"_prvalues.txt");
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(print);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void calculatePAtK(String folderPath) {

        String fs = File.separator;

        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        String line = "";
        if (files == null) {
            System.out.println("No files present or invalid folder");
        } else {
            for (File file : files) {

                List<String> lines = getTextFromFile(folderPath + fs + file.getName());
                //System.out.println(lines);
                if(lines.size() != 0){
                    String[] tuple;
                    tuple = lines.get(0).split(" ");
                    String queryId = tuple[0];
                    ArrayList<Double> lstOfPrecision = listOfPrecision(lines);
                    line  = line.concat(pAtK(lstOfPrecision, queryId)) ;
                }

            }
            File file = new File(folderPath+ "\\" + "pAtK.txt");
            FileWriter fileWriter;
            try {
                fileWriter = new FileWriter(file);
                fileWriter.write(line);
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public String pAtK(ArrayList<Double> precValues, String qId) {

        double pAtK5, pAtK20;
        if(precValues.size()>=5) {
            pAtK5 = precValues.get(4);
        }
        else {
            pAtK5 = 0;
        }
        if (precValues.size()>=20) {
            pAtK20 = precValues.get(19);
        }
        else {
            pAtK20 = 0;
        }

        String line = qId + " " + pAtK5 + " " + pAtK20 + "\n";
        return line;


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
