package athena.retrievalmodel;

import athena.index.InvertedIndexer;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

@Component
public class BM25 extends RetrievalModelImp {

    private static final Integer K2 = 100;
    private static final Double K1 = 1.2;
    private static final Double B = 0.75;
    private static final String SPILT_CHARACTER = " ";

    public HashMap<String, Double> calculateBM25(String query) {
        HashMap<String, Double> bm25Map = new HashMap<>();
        InvertedIndexer invertedIndexer = new InvertedIndexer();
        HashMap<String, HashMap<String, Integer>> index = invertedIndexer.readIndexFromJsonFile();
        HashMap<String, Integer> tokenCountMap = invertedIndexer.readTokenCountToJsonFile();
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

        return sortBM(bm25Map);
    }

    private Double calculateK(Integer documentLength, Double averageLength) {
        return K1 * ((1 - B) + B * (documentLength / averageLength));
    }

}
