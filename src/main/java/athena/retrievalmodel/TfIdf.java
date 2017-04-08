package athena.retrievalmodel;

import athena.index.InvertedIndexer;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by Pallav on 4/8/2017.
 */
public class TfIdf extends RetrievalModelImp {

    HashMap<String, Double> tfIdfMap = new HashMap<>();
    InvertedIndexer invertedIndexer = new InvertedIndexer();
    HashMap<String, HashMap<String, Integer>> index = invertedIndexer.readIndexFromJsonFile();
    HashMap<String, Integer> tokenCountMap = invertedIndexer.readTokenCountToJsonFile();
    Integer totalDocumentCount = tokenCountMap.size();

    public HashMap<String, Double> calculateTfIdf(String query) {

        HashMap queryTerms = getQueryMap(query);
        Set<String> queries = queryTerms.keySet();
        Set<String> docs = tokenCountMap.keySet();

        for (String s : docs) {
            String docId = s;                           //Document name
            double tfIdfScore = 0;
            for (String q : queries) {
                String queryTerm = q;
                double tf = calculateTf(queryTerm, docId);
                double idf = calculateIdf(queryTerm, docId);
                tfIdfScore += tf * idf;
            }
            tfIdfMap.put(docId, tfIdfScore);
        }
        return sortBM(tfIdfMap);
    }

    public double calculateTf(String queryTerm, String docId) {
        // Number of times a term appears in a document
        HashMap<String, Integer> integerHashMap = index.get(queryTerm.toLowerCase());
        if (integerHashMap.containsKey(docId)) {
            double num = index.get(queryTerm.toLowerCase()).get(docId);
            // Total number of terms in a document
            double totalTerms = tokenCountMap.get(docId);
            return num / totalTerms;
        } else {
            return 1;
        }
    }

    public double calculateIdf(String queryTerm, String docId) {
        // number of docs with term t in it
        double num = index.get(queryTerm).size();
        double idf = Math.log(totalDocumentCount / num);
        return idf;
    }

}
