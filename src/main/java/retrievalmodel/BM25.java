package retrievalmodel;

import index.InvertedIndexer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class BM25 {
    private static final Integer K2 = 100;
    private static final Double K1 = 1.2;
    private static final Double B = 0.75;
    private static final String SPACE = " ";

    public HashMap<String, Double> calculateBM25(String query) {
        HashMap<String, Double> bm25Map = new HashMap<>();
        InvertedIndexer invertedIndexer = new InvertedIndexer();
        HashMap<String, HashMap<String, Integer>> index = invertedIndexer.readIndexFromJsonFile(1);
        HashMap<String, Integer> tokenCountMap = invertedIndexer.readTokenCountToJsonFile(1);
        Double averageTokenCount = getAverageTokenCount(tokenCountMap);
        Integer totalDocumentCount = tokenCountMap.size();

        HashMap<String, Integer> terms;
        Set<String> termKeySet;
        HashMap<String, Integer> queryMap = getQueryMap(query);
        Set<String> queryWords = queryMap.keySet();

        for (String s : queryWords) {
            terms = index.get(s);
            if (terms != null) {
                termKeySet = terms.keySet();
                Integer termDocumentCount = termKeySet.size();
                Integer termQueryCount = queryMap.get(s);
                for (String t : termKeySet) {
                    Double value = Math.log((totalDocumentCount - termDocumentCount + 0.5) / (termDocumentCount + 0.5));
                    value = value * (((K1 + 1) * terms.get(t)) / (calculateK(tokenCountMap.get(t), averageTokenCount) + terms.get(t)));
                    value = value * (((K2 + 1) * termQueryCount) / (K2 + termQueryCount));
                    if (bm25Map.containsKey(t)) {
                        bm25Map.put(t, bm25Map.get(t) + value);
                    } else {
                        bm25Map.put(t, value);
                    }
                }
            }
        }

        return sortBM(bm25Map);
    }

    private HashMap<String, Integer> getQueryMap(String query) {
        HashMap<String, Integer> hashMap = new HashMap<>();
        String[] strings = query.split(SPACE);
        for (String s : strings) {
            if (hashMap.containsKey(s)) {
                hashMap.put(s, hashMap.get(s) + 1);
            } else {
                hashMap.put(s, 1);
            }
        }
        return hashMap;
    }

    private double getAverageTokenCount(HashMap<String, Integer> tokenCount) {
        Integer totalTokenCount = 0;
        for (String s : tokenCount.keySet()) {
            totalTokenCount += tokenCount.get(s);
        }
        return ((double) totalTokenCount / tokenCount.size());
    }

    private Double calculateK(Integer documentLength, Double averageLength) {
        return K1 * ((1 - B) + B * (documentLength / averageLength));
    }


    private HashMap<String, Double> sortBM(HashMap<String, Double> hashMap) {
        List<Map.Entry<String, Double>> entrySet = new ArrayList<>(hashMap.entrySet());
        entrySet.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        HashMap<String, Double> hashMap1 = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : entrySet) {
            hashMap1.put(e.getKey(), e.getValue());
        }
        return hashMap1;
    }

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
}
