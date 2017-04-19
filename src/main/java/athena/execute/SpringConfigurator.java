package athena.execute;

import athena.TextFileParser;
import athena.evaluation.EffectivenessEvaluation;
import athena.index.InvertedIndexer;
import athena.queryexpansion.PseudoRelevanceFeedback;
import athena.retrievalmodel.RetrievalModel;
import athena.utils.CommonUtils;
import athena.utils.SearchEngineUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SpringConfigurator {
    private Properties properties;
    private ClassPathXmlApplicationContext context;
    private CommonUtils commonUtils;

    public SpringConfigurator() {
        context = new ClassPathXmlApplicationContext("spring/bean.xml");
        properties = (Properties) context.getBean("searchEngineProperties");
        commonUtils = (CommonUtils) context.getBean("commonUtils");
    }

    private void generateIndex() {
        CommonUtils commonUtils = (CommonUtils) context.getBean("commonUtils");
        long startTime = commonUtils.printTimeStamp("Index Creation Started");
        InvertedIndexer indexer = (InvertedIndexer) context.getBean("invertedIndexer");
        String resourceFolder = commonUtils.getResourcePath();
        String inputFolder;
        if (Boolean.getBoolean(properties.getProperty("search.engine.enable.stemming"))) {
            inputFolder = resourceFolder + properties.getProperty("search.engine.steminput.folder") + "\\";
        } else {
            inputFolder = resourceFolder + properties.getProperty("search.engine.input.folder") + "\\";
        }
        String indexFolder = resourceFolder + properties.getProperty("search.engine.index.folder") + "\\";
        indexer.setIndexFolder(indexFolder);
        commonUtils.cleanFolder(indexFolder);
        indexer.createIndex(inputFolder);
        long stopTime = commonUtils.printTimeStamp("Index Creation Completed");
        commonUtils.printTotalTime(startTime, stopTime);
    }

    private void retrieveRanking(String query, Integer queryID) {
        RetrievalModel retrievalModel = (RetrievalModel) context.getBean("retrievalModel");

        if (properties.getProperty("search.engine.enable.query.expansion").equals("false")) {
            retrievalModel.printN(retrievalModel.getRanking(query, queryID), queryID, query);
        } else {
            PseudoRelevanceFeedback feedback = (PseudoRelevanceFeedback) context.getBean("pseudoRelevanceFeedback");
            retrievalModel.printN(feedback.getRanking(query, queryID), queryID, query);
        }
    }

    public void executeQuerySearching(Boolean createIndex) {
        long startTime = commonUtils.printTimeStamp("Ranking Started");
        Boolean isLuceneEnabled = Boolean.parseBoolean(properties.getProperty("search.engine.enable.lucene"));
        commonUtils.cleanFolder(commonUtils.getOutputPath() + properties.getProperty("search.engine.name"));
        if (isLuceneEnabled) {
            executeLucene(createIndex);
        } else {
            executeAthena(createIndex);
        }
        long stopTime = commonUtils.printTimeStamp("Ranking Completed");
        commonUtils.printTotalTime(startTime, stopTime);
    }

    public void executePseudoRelevanceFeedback() {
        CommonUtils commonUtils = (CommonUtils) context.getBean("commonUtils");
        long startTime = commonUtils.printTimeStamp("Query Expansion Started");
        InvertedIndexer indexer = (InvertedIndexer) context.getBean("invertedIndexer");
        String resourceFolder = commonUtils.getResourcePath();
        String indexFolder = resourceFolder + properties.getProperty("search.engine.index.folder") + "\\";
        indexer.setIndexFolder(indexFolder);
        setRetrievalModel();
        PseudoRelevanceFeedback feedback = (PseudoRelevanceFeedback) context.getBean("pseudoRelevanceFeedback");
        System.out.println(feedback.expandQuery("samelson", 1));
        long stopTime = commonUtils.printTimeStamp("Query Expansion Completed");
        commonUtils.printTotalTime(startTime, stopTime);
    }

    private ClassPathXmlApplicationContext getContext() {
        return context;
    }

    private Properties getProperties() {
        return properties;
    }

    private void setRetrievalModel() {
        ConfigurableEnvironment environment = context.getEnvironment();
        environment.addActiveProfile("default");
        String retrievalModel = properties.getProperty("search.engine.retrieval.model");
        switch (retrievalModel.toLowerCase()) {
            case "tfidf":
                environment.addActiveProfile("tfidf");
                System.out.println("Using " + retrievalModel + " Retrieval Model");
                break;
            case "bm25":
                System.out.println("Using " + retrievalModel + " Retrieval Model");
                break;
            default:
                System.err.println("Invalid Retrieval Model : " + retrievalModel);
                System.out.println("Using Default [BM25] RetrievalModel");
                break;
        }
        context.setEnvironment(environment);
        context.refresh();
    }

    private void execStemFile(String filename) {
        TextFileParser t = (TextFileParser) context.getBean("textFileParser");
        t.splitTextFile(filename);
    }

    public void executeEvaluation() {
        EffectivenessEvaluation e = (EffectivenessEvaluation) context.getBean("effectivenessEvaluation");
        Boolean isLuceneEnabled = Boolean.parseBoolean(properties.getProperty("search.engine.enable.lucene"));
        if (isLuceneEnabled) {
            e.evaluation("Lucene");
        } else {
            e.evaluation(properties.getProperty("search.engine.name"));
        }
    }

    private void executeLucene(Boolean createIndex) {
        LuceneExecutor luceneExecutor = new LuceneExecutor();
        luceneExecutor.executor(createIndex);
    }

    private void executeAthena(Boolean createIndex) {
        String filePath = "query\\cacm.query.txt";
        if (createIndex) {
            generateIndex();
        }
        InvertedIndexer indexer = (InvertedIndexer) context.getBean("invertedIndexer");
        String resourceFolder = commonUtils.getResourcePath();
        String indexFolder = resourceFolder + properties.getProperty("search.engine.index.folder") + "\\";
        indexer.setIndexFolder(indexFolder);
        setRetrievalModel();
        Boolean doCaseFolding = Boolean.parseBoolean(properties.getProperty("search.engine.enable.case.fold"));
        Boolean doStopping = Boolean.parseBoolean(properties.getProperty("search.engine.enable.stopping"));
        /*Map<Integer, String> queries = new HashMap<>();
        queries.put(13, "code optimization for space efficiency");
        retrieveRanking(queries.get(13), 13);*/
        Map<Integer, String> queries = SearchEngineUtils.getQuerySet(resourceFolder + filePath, doCaseFolding, doStopping);

        for (int i = 1; i <= queries.size(); i++) {
            retrieveRanking(queries.get(i), i);
        }
    }
}
