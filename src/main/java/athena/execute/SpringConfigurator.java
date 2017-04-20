package athena.execute;

import athena.evaluation.EffectivenessEvaluation;
import athena.index.InvertedIndexer;
import athena.queryexpansion.PseudoRelevanceFeedback;
import athena.retrievalmodel.RetrievalModel;
import athena.utils.CommonUtils;
import athena.utils.SearchEngineUtils;
import athena.utils.TextFileParser;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

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
        Boolean doStemming = Boolean.parseBoolean(properties.getProperty("search.engine.enable.stemming"));
        String inputFolder;
        if (doStemming) {
            createStemDocuments();
            inputFolder = resourceFolder + properties.getProperty("search.engine.steminput.folder") + "\\";
        } else {
            inputFolder = resourceFolder + properties.getProperty("search.engine.input.folder") + "\\";
        }
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
        String filePath;
        if (createIndex) {
            generateIndex();
        }
        String resourceFolder = commonUtils.getResourcePath();
        setRetrievalModel();
        Boolean doCaseFolding = Boolean.parseBoolean(properties.getProperty("search.engine.enable.case.fold"));
        Boolean doStopping = Boolean.parseBoolean(properties.getProperty("search.engine.enable.stopping"));
        Boolean doStemming = Boolean.parseBoolean(properties.getProperty("search.engine.enable.stemming"));
        Map<Integer, String> queries;
        if(doStemming) {
            filePath = "query\\cacm_stem.query.txt";
            queries = SearchEngineUtils.getQuerySetStem(resourceFolder + filePath, doCaseFolding, doStopping);
        } else {
            filePath = "query\\cacm.query.txt";
            queries = SearchEngineUtils.getQuerySet(resourceFolder + filePath, doCaseFolding, doStopping);
        }

        for (Integer i : queries.keySet()) {
            retrieveRanking(queries.get(i), i);
        }
    }

    private void createStemDocuments() {
        TextFileParser t = (TextFileParser) context.getBean("textFileParser");
        t.splitTextFile();
    }
}
