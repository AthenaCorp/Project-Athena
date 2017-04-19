package athena.queryexpansion;

import athena.index.InvertedIndexer;
import athena.retrievalmodel.RetrievalModel;
import athena.retrievalmodel.RetrievalModels;
import athena.utils.SearchEngineUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Harshit on 4/8/2017.
 */
@Component
public class PseudoRelevanceFeedback {

    @Autowired
    private InvertedIndexer invertedIndexer;
    @Autowired
    private RetrievalModel retrievalModel;

    public String expandQuery(String query) {
        String result = query;

        HashMap<String, Double> bm25map = retrievalModel.getRanking(query);
        HashMap<String, Double> bm25mapSorted = RetrievalModels.sortBM(bm25map);
        Set<String> keySet = bm25mapSorted.keySet();
        File[] files = new File[15];
        int i = 0;

        for (String s : keySet) {
            if(i<15){
                files[i] = new File(invertedIndexer.getDataFolder() + s + ".txt");
                i = i+ 1;
            }
        }
        HashMap<String, HashMap<String, Integer>> topDocs = invertedIndexer.createIndex(files, false);
        HashMap<String, Integer> termFrequencyTable = invertedIndexer.generateTermFrequencyTable(topDocs);
        HashMap<String, Integer> sortedTF = invertedIndexer.sortTermFrequency(termFrequencyTable);
        Set<String> sortKeySet = sortedTF.keySet();
        int j = 0;
        ArrayList<String> stopList = SearchEngineUtils.getStopWords();

        for (String s : sortKeySet) {
            if ((j < 4)&&(!stopList.contains(s.toLowerCase()))) {
                result = result + " " + s;
                j++;
            }
        }
        return result;
    }

    public HashMap<String, Double> getRanking(String query) {
        return retrievalModel.getRanking(expandQuery(query));
    }

}
