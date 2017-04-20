package athena.evaluation;

import athena.utils.CommonUtils;
import athena.utils.SearchEngineUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class EffectivenessEvaluation {
    @Autowired
    private CommonUtils commonUtils;

    @Value("${search.engine.precision.k}")
    private String precisionKs;

    private List<Integer> kList;

    public void evaluation(String folderPath) {
        double count = 0.0;
        double totalPrecision = 0.0;
        double totalReciprocal = 0.0;
        double mean;
        String fs = File.separator;
        String result = "";
        folderPath = commonUtils.getOutputPath() + fs + folderPath;
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        calculatePAtK(folderPath);
        if (files == null) {
            System.out.println("No files present or invalid folder");
        } else {
            for (File file : files) {
                List<String> lines = commonUtils.getLinesFromFile(folderPath + fs + file.getName());
                if ((lines.size() != 0) && (!file.getName().equals("pAtK.txt"))) {
                    result= result + averagePrecision(lines) + "\n";
                    totalPrecision += averagePrecision(lines);
                    totalReciprocal += reciprocalRank(lines);
                    precisionRecallValues(lines, folderPath);
                } else {
                    totalPrecision += 0;
                    totalReciprocal += 0;
                }

                count++;
            }
        }
        String resultFolderPath = folderPath.concat("\\eval_results");
        commonUtils.writeToFile(resultFolderPath + "\\" + "query_pr.txt", result);
        mean = totalPrecision / count;
        System.out.println("Mean average precision: " + mean);
        mean = totalReciprocal / count;
        System.out.println("Mean Reciprocal Rank: " + mean);
        //calculatePAtK(folderPath);
        System.out.println("Query-by-query precision/recall values printed in [qid]_prvalues.txt");
    }

    private double reciprocalRank(List<String> lines) {
        double count = 0.0;
        String[] tuple;
        tuple = lines.get(0).split(" ");
        ArrayList<String> relevantDocs = SearchEngineUtils.getRelevance(Integer.parseInt(tuple[0]));
        for (String line : lines) {
            tuple = line.split(" ");
            count += 1.0;
            if (relevantDocs.contains(tuple[2])) {
                return 1.0 / count;
            }
        }
        return 0.0;
    }

    private double averagePrecision(List<String> lines) {
        double mean = 0.0;
        double count = 0.0;
        double relevantCount = 0.0;
        double totalPrecision = 0.0;
        String[] tuple;
        tuple = lines.get(0).split(" ");
        ArrayList<String> relevantDocs = SearchEngineUtils.getRelevance(Integer.parseInt(tuple[0]));
        for (String line : lines) {
            tuple = line.split(" ");
            count += 1.0;
            if (relevantDocs.contains(tuple[2])) {
                relevantCount += 1.0;
                totalPrecision += (relevantCount / count);
            }

        }
        if (relevantCount != 0.0) {
            mean = totalPrecision / relevantCount;
        }

        return mean;
    }

    private ArrayList<Double> listOfPrecision(List<String> lines) {
        ArrayList<Double> listOfPrecision = new ArrayList<>();
        double count = 0.0;
        double relevantCount = 0.0;
        double totalPrecision;
        String[] tuple;
        tuple = lines.get(0).split(" ");
        ArrayList<String> relevantDocs = SearchEngineUtils.getRelevance(Integer.parseInt(tuple[0]));
        for (String line : lines) {
            tuple = line.split(" ");
            count += 1.0;
            if (relevantDocs.contains(tuple[2])) {
                relevantCount += 1.0;
            }
            totalPrecision = (relevantCount / count);
            listOfPrecision.add(totalPrecision);
        }
        return listOfPrecision;
    }

    private ArrayList<Double> listOfRecall(List<String> lines) {
        ArrayList<Double> listOfRecall = new ArrayList<>();
        double relevantCount = 0.0;
        double totalRecall;
        String[] tuple;
        tuple = lines.get(0).split(" ");
        ArrayList<String> relevantDocs = SearchEngineUtils.getRelevance(Integer.parseInt(tuple[0]));
        for (String line : lines) {
            tuple = line.split(" ");
            if (relevantDocs.contains(tuple[2])) {
                relevantCount += 1.0;
            }
            totalRecall = (relevantCount / relevantDocs.size());
            listOfRecall.add(totalRecall);
        }
        return listOfRecall;
    }

    private void precisionRecallValues(List<String> lines, String folderPath) {
        ArrayList<Double> lop = listOfPrecision(lines);
        ArrayList<Double> lor = listOfRecall(lines);
        String print = "";
        for (int i = 0; i < lop.size(); i++) {
            print = print.concat(lop.get(i) + " " + lor.get(i) + "\n");
        }
        String[] tuple;
        tuple = lines.get(0).split(" ");
        String resultFolderPath = folderPath.concat("\\eval_results");
        commonUtils.writeToFile(resultFolderPath + "\\" + tuple[0] + "_prvalues.txt", print);
    }

    private void calculatePAtK(String folderPath) {
        String fs = File.separator;
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        String line = "";
        String resultFolderPath = folderPath.concat("\\eval_results");
        commonUtils.verifyFolder(resultFolderPath);
        if (files == null) {
            System.out.println("No files present or invalid folder");
        } else {
            for (File file : files) {
                List<String> lines = commonUtils.getLinesFromFile(folderPath + fs + file.getName());
                if (lines.size() != 0) {
                    String[] tuple;
                    tuple = lines.get(0).split(" ");
                    String queryId = tuple[0];
                    ArrayList<Double> lstOfPrecision = listOfPrecision(lines);
                    line = line.concat(pAtK(lstOfPrecision, queryId));
                }

            }
            commonUtils.writeToFile(resultFolderPath + "\\" + "pAtK.txt", line);
            commonUtils.convertDocToCSV(resultFolderPath + "\\" + "pAtK.txt");
            System.out.println("Precision at K " + getKList() + " printed in pAtK.txt\n");
        }

    }



    private String pAtK(ArrayList<Double> precValues, String qId) {
        Double pAtk;
        List<Integer> kList = getKList();
        StringBuilder lineBuilder = new StringBuilder(qId);
        for (Integer k : kList) {
            if (precValues.size() >= k) {
                pAtk = precValues.get(k - 1);
            } else {
                pAtk = 0.0;
            }
            lineBuilder.append(" ").append(pAtk);
        }
        return lineBuilder.toString() + "\n";
    }

    private List<Integer> getKList() {
        if (kList == null) {
            String[] kStrings = precisionKs.split(",");
            kList = new ArrayList<>();
            for (String s : kStrings) {
                kList.add(Integer.parseInt(s.trim()));
            }
        }
        return kList;
    }
}
