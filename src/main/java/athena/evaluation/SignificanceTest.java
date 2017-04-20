package athena.evaluation;

import athena.utils.CommonUtils;
import org.apache.commons.math3.stat.inference.TTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * Created by Harshit on 4/20/2017.
 */

public class SignificanceTest {


    private static CommonUtils commonUtils = new CommonUtils();

    public static double performTTest(String a, String b){
        TTest obj = new TTest();


        String resultPath_A;
        String resultPath_B;
        String fs = File.separator;
        Double t = 0.0;
        Double B_A = 0.0;
        Double mean_B_A = 0.0;
        Double rootN;
        Double sd = 0.0;
        resultPath_A = commonUtils.getOutputPath() + fs + a + fs + "eval_results" + fs + "query_pr.txt";
        List<String> lines_A = commonUtils.getLinesFromFile(resultPath_A);
        resultPath_B = commonUtils.getOutputPath() + fs + b + fs + "eval_results" + fs + "query_pr.txt";
        List<String> lines_B = commonUtils.getLinesFromFile(resultPath_B);
        rootN = Math.sqrt(lines_A.size());
        if(lines_A.size() == lines_B.size()){
            for(int i=0; i<lines_A.size(); i++){
                B_A = Double.parseDouble(lines_B.get(i)) - Double.parseDouble(lines_A.get(i));
                mean_B_A += B_A;
            }
            mean_B_A = mean_B_A / lines_A.size();
            for(int i=0; i<lines_A.size(); i++){
                B_A = Double.parseDouble(lines_B.get(i)) - Double.parseDouble(lines_A.get(i));
                sd += (B_A - mean_B_A)*(B_A - mean_B_A);
            }
            sd = sd/lines_A.size();
            sd = Math.sqrt(sd);

            t = mean_B_A/sd * rootN;

        }

        return t;
    }
}
