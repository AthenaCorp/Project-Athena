package athena.retrievalmodel;

import athena.index.InvertedIndexer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by Pallav on 4/8/2017.
 */

public class TfIdf implements RetrievalModel {

    @Autowired
    private InvertedIndexer invertedIndexer;

    private HashMap<String, Double> tfIdfMap = new HashMap<>();
    private HashMap<String, HashMap<String, Integer>> index;

    private HashMap<String, Double> calculateTfIdf(String query) {
        HashMap queryTerms = RetrievalModels.getQueryMap(query);
        HashMap<String, Integer> tokenCountMap = invertedIndexer.readTokenCountToJsonFile();
        Set<String> queries = queryTerms.keySet();
        Set<String> docs = tokenCountMap.keySet();

        for (String s : docs) {                       //Document name
            double tfIdfScore = 0;
            for (String q : queries) {
                String queryTerm = q;
                double tf = calculateTf(queryTerm, s, tokenCountMap);
                double idf = calculateIdf(queryTerm, tokenCountMap.size());
                tfIdfScore += tf * idf;
            }
            tfIdfMap.put(s, tfIdfScore);
        }
        return RetrievalModels.sortBM(tfIdfMap);
    }

    public double calculateTf(String queryTerm, String docId, HashMap<String, Integer> tokenCountMap) {
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

    public double calculateIdf(String queryTerm, Integer totalDocumentCount) {
        // number of docs with term t in it
        double num = index.get(queryTerm).size();
        return Math.log(totalDocumentCount / num);
    }

    @Override
    public HashMap<String, Double> getRanking(String query) {
        setIndex();
        return calculateTfIdf(query);
    }

    public void setIndex() {
        index = invertedIndexer.readIndexFromJsonFile();
    }
}
