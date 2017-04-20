package athena.retrievalmodel;

import athena.index.InvertedIndexer;
import athena.utils.CommonUtils;
import athena.utils.SearchEngineUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class BM25 implements RetrievalModel {

    @Autowired
    private InvertedIndexer invertedIndexer;
    @Autowired
    private CommonUtils commonUtils;

    @Value("${search.engine.name}")
    private String searchEngineName;
    @Value("${search.engine.ngrams}")
    private Integer nGrams;
    @Value("${search.engine.print.size}")
    private Integer printSize;
    @Value("${search.engine.enable.snippet}")
    private Boolean genSnippet;
    @Value("${search.engine.relevance.bm25}")
    private Boolean useRelevance;

    private static final Integer K2 = 100;
    private static final Double K1 = 1.2;
    private static final Double B = 0.75;

    public HashMap<String, Double> getRanking(String query, Integer queryID) {
        return calculateBM25(query, queryID);
    }

    @Override
    public String getModelName() {
        return "BM25";
    }

    private HashMap<String, Double> calculateBM25(String query, Integer queryID) {
        HashMap<String, Double> bm25Map = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> index = invertedIndexer.readIndexFromJsonFile();
        HashMap<String, Integer> tokenCountMap = invertedIndexer.readTokenCountToJsonFile();
        Double averageTokenCount = RetrievalModels.getAverageTokenCount(tokenCountMap);
        Integer totalDocumentCount = tokenCountMap.size();

        HashMap<String, Integer> documentList;
        Set<String> documentKeySet;
        HashMap<String, Integer> queryMap = RetrievalModels.getQueryMap(query, nGrams);
        Set<String> queryWords = queryMap.keySet();
        List<String> relevantDocs = SearchEngineUtils.getRelevance(queryID);
        Integer R = relevantDocs.size();

        for (String s : queryWords) {
            documentList = index.get(s);
            if (documentList != null) {
                documentKeySet = documentList.keySet();
                Integer ri = getRelevantCountForTerm(relevantDocs, documentKeySet);
                Integer termDocumentCount = documentKeySet.size();
                Double logValue;
                if (useRelevance) {
                    Double numerator = (ri + 0.5) / (R - ri + 0.5);
                    Double denominator = (termDocumentCount - ri + 0.5) / (totalDocumentCount - termDocumentCount - R + ri + 0.5);
                    logValue = Math.log(numerator / denominator);
                } else {
                    logValue = Math.log((totalDocumentCount - termDocumentCount + 0.5) / (termDocumentCount + 0.5));
                }
                Integer termQueryCount = queryMap.get(s);
                for (String docID : documentKeySet) {
                    Double value = logValue;
                    value = value * (((K1 + 1) * documentList.get(docID)) / (calculateK(tokenCountMap.get(docID), averageTokenCount) +
                            documentList.get(docID)));
                    value = value * (((K2 + 1) * termQueryCount) / (K2 + termQueryCount));
                    if (bm25Map.containsKey(docID)) {
                        bm25Map.put(docID, bm25Map.get(docID) + value);
                    } else {
                        bm25Map.put(docID, value);
                    }
                }
            }
        }

        return RetrievalModels.sortBM(bm25Map);
    }

    private Integer getRelevantCountForTerm(List<String> relevantDocs, Set<String> termDocuments) {
        int count = relevantDocs.size();
        relevantDocs.removeAll(termDocuments);
        return count - relevantDocs.size();
    }

    private Double calculateK(Integer documentLength, Double averageLength) {
        return K1 * ((1 - B) + B * (documentLength / averageLength));
    }


    @Override
    public void printN(HashMap<String, Double> hashMap, Integer queryID, String query) {
        String fs = File.separator;
        String folderName = commonUtils.getOutputPath() + fs + searchEngineName + fs;
        commonUtils.verifyFolder(folderName);
        if (queryID < 10) {
            folderName = folderName + "0";
        }
        String filePath = folderName + queryID + ".txt";
        RetrievalModels.printN(hashMap, queryID, filePath, searchEngineName, printSize, query, genSnippet);
    }
}
