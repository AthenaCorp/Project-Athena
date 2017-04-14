package athena.queryexpansion;

import athena.index.InvertedIndexer;
import athena.retrievalmodel.RetrievalModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
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
        Set<String> keySet = bm25map.keySet();
        File[] files = new File[keySet.size()];
        int i = 0;
        for (String s : keySet) {
            files[i] = new File(invertedIndexer.getDataFolder() + s + ".txt");
            i++;
        }
        HashMap<String, HashMap<String, Integer>> topDocs = invertedIndexer.createIndex(files);
        HashMap<String, Integer> termFrequencyTable = invertedIndexer.generateTermFrequencyTable(topDocs);
        HashMap<String, Integer> sortedTF = invertedIndexer.sortTermFrequency(termFrequencyTable);
        Set<String> sortKeySet = sortedTF.keySet();

        int j = 0;
        for (String s : sortKeySet) {
            if (j < 4) {
                result = result + " " + s;
            }
            j++;
        }
        return result;
    }
}
