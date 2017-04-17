package athena.snippetgeneration;

import athena.evaluation.EffectivenessEvaluation;
import athena.utils.CommonUtils;
import athena.utils.SearchEngineUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Pallav on 4/17/2017.
 */
public class SnippetGeneration {

    @Autowired
    private CommonUtils commonUtils;

    public void extractQueries(String folderPath) {
        Map<Integer, String> queries = SearchEngineUtils.getQuerySet(commonUtils.getResourcePath()
                + "query\\cacm.query.txt");
        for (int i = 1; i <= queries.size(); i++) {
            ArrayList<String> lstOfQuery = new ArrayList<>(Arrays.asList(queries.get(i).split(" ")));
//            System.out.println(lstOfQuery);
            snippetProcessing(lstOfQuery, folderPath);
        }
    }

    public void snippetProcessing(ArrayList<String> lstOfQ, String folderPath) {
        String[] tuple;
        String fs = File.separator;
        folderPath = commonUtils.getOutputPath() + fs + folderPath;
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        for (File file : files) {
            List<String> lines = commonUtils.getTextFromFile(folderPath + fs + file.getName());
            tuple = lines.get(0).split(" ");
            snippetGeneration(tuple, lstOfQ);
        }
    }

    public void snippetGeneration(String[] tuple, ArrayList<String> lstOfQ) {
        String
    }

}
