package athena.retrievalmodel;

import java.util.HashMap;

public interface RetrievalModel {
    HashMap<String, Double> getRanking(String query);
}
