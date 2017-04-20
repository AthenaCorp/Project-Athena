package athena.retrievalmodel;

import athena.snippetgeneration.SnippetGeneration;
import athena.utils.CommonUtils;

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

    private static SnippetGeneration sn = new SnippetGeneration();
    private static CommonUtils commonUtils = new CommonUtils();

    // Split the whole query into separate words and counts
    public static HashMap<String, Integer> getQueryMap(String query, Integer nGrams) {
        HashMap<String, Integer> hashMap = new HashMap<>();
        String[] words = query.split(SPLIT_CHARACTER);
        int tokenCount = words.length - nGrams;
        String word;
        for (int i = 0; i <= tokenCount; i++) {
            word = words[i];
            for (int j = 1; j < nGrams; j++) {
                word = word + " " + words[i + j];
            }
            if (hashMap.containsKey(word)) {
                hashMap.put(word, hashMap.get(word) + 1);
            } else {
                hashMap.put(word, 1);
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

    public static void printN(HashMap<String, Double> hashMap, Integer
            queryID, String filePath, String model, Integer printSize, String
                                      query, Boolean genSnippet) {
        int k = 1;
        DecimalFormat numberFormat = new DecimalFormat("#.000");
        File file = new File(filePath);
        String snipFolder = file.getParent() + "\\Snippets";
        commonUtils.verifyFolder(snipFolder);
        try {
            FileWriter fileWriter = new FileWriter(file);
            for (String s : hashMap.keySet()) {
                fileWriter.write(queryID + " Q0 " + s + " " + k + " " + numberFormat.format(hashMap.get(s)) +
                        " Athena[" + model + "]\n");
                if (genSnippet) {
                    commonUtils.appendToFile(snipFolder + "\\Snippet_" + queryID + ".txt", s + "\n" + sn.thisSnippet(s, query) + "\n\n");
                }
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
