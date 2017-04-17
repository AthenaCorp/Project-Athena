package athena.retrievalmodel;

import athena.index.InvertedIndexer;
import athena.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Pallav on 4/8/2017.
 */

public class TfIdf implements RetrievalModel {

    @Autowired
    private InvertedIndexer invertedIndexer;
    @Autowired
    private CommonUtils commonUtils;
    @Value("${search.engine.print.size}")
    private Integer printSize;

    private HashMap<String, Double> tfIdfMap = new HashMap<>();
    private HashMap<String, HashMap<String, Integer>> index;
    private HashMap<String, Integer> tokenCountMap;

    private HashMap<String, Double> calculateTfIdf(String query) {
        HashMap<String, Integer> queryTerms = RetrievalModels.getQueryMap(query);
        Set<String> queries = queryTerms.keySet();
        Set<String> docs = tokenCountMap.keySet();
        Integer totalDocumentSize = tokenCountMap.size();
        for (String s : docs) {                       //Document name
            double tfIdfScore = 0;
            for (String q : queries) {
                double tf = calculateTf(q, s);
                double idf = calculateIdf(q, totalDocumentSize);
                tfIdfScore += tf * idf;
            }
            if (tfIdfScore != 0) {
                tfIdfMap.put(s, tfIdfScore);
            }
        }
        return RetrievalModels.sortBM(tfIdfMap);
    }

    private double calculateTf(String queryTerm, String docId) {
        // Number of times a term appears in a document
        HashMap<String, Integer> integerHashMap = index.get(queryTerm.toLowerCase());
        if (integerHashMap.containsKey(docId)) {
            double num = index.get(queryTerm.toLowerCase()).get(docId);
            // Total number of terms in a document
            double totalTerms = tokenCountMap.get(docId);
            return num / totalTerms;
        } else {
            return 0;
        }
    }

    private double calculateIdf(String queryTerm, Integer totalDocumentCount) {
        // number of docs with term t in it
        double num = index.get(queryTerm).size();
        return Math.log(totalDocumentCount / num);
    }

    @Override
    public HashMap<String, Double> getRanking(String query) {
        setIndexAndTokenMap();
        return calculateTfIdf(query);
    }

    @Override
    public String getModelName() {
        return "Tf-idf";
    }

    private void setIndexAndTokenMap() {
        index = invertedIndexer.readIndexFromJsonFile();
        tokenCountMap = invertedIndexer.readTokenCountToJsonFile();
    }

    @Override
    public void printN(HashMap<String, Double> hashMap, Integer queryID) {
        String fs = File.separator;
        String folderName = commonUtils.getOutputPath() + fs + getModelName() + fs;
        commonUtils.verifyFolder(folderName);

        if (queryID < 10) {
            folderName = folderName + "0";
        }
        String filePath = folderName + queryID + ".txt";

        RetrievalModels.printN(hashMap, queryID, filePath, getModelName(), printSize);
    }
}
