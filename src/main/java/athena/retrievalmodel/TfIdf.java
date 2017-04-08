package athena.retrievalmodel;

import athena.index.InvertedIndexer;

import java.util.*;

/**
 * Created by Pallav on 4/8/2017.
 */
public class TfIdf {

    BM25 helpObject = new BM25();
    HashMap<String, Double> tfIdfMap = new HashMap<>();
    InvertedIndexer invertedIndexer = new InvertedIndexer();
    HashMap<String, HashMap<String, Integer>> index = invertedIndexer.readIndexFromJsonFile();
    HashMap<String, Integer> tokenCountMap = invertedIndexer.readTokenCountToJsonFile();
    Double averageTokenCount = helpObject.getAverageTokenCount(tokenCountMap);
    Integer totalDocumentCount = tokenCountMap.size();

    public HashMap<String,Double> calculateTfIdf(String query) {

        HashMap<String,Double> scores = new HashMap<>();
        List<String> queryTerms = new ArrayList<>(Arrays.asList(query.split
                (" ")));
        Set<String> docs = tokenCountMap.keySet();

        for(String s: docs) {
            String docId = s;                           //Document name
            double tfIdfScore = 0;
            for (int i=0; i<queryTerms.size(); i++) {
                String queryTerm = queryTerms.get(i);
                double tf = calculateTf(queryTerm,docId);
                double idf = calculateIdf(queryTerm,docId);
                tfIdfScore += tf*idf;
            }
            scores.put(docId,tfIdfScore);
        }
        return scores;
    }

    public double calculateTf(String queryTerm, String docId) {
        // Number of times a term appears in a document
        double num = index.get(queryTerm).get(docId);
        // Total number of terms in a document
        double totalTerms = tokenCountMap.get(docId);
        return num/totalTerms;
    }

    public double calculateIdf(String queryTerm, String docId) {
        // number of docs with term t in it
        double num = index.get(queryTerm).size();
        double idf = Math.log(totalDocumentCount/num);
        return idf;
    }

}
