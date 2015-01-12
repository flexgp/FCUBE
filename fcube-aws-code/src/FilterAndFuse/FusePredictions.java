/**
 * Copyright (c) 2014 ALFA Group
 * 
 * Licensed under the MIT License.
 * 
 * See the "LICENSE" file for a copy of the license.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.  
 *
 * @author Ignacio Arnaldo
 * 
 */

package FilterAndFuse;

import java.io.IOException;

/**
 * Implements fitness evaluation for symbolic regression.
 * 
 */
public class FusePredictions {
    
    private String pathToCsv = "models/filteredMatrixTest.csv";
    private final CSVDataJava preds;
    private double FN_weight, FP_weight;
    
    /**
     * Create a new fitness operator, using the provided data, for assessing
     * individual solutions to Symbolic Regression problems. There is one
     * parameter for this fitness evaluation:
     * @param aFN_weight
     * @throws java.io.IOException
     */   
    public FusePredictions(double aFN_weight) throws IOException {
        preds = new CSVDataJava(pathToCsv);
        FN_weight = aFN_weight;
        FP_weight = 1 - FN_weight;
    }

    /**
     * Compute performance metrics of the majority vote of the classifiers
     */
    public void computeStatsMajorityVote() {
        
        
        int numClassifiers = preds.getNumFeatures();
        int numFitnessCasesTrain = preds.getNumExemplars();
        
        // COMPUTE COST FOR ALL CLASSIFIERS ON TRAIN SET
        double[][] predsTrainMatrix = preds.getInputValues();
        double[] trueLabels = preds.getTargetValues();

        double numPositiveTarget = 0;
        double numNegativeTarget = 0;
        double numPositivePrediction = 0;
        double numNegativePrediction = 0;
        double numFalsePositives = 0;
        double numFalseNegatives = 0;
        double numTruePositives = 0;
        double numTrueNegatives = 0;
        double accuratePredictions = 0;
        
        for(int i=0;i<numFitnessCasesTrain;i++){
            boolean val = false;
            int countPositives = 0;
            int countNegatives = 0;
            for(int j=0;j<numClassifiers;j++){
                if(predsTrainMatrix[i][j]==1){
                    countPositives++;
                }else{
                    countNegatives++;
                }
            }
            if(countPositives>=countNegatives){
                val = true;
            }

            boolean target = false;
            if(trueLabels[i]==1) target = true;
            if(val==true && target==true) {
                numPositivePrediction++;
                numPositiveTarget++;
                numTruePositives++;
                accuratePredictions++;
            }else if(val==true && target==false) {
                numPositivePrediction++;
                numNegativeTarget++;
                numFalsePositives++; 
            }else if(val==false && target==true){
                numNegativePrediction++;
                numPositiveTarget++;
                numFalseNegatives++;
            }else if(val==false && target==false){
                numNegativePrediction++;
                numNegativeTarget++;
                numTrueNegatives++;
                accuratePredictions++;
            }
        }
        double falsePositiveRate = numFalsePositives / numNegativeTarget;
        double falseNegativeRate = numFalseNegatives / numPositiveTarget;
        double cost = FN_weight*falseNegativeRate + FP_weight*falsePositiveRate;

        double accuracy = accuratePredictions / preds.getNumExemplars();
        double precision = numTruePositives / numPositivePrediction;
        double recall = numTruePositives / numPositiveTarget;
        double fscore = 2 * ( (precision*recall) / (precision + recall) );

        // cost , fpr, fnr, accuracy, precision, recall, F-score
        System.out.println("COST: " + cost);
        System.out.println("FP RATE: " + falsePositiveRate);
        System.out.println("FN RATE: " + falseNegativeRate);
        System.out.println("ACCURACY: " + accuracy);
        System.out.println("PRECISION: " + precision);
        System.out.println("RECALL: " + recall);
        System.out.println("F-SCORE: " + fscore);        
        
    }
    
    // fuse logistic regression
    //export LD_LIBRARY_PATH=LD_LIBRARY_PATH:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v717/runtime/glnxa64:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v717/bin/glnxa64:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v717/sys/os/glnxa64:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v717/sys/java/jre/glnxa64/jre/lib/amd64/native_threads:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v717/sys/java/jre/glnxa64/jre/lib/amd64/server:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v717/sys/java/jre/glnxa64/jre/lib/amd64
    //export XAPPLRESDIR=/usr/local/MATLAB/MATLAB_Compiler_Runtime/v717/X11/app-defaults
    
    /**
     * External call to matlab to fuse the predictions of the classifiers with
     * logistic regression
     * @throws IOException
     * @throws InterruptedException 
     */
    public void logisticRegressionMatlab() throws IOException, InterruptedException{
        String logreg_cmd = "export LD_LIBRARY_PATH=/usr/local/MATLAB/MATLAB_Compiler_Runtime/v717/runtime/glnxa64"
                + ":/usr/local/MATLAB/MATLAB_Compiler_Runtime/v717/bin/glnxa64:/usr/local/MATLAB/MATLAB_Compiler_Runtime/v717/sys/os/glnxa64"
                + ":/usr/local/MATLAB/MATLAB_Compiler_Runtime/v717/sys/java/jre/glnxa64/jre/lib/amd64/native_threads"
                + ":/usr/local/MATLAB/MATLAB_Compiler_Runtime/v717/sys/java/jre/glnxa64/jre/lib/amd64/server"
                + ":/usr/local/MATLAB/MATLAB_Compiler_Runtime/v717/sys/java/jre/glnxa64/jre/lib/amd64 ; "
                + "export XAPPLRESDIR=/usr/local/MATLAB/MATLAB_Compiler_Runtime/v717/X11/app-defaults ; "
                + "/usr/share/tomcat7/logreg > matlab.log & ";
        System.out.println(logreg_cmd);
        Process logreg_process = Runtime.getRuntime().exec(new String[]{"bash","-c",logreg_cmd});        
        logreg_process.waitFor();
    }


}