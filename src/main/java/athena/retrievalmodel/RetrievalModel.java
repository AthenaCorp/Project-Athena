package athena.retrievalmodel;

import java.util.HashMap;

public interface RetrievalModel {

    // Split the whole query into separate words and counts
    HashMap<String, Integer> getQueryMap(String query);

    // For a given map, calculates the average token count
    double getAverageTokenCount(HashMap<String, Integer> tokenCount);

    // For a given map, sorts according to the values
    HashMap<String, Double> sortBM(HashMap<String, Double> hashMap);

    // Prints the scores in a proper given format
    void printN(HashMap<String, Double> hashMap, Integer count, Integer
            queryID);
}
