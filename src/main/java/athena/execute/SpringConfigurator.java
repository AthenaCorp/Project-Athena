package athena.execute;

import athena.TextFileParser;
import athena.evaluation.EffectivenessEvaluation;
import athena.index.InvertedIndexer;
import athena.queryexpansion.PseudoRelevanceFeedback;
import athena.retrievalmodel.RetrievalModel;
import athena.retrievalmodel.RetrievalModels;
import athena.utils.CommonUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

public class SpringConfigurator {
    private Properties properties;
    private ClassPathXmlApplicationContext context;

    public SpringConfigurator() {
        context = new ClassPathXmlApplicationContext("spring/bean.xml");
        properties = (Properties) context.getBean("searchEngineProperties");
    }

    public void generateIndex() {
        CommonUtils commonUtils = (CommonUtils) context.getBean("commonUtils");
        long startTime = commonUtils.printTimeStamp("Index Creation Started");
        InvertedIndexer indexer = (InvertedIndexer) context.getBean("invertedIndexer");
        String resourceFolder = commonUtils.getResourcePath();
        String inputFolder;
        if(Boolean.getBoolean(properties.getProperty("search.engine.enable.stemming"))){
            inputFolder = resourceFolder + properties.getProperty("search" +
                    ".engine.steminput.folder") + "\\";
        }
        else {
            inputFolder = resourceFolder + properties.getProperty("search.engine.input.folder") + "\\";
        }
        String indexFolder = resourceFolder + properties.getProperty("search.engine.index.folder") + "\\";
        indexer.setIndexFolder(indexFolder);
        indexer.createIndex(inputFolder);
        long stopTime = commonUtils.printTimeStamp("Index Creation Completed");
        commonUtils.printTotalTime(startTime, stopTime);
    }

    public void retrieveRanking(String query, Integer count, Integer queryID) {
        CommonUtils commonUtils = (CommonUtils) context.getBean("commonUtils");
        long startTime = commonUtils.printTimeStamp("Ranking Started");

        InvertedIndexer indexer = (InvertedIndexer) context.getBean("invertedIndexer");
        String resourceFolder = commonUtils.getResourcePath();
        String indexFolder = resourceFolder + properties.getProperty("search.engine.index.folder") + "\\";
        indexer.setIndexFolder(indexFolder);
        setRetrievalModel();
        RetrievalModel retrievalModel = (RetrievalModel) context.getBean("retrievalModel");

        if(properties.getProperty("search.engine.enable.query.expansion").equals("false")) {
            RetrievalModels.printN(retrievalModel.getRanking(query), count, queryID, retrievalModel.getModelName());
        } else {
            PseudoRelevanceFeedback feedback = (PseudoRelevanceFeedback) context.getBean("pseudoRelevanceFeedback");
            RetrievalModels.printN(feedback.getRanking(query), count, queryID, retrievalModel.getModelName());
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
        System.out.println(feedback.expandQuery("samelson"));
        long stopTime = commonUtils.printTimeStamp("Query Expansion Completed");
        commonUtils.printTotalTime(startTime, stopTime);
    }

    public ClassPathXmlApplicationContext getContext() {
        return context;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setRetrievalModel() {
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

    public void execStemFile(String filename){
        TextFileParser t = (TextFileParser) context.getBean("textFileParser");
        t.splitTextFile(filename);
    }

    public void execEval(){
        EffectivenessEvaluation e = (EffectivenessEvaluation) context.getBean
                ("effectivenessEvaluation");
        //e.meanAveragePrecision("Task2_1.txt");
        System.out.println(e.meanAveragePrecision("folderpath"));
    }
}
