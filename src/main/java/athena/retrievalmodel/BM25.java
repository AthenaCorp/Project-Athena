package athena.retrievalmodel;

import athena.index.InvertedIndexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class BM25 implements RetrievalModel {

    @Autowired
    private InvertedIndexer invertedIndexer;

    private static final Integer K2 = 100;
    private static final Double K1 = 1.2;
    private static final Double B = 0.75;


    public HashMap<String, Double> getRanking(String query) {
        return calculateBM25(query);
    }

    private HashMap<String, Double> calculateBM25(String query) {
        HashMap<String, Double> bm25Map = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> index = invertedIndexer.readIndexFromJsonFile();
        HashMap<String, Integer> tokenCountMap = invertedIndexer.readTokenCountToJsonFile();
        Double averageTokenCount = RetrievalModels.getAverageTokenCount(tokenCountMap);
        Integer totalDocumentCount = tokenCountMap.size();

        HashMap<String, Integer> terms;
        Set<String> termKeySet;
        HashMap<String, Integer> queryMap = RetrievalModels.getQueryMap(query);
        Set<String> queryWords = queryMap.keySet();

        for (String s : queryWords) {
            terms = index.get(s);
            if (terms != null) {
                termKeySet = terms.keySet();
                Integer termDocumentCount = termKeySet.size();
                for (String t : termKeySet) {
                Integer termQueryCount = queryMap.get(s);
                    Double value = Math.log((totalDocumentCount - termDocumentCount + 0.5) / (termDocumentCount + 0.5));
                    value = value * (((K1 + 1) * terms.get(t)) / (calculateK(tokenCountMap.get(t), averageTokenCount) +
                            terms.get(t)));
                    value = value * (((K2 + 1) * termQueryCount) / (K2 + termQueryCount));
                    if (bm25Map.containsKey(t)) {
                        bm25Map.put(t, bm25Map.get(t) + value);
                    } else {
                        bm25Map.put(t, value);
                    }
                }
            }
        }

        return RetrievalModels.sortBM(bm25Map);
    }

    private Double calculateK(Integer documentLength, Double averageLength) {
        return K1 * ((1 - B) + B * (documentLength / averageLength));
    }

}
