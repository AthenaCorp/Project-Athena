package athena.snippetgeneration;

import athena.utils.CommonUtils;
import athena.utils.SearchEngineUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static athena.retrievalmodel.RetrievalModels.sortBM;

/**
 * Created by Pallav on 4/17/2017.
 */
public class SnippetGeneration {

    private CommonUtils commonUtils = new CommonUtils();
    private String fs = File.separator;

//    public void extractQueries(String folderPath) {
//        folderPath = commonUtils.getOutputPath() + fs + folderPath;
//        File folder = new File(folderPath);
//        File[] files = folder.listFiles();
//        if (files == null) {
//            System.out.println("No files present or invalid folder");
//        } else {
//            for (File file : files) {
//                List<String> lines = commonUtils.getLinesFromFile(folderPath + fs + file.getName());
//                for(int i=0; i<lines.size(); i++) {
//                    String[] tuple;
//                    tuple = lines.get(i).split(" ");
//                    String docId = tuple[2];
//                    snippetProcessing(docId);
//                }
//            }
//        }
//    }
//
//    public void snippetProcessing(String docId) {
//        String filePath = commonUtils.getResourcePath() + "\\Athena\\DataFiles\\" + docId + ".txt";
//        String fileContent = commonUtils.getTextFromFile(filePath);
//
//    }

    public String thisSnippet(String docName, String query){
        String[] queryArray = query.split(" ");
        query = SearchEngineUtils.stoppedText(query);
        //System.out.println(query);
        //query = commonUtils.stoppedText(query);
        //System.out.println(query);
        //System.exit(1);
        String filePath = commonUtils.getResourcePath() +
                "\\cacm\\" + docName + ".html";
        List<String> lines = commonUtils.getLinesFromFile(filePath);
        HashMap<String, Double> sigMap = new HashMap<>();
        for(int i=0; i<lines.size(); i++) {
            String thisLine = lines.get(i);
            if(thisLine.length() == 0){
                continue;
            }
            thisLine = thisLine.replace(",","");
            thisLine = thisLine.replace(".","");
            thisLine = thisLine.replace("\n","");
            double sig = 0.0;
            String snippet1 = "";
            String[] lineArray;
            lineArray = thisLine.split(" ");
            for (String word: lineArray) {
                if(query.contains(word)){
                    sig += 1;
                    snippet1 = snippet1.concat(word.toUpperCase()+" ");
                }else{
                    snippet1 = snippet1.concat(word+" ");
                }
            }
            sig = sig * sig / lineArray.length;
            sigMap.put(snippet1,sig);
        }
        String snippet = "";
        int count = 0;
        sigMap = sortBM(sigMap);
        for (String snip: sigMap.keySet()) {
            snippet = snippet.concat(snip+"\n");
            count+= 1;
            if(count == 4){
                snippet = snippet.concat("\n");
                break;
            }
        }
        return snippet;
    }



}
