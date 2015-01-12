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
 * Evaluates the cost of the retrieved classifiers
 * It assumes that no variable factoring was performed
 * @author Ignacio Arnaldo
 */
public class CostAllCIM {
    
    String pathToMatrix;
    private CSVDataJava preds;
    private double FN_weight, FP_weight, majorityCost;
    private int majorityClass;
    
    /**
     * Create a new fitness operator, using the provided data, for assessing
     * individual solutions to Symbolic Regression problems. There is one
     * parameter for this fitness evaluation:
     * @param aFN_weight
     * @param aPathToMatrix
     */
    public CostAllCIM(double aFN_weight, String aPathToMatrix) {
        FN_weight = aFN_weight;
        FP_weight = 1 - FN_weight;
        majorityClass = 0;
        majorityCost = 0;
        pathToMatrix = aPathToMatrix;
    }
    
    /**
     * Set the baseline accuracy to be that of always predicting the majority class
     * @throws IOException 
     */
    public void setMajorityVote() throws IOException{
        preds = new CSVDataJava(pathToMatrix);
        double[] trueLabels = preds.getTargetValues();
        int counter0 = 0;
        int counter1 = 0;
        for(int i=0;i<preds.getNumExemplars();i++){
            if(trueLabels[i]==0){
                counter0++;
            }else{
                counter1++;
            }
        }
        majorityClass = 1;
        if(counter0>counter1){
            majorityClass = 0;
        }
    }
    
    /**
    * Determines the cost of always predicting the majority class 
    */
    public void setMajorityCost(){
        int numFitnessCasesTrain = preds.getNumExemplars();
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
        boolean val = false;
        if(majorityClass==1){
                val = true;
        }
        for(int i=0;i<numFitnessCasesTrain;i++){
            boolean target = false;
            if((int)trueLabels[i]==1) {
                target = true;
            }
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

        majorityCost = FN_weight*falseNegativeRate + FP_weight*falsePositiveRate;

    }

    /**
     * Computes the cost of all the models
     */
    public void computeCostAll() {
        
        int numClassifiers = preds.getNumFeatures();
        int numFitnessCasesTrain = preds.getNumExemplars();
        // | cost | fp | fn |
        double[][] costs = new double[numClassifiers][3];
        
        double[][] predsTrainMatrix = preds.getInputValues();
        double[] trueLabels = preds.getTargetValues();
        for(int j=0;j<numClassifiers;j++){
            double numPositiveTarget = 0;
            double numNegativeTarget = 0;
            double numFalsePositives = 0;
            double numFalseNegatives = 0;
            for(int i=0;i<numFitnessCasesTrain;i++){
                boolean val = false;
                if(predsTrainMatrix[i][j]==1){
                    val = true;
                }
                boolean target = false;
                if(trueLabels[i]==1) target = true;
                if(val==true && target==true) {
                    numPositiveTarget++;
                }else if(val==true && target==false) {
                    numNegativeTarget++;
                    numFalsePositives++; 
                }else if(val==false && target==true){
                    numPositiveTarget++;
                    numFalseNegatives++;
                }else if(val==false && target==false){
                    numNegativeTarget++;
                }
            }
            double falsePositiveRate = numFalsePositives / numNegativeTarget;
            double falseNegativeRate = numFalseNegatives / numPositiveTarget;
            double cost = FN_weight*falseNegativeRate + FP_weight*falsePositiveRate;
            costs[j][0] = cost;
            costs[j][1] = falsePositiveRate;
            costs[j][2] = falseNegativeRate;
        }
        for(int j=0;j<numClassifiers;j++){
            System.out.println(costs[j][0] + "," + costs[j][1] + "," + costs[j][2] );
        }
    }

}