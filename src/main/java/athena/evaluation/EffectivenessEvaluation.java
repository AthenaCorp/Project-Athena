package athena.evaluation;

import athena.crawler.CrawlerUtils;
import athena.index.InvertedIndexer;
import athena.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Component

public class EffectivenessEvaluation {

    @Autowired
    private CrawlerUtils crawlerUtils;
    @Autowired
    private CommonUtils commonUtils;
    @Autowired
    private InvertedIndexer invertedIndexer;

    public ArrayList<ArrayList> getAllQueries(String filename){
        List<String> lines = getTextFromFile(filename);
        String[] tuple;
        ArrayList<String> relevantDocs;
        int count = 0;
        for (String line : lines) {
            tuple = line.split(" ");
            if(Integer.parseInt(tuple[0])!=count){
                count = Integer.parseInt(tuple[0]);
                relevantDocs = getRelevance(count);
            }
        }
        Double precision = 0.0;
        Double recall;

        System.out.println(precision);
    }

    public ArrayList<String> getRelevance (int queryNumber){
        String[] tuple;
        String filename = commonUtils.getResourcePath() + "//query//cacm.rel";
        List<String> lines = getTextFromFile(filename);
        ArrayList<String > relevantDocs = new ArrayList<>();
        for (String line : lines) {
            tuple = line.split(" ");
            if(Integer.parseInt(tuple[0]) == queryNumber){
                relevantDocs.add(tuple[2]);
            }
        }
        return relevantDocs;
    }

    public List<String> getTextFromFile(String filename){
        List<String> lines = null;
        try {
            lines = Files.readAllLines (Paths.get (filename),
                    Charset.forName ("UTF-8"));
        }
        catch (IOException e) {
            System.out.print ("IO Exception reading from file: ");
            System.out.println (filename);
            System.out.println (e);
        }
        return lines;
    }
}
