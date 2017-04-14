package athena.queryexpansion;
import athena.index.InvertedIndexer;
import athena.retrievalmodel.RetrievalModel;
import athena.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Harshit on 4/8/2017.
 */
@Component
public class PsFeedback {

    @Autowired
    private InvertedIndexer invertedIndexer;
    @Autowired
    private CommonUtils commonUtils;
    @Autowired
    private RetrievalModel retrievalModel;

    public String expandQuery(String query){
        String result = query;
        //InvertedIndexer in = new InvertedIndexer();
        //CommonUtils commonUtils = new CommonUtils();

        HashMap<String, Double> bm25map = retrievalModel.getRanking(query);
        Set<String > keySet = bm25map.keySet();
        File[] files = new File[keySet.size()];
        int i = 0;
        for (String s : keySet) {
            files[i] = new File(commonUtils.getResourcePath() +  "athena\\DataFiles\\"+ s + ".txt");
            System.out.println(files[i]);
            i++;
        }
        HashMap<String, HashMap<String, Integer>> topDocs = invertedIndexer.createIndex(files);
        HashMap<String, Integer> termFrequencyTable = invertedIndexer.generateTermFrequencyTable(topDocs);
        HashMap<String, Integer> sortedTF = invertedIndexer.sortTermFrequency(termFrequencyTable);
        Set<String > sortKeySet = sortedTF.keySet();

        int j = 0;
        for (String s : sortKeySet) {
            if(j<4){
                result = result + " "+ s;
            }
            j++;
        }
        return result;
    }
}
