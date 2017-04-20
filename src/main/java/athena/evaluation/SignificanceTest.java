package athena.evaluation;

import athena.utils.CommonUtils;
import java.io.File;
import java.util.List;

public class SignificanceTest {


    private static CommonUtils commonUtils = new CommonUtils();

    public static double performTTest(String a, String b){
        String resultPath_A;
        String resultPath_B;
        String fs = File.separator;
        double t = 0.0;
        double B_A;
        double mean_B_A = 0.0;
        double N;
        double rootN;
        double sd = 0.0;
        resultPath_A = commonUtils.getOutputPath() + fs + a + fs + "eval_results" + fs + "query_pr.txt";
        List<String> lines_A = commonUtils.getLinesFromFile(resultPath_A);
        resultPath_B = commonUtils.getOutputPath() + fs + b + fs + "eval_results" + fs + "query_pr.txt";
        List<String> lines_B = commonUtils.getLinesFromFile(resultPath_B);

        if(lines_A.size() == lines_B.size()){
            N = (double)lines_A.size();
            rootN = Math.sqrt(N);
            for(int i=0; i<lines_A.size(); i++){
                B_A = Double.parseDouble(lines_B.get(i)) - Double.parseDouble(lines_A.get(i));
                mean_B_A += B_A;
            }
            mean_B_A = mean_B_A / N;
            System.out.println(mean_B_A);
            for(int i=0; i<lines_A.size(); i++){
                B_A = Double.parseDouble(lines_B.get(i)) - Double.parseDouble(lines_A.get(i));
                sd += (B_A - mean_B_A)*(B_A - mean_B_A);
            }
            sd = Math.sqrt(sd/N);
            System.out.println(sd);

            t = mean_B_A / sd * rootN;
        }
        return t;
    }
}
