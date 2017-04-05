package athena.execute;

import athena.index.InvertedIndexer;
import athena.test.TestProperties;
import athena.utils.CommonUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringExecutor {
    public static void main(String[] args) {
        System.out.println("Hello World");
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/bean.xml");
        TestProperties properties = (TestProperties) context.getBean("testProperties");
        properties.printProperties();

        CommonUtils commonUtils = new CommonUtils();
        long startTime = commonUtils.printTimeStamp("Index Creation Started");
        String inputFolder = "C:\\Lucifer\\IndexFiles";
        InvertedIndexer indexer = (InvertedIndexer) context.getBean("invertedIndexer", "C:\\Lucifer\\Test\\");
        indexer.createIndex(inputFolder);
        long stopTime = commonUtils.printTimeStamp("Index Creation Completed");
        commonUtils.printTotalTime(startTime, stopTime);
        context.close();
    }
}
