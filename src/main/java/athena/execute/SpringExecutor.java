package athena.execute;

import athena.index.InvertedIndexer;
import athena.queryexpansion.PsFeedback;
import athena.utils.CommonUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

public class SpringExecutor {
    public static void main(String[] args) {
        SpringExecutor executor = new SpringExecutor();
        //executor.executeIndex();
        executor.executeExpand();
    }

    public void executeIndex() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/bean.xml");
        CommonUtils commonUtils = (CommonUtils) context.getBean("commonUtils");
        long startTime = commonUtils.printTimeStamp("Index Creation Started");
        InvertedIndexer indexer = (InvertedIndexer) context.getBean("invertedIndexer");
        ConfigurableEnvironment environment = context.getEnvironment();
        environment.addActiveProfile("default");
        context.setEnvironment(environment);
        context.refresh();
        //RetrievalModel model = (BM25) context.getBean("bm25");

        String inputFolder = commonUtils.getResourcePath() + "cacm\\";
        indexer.setIndexFolder(commonUtils.getResourcePath() + "athena\\");
        indexer.createIndex(inputFolder);
        long stopTime = commonUtils.printTimeStamp("Index Creation Completed");
        commonUtils.printTotalTime(startTime, stopTime);
        context.close();
    }

    public void executeExpand() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/bean.xml");
        Properties properties = (Properties) context.getBean("searchEngineProperties");
        CommonUtils commonUtils = (CommonUtils) context.getBean("commonUtils");

        ConfigurableEnvironment environment = context.getEnvironment();
        environment.addActiveProfile("default");
        String retrievalModel = properties.getProperty("search.engine.retrievalmodel");
        if (retrievalModel.equals("tfidf")) {
            environment.addActiveProfile("tfidf");
            System.out.println("Using RetrievalModel : " + retrievalModel);

        } else if (retrievalModel.equals("bm25")) {
            System.out.println("Using RetrievalModel : " + retrievalModel);
        } else {
            System.out.println("Using Default RetrievalModel : BM25");
        }

        context.setEnvironment(environment);
        context.refresh();

        long startTime = commonUtils.printTimeStamp("Query Expansion Started");
        InvertedIndexer indexer = (InvertedIndexer) context.getBean("invertedIndexer");
        indexer.setIndexFolder(commonUtils.getResourcePath() + "athena\\");

        PsFeedback psFeedback = (PsFeedback) context.getBean("psFeedback");
        System.out.println(psFeedback.expandQuery("samelson"));
        long stopTime = commonUtils.printTimeStamp("Query Expansion Completed");
        commonUtils.printTotalTime(startTime, stopTime);
        context.close();

    }
}
