package athena.retrievalmodel;

import java.util.HashMap;

/**
 * Created by Pallav on 4/8/2017.
 */
public interface RetrievalModel {
    HashMap<String, Double> getRanking(String query);
}
