package athena.retrievalmodel;

import athena.queryexpansion.PsFeedback;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class RetrievalModelImp implements RetrievalModel {


    String SPLIT_CHARACTER = " ";
    @Override
    public HashMap<String, Integer> getQueryMap(String query) {
        HashMap<String, Integer> hashMap = new HashMap<>();
        String[] strings = query.split(SPLIT_CHARACTER);
        for (String s : strings) {
            if (hashMap.containsKey(s)) {
                hashMap.put(s, hashMap.get(s) + 1);
            } else {
                hashMap.put(s, 1);
            }
        }
        return hashMap;
    }

    @Override
    public double getAverageTokenCount(HashMap<String, Integer> tokenCount) {
        Integer totalTokenCount = 0;
        for (String s : tokenCount.keySet()) {
            totalTokenCount += tokenCount.get(s);
        }
        return ((double) totalTokenCount / tokenCount.size());
    }

    @Override
    public HashMap<String, Double> sortBM(HashMap<String, Double> hashMap) {
        List<Map.Entry<String, Double>> entrySet = new ArrayList<>(hashMap.entrySet());
        entrySet.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        HashMap<String, Double> hashMap1 = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : entrySet) {
            hashMap1.put(e.getKey(), e.getValue());
        }
        return hashMap1;
    }

    @Override
    public void printN(HashMap<String, Double> hashMap, Integer count, Integer queryID) {
        int length = 60;
        int k = 1;
        String s1;
        // Interstellar

        DecimalFormat numberFormat = new DecimalFormat("#.000");
        File file = new File("Task2_" + queryID + ".txt");
        try {
            FileWriter fileWriter = new FileWriter(file);
            for (String s : hashMap.keySet()) {
                s1 = s;
                while (length > s1.length()) {
                    s1 = s1 + " ";
                }
                fileWriter.write(queryID + "\tQ0   " + s1 + k + "\t\t" + numberFormat.format(hashMap.get(s)) + "  Normandy\n");
                k++;
                if (k > count) {
                    break;
                }
            }
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String expandThisQuery(String query){
        BM25 b = new BM25();
        HashMap<String, Double> bm25map = b.calculateBM25(query);
        return PsFeedback.expandQuery(query, bm25map);
    }
}
