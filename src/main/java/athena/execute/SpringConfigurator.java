package athena.execute;

import athena.index.InvertedIndexer;
import athena.queryexpansion.PsFeedback;
import athena.utils.CommonUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

public class SpringConfigurator {
    private Properties properties;
    private ClassPathXmlApplicationContext context;

    public SpringConfigurator() {
        context = new ClassPathXmlApplicationContext("spring/bean.xml");
        ConfigurableEnvironment environment = context.getEnvironment();
        properties = (Properties) context.getBean("searchEngineProperties");
        environment.addActiveProfile("default");
        String retrievalModel = properties.getProperty("search.engine.retrieval.model");
        switch (retrievalModel.toLowerCase()) {
            case "tfidf":
                environment.addActiveProfile("tfidf");
                System.out.println("Using RetrievalModel : " + retrievalModel);
                break;
            case "bm25":
                System.out.println("Using RetrievalModel : " + retrievalModel);
                break;
            default:
                System.out.println("Using Default RetrievalModel : BM25");
                break;
        }
        context.setEnvironment(environment);
        context.refresh();
    }


    public void executeIndex() {
        CommonUtils commonUtils = (CommonUtils) context.getBean("commonUtils");
        long startTime = commonUtils.printTimeStamp("Index Creation Started");
        InvertedIndexer indexer = (InvertedIndexer) context.getBean("invertedIndexer");
        String resourceFolder = commonUtils.getResourcePath();
        String inputFolder = resourceFolder + properties.getProperty("search.engine.input.folder") + "\\";
        String indexFolder = resourceFolder + properties.getProperty("search.engine.index.folder") + "\\";
        indexer.setIndexFolder(indexFolder);
        indexer.createIndex(inputFolder);
        long stopTime = commonUtils.printTimeStamp("Index Creation Completed");
        commonUtils.printTotalTime(startTime, stopTime);
    }

    public void executeExpand() {
        CommonUtils commonUtils = (CommonUtils) context.getBean("commonUtils");
        long startTime = commonUtils.printTimeStamp("Query Expansion Started");
        InvertedIndexer indexer = (InvertedIndexer) context.getBean("invertedIndexer");
        indexer.setIndexFolder(commonUtils.getResourcePath() + "athena\\");

        PsFeedback psFeedback = (PsFeedback) context.getBean("psFeedback");
        System.out.println(psFeedback.expandQuery("samelson"));
        long stopTime = commonUtils.printTimeStamp("Query Expansion Completed");
        commonUtils.printTotalTime(startTime, stopTime);
    }

    public ClassPathXmlApplicationContext getContext() {
        return context;
    }

    public Properties getProperties() {
        return properties;
    }
}
