package athena.snippetgeneration;

import athena.utils.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Pallav on 4/17/2017.
 */
public class SnippetGeneration {

    @Autowired
    private CommonUtils commonUtils;
    String fs = File.separator;

    public void extractQueries(String folderPath) {
        folderPath = commonUtils.getOutputPath() + fs + folderPath;
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        if (files == null) {
            System.out.println("No files present or invalid folder");
        } else {
            for (File file : files) {
                List<String> lines = commonUtils.getLinesFromFile(folderPath + fs + file.getName());
                for(int i=0; i<lines.size(); i++) {
                    String[] tuple;
                    tuple = lines.get(i).split(" ");
                    String docId = tuple[2];
                    snippetProcessing(docId);
                }
            }
        }
    }

    public void snippetProcessing(String docId) {
        String filePath = commonUtils.getResourcePath() + "\\Athena\\DataFiles\\" + docId + ".txt";
        String fileContent = commonUtils.getTextFromFile(filePath);

    }

    public String thisSnippet(Integer queryID, String s, String query){
        String filePath = commonUtils.getResourcePath() +
                "\\Athena\\DataFiles\\" + s + ".txt";
        List<String> lines = commonUtils.getLinesFromFile(filePath);
        String snip = "";
        snip = snip.concat(s);
        return snip;

    }



}
