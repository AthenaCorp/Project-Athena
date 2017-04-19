package athena.execute;

import athena.retrievalmodel.BM25;
import athena.retrievalmodel.TfIdf;
import athena.utils.CommonUtils;

import java.util.HashMap;

public class Executor {
    public static void main(String[] args) {
        String folderPath = "HW3\\WebPages\\";
        Executor executor = new Executor();

        //executor.executeTask1(folderPath);
        //executor.executeTask2();
        //executor.executeTask3();
//        executor.executeBM25();
        //executor.executeTfIdf();
        //executor.executePS();
    }

    public void executePS() {
        //System.out.println(PsFeedback.expandQuery("samelson"));
    }

    public void executeBM25() {
        CommonUtils commonUtils = new CommonUtils();
        long startTime = commonUtils.printTimeStamp("Index Creation Started");

        BM25 bm25 = new BM25();

        HashMap<Integer, String> queries = new HashMap<>();
        queries.put(1, "global warming potential");
        queries.put(2, "green power renewable energy");
        queries.put(3, "solar energy california");
        queries.put(4, "light bulb bulbs alternative alternatives");

        for(Integer i : queries.keySet()) {
            HashMap<String, Double> bm = bm25.getRanking(queries.get(i), i);
            bm25.printN(bm, i, queries.get(i));
        }

        long stopTime = commonUtils.printTimeStamp("Index Creation Completed");
        commonUtils.printTotalTime(startTime, stopTime);
    }

    public void executeTfIdf() {
        CommonUtils commonUtils = new CommonUtils();
        long startTime = commonUtils.printTimeStamp("Index Creation Started");

        TfIdf tfIdf = new TfIdf();
        BM25 bm25 = new BM25();

        HashMap<Integer, String> queries = new HashMap<>();
        queries.put(1, "global warming potential");
        queries.put(2, "green power renewable energy");
        queries.put(3, "solar energy california");
        queries.put(4, "light bulb bulbs alternative alternatives");

        for(Integer i : queries.keySet()) {
            HashMap<String, Double> tidf = tfIdf.getRanking(queries.get(i), i);
            tfIdf.printN(tidf, i,  queries.get(i));
        }

        long stopTime = commonUtils.printTimeStamp("Index Creation Completed");
        commonUtils.printTotalTime(startTime, stopTime);
    }
}
