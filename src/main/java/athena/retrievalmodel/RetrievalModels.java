package athena.retrievalmodel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by Pallav on 4/8/2017.
 */
public class RetrievalModels {
    private static final String SPLIT_CHARACTER = " ";

    // Split the whole query into separate words and counts
    public static HashMap<String, Integer> getQueryMap(String query) {
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

    // For a given map, calculates the average token count
    public static double getAverageTokenCount(HashMap<String, Integer> tokenCount) {
        Integer totalTokenCount = 0;
        for (String s : tokenCount.keySet()) {
            totalTokenCount += tokenCount.get(s);
        }
        return ((double) totalTokenCount / tokenCount.size());
    }

    // For a given map, sorts according to the values
    public static HashMap<String, Double> sortBM(HashMap<String, Double> hashMap) {
        List<Map.Entry<String, Double>> entrySet = new ArrayList<>(hashMap.entrySet());
        entrySet.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        HashMap<String, Double> hashMap1 = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : entrySet) {
            hashMap1.put(e.getKey(), e.getValue());
        }
        return hashMap1;
    }

    public static void printN(HashMap<String, Double> hashMap, Integer queryID, String filePath, String model, Integer printSize) {
        int k = 1;
        DecimalFormat numberFormat = new DecimalFormat("#.000");
        File file = new File(filePath);
        try {
            FileWriter fileWriter = new FileWriter(file);
            for (String s : hashMap.keySet()) {
                fileWriter.write(queryID + " Q0 " + s + " " + k + " " + numberFormat.format(hashMap.get(s)) +
                        " Athena[" + model + "]\n");
                k++;
                if (k > printSize) {
                    break;
                }
            }
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
