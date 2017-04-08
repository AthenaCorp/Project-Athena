package athena.queryexpansion;
import athena.index.InvertedIndexer;
import athena.retrievalmodel.BM25;
import athena.utils.CommonUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Harshit on 4/8/2017.
 */
public class PsFeedback {

    public static String expandQuery(String query){
        BM25 b = new BM25();
        String result;
        InvertedIndexer in = new InvertedIndexer();
        CommonUtils commonUtils = new CommonUtils();

        HashMap<String, Double> bm25map = b.calculateBM25(query);
        Set<String > keySet = bm25map.keySet();
        File[] files = new File[keySet.size()];
        int i = 0;
        for (String s : keySet) {
            files[i] = new File(commonUtils.getResourcePath() +
                    "athena\\DataFiles\\"+ s + ".txt");
            i++;
        }
        HashMap<String, HashMap<String, Integer>> topdocs = in.createIndex
                (files);

        HashMap<String, Integer> sortedTF = in.sortTermFrequency(in
                .generateTermFrequencyTable
                (topdocs));
        Set<String > sortkeyset = sortedTF.keySet();
        int j = 0;
        for (String s : sortkeyset) {
            if(j<4){
                result = query.concat(s);
            }
            j++;
        }


        return query;

    }
}
