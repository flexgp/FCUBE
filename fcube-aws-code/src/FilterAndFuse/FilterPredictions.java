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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * Discard the predictions of classifiers worse than a naive classifier that always
 * predicts the majority class.
 * 
 * @author Ignacio Arnaldo
 */
public class FilterPredictions {
    
    String pathToPredsTrain = "models/predictionsMatrixTrain.csv";
    String pathToPredsTest = "models/predictionsMatrixTest.csv";
    String pathToFilterTrain = "models/filteredMatrixTrain.csv";
    String pathToFilterTest = "models/filteredMatrixTest.csv";
    String pathToIndices = "models/filteredIndices.csv";
    private CSVDataJava predsTrain, predsTest;
    private double FN_weight, FP_weight, majorityCost;
    private int majorityClass;
    boolean[] filteredClassifiers; // a boolean flag that determines whether the classifier passes the filter (true) or is discarded (false)
    private String dataPathTrain, dataPathTest, model;
    
    
    /**
     * Create a new fitness operator, using the provided data, for assessing
     * individual solutions to Symbolic Regression problems. There is one
     * parameter for this fitness evaluation:
     * @param aDataTrainPath
     * @param aDataTestPath
     * @param aModel
     * @param aFN_weight
     */
    public FilterPredictions(String aDataTrainPath, String aDataTestPath, String aModel,double aFN_weight) {
        dataPathTrain = aDataTrainPath;
        dataPathTest = aDataTestPath;
        model = aModel;
        FN_weight = aFN_weight;
        FP_weight = 1 - FN_weight;
        majorityClass = 0;
        majorityCost = 0;
    }
    
    /**
     * Obtain the matrix of the predictions, one line per exemplar and one column per classifier
     * @throws IOException
     * @throws InterruptedException 
     */
    public void getPredictionsMatrices() throws IOException, InterruptedException{
        // prepare csv file with targets
        CSVDataJava trainSet = new CSVDataJava(dataPathTrain);
        double[] targetsTrain = trainSet.getTargetValues();
        String pathTargetsTrain = "models/targetsTrain.csv";
        BufferedWriter bwTrain = new BufferedWriter(new FileWriter(pathTargetsTrain));
        PrintWriter printWriterTr = new PrintWriter(bwTrain);
        for(int i=0;i<targetsTrain.length;i++){
            printWriterTr.println(targetsTrain[i]);
        }
        printWriterTr.flush();
        printWriterTr.close();
        
        // paste predictions
        String pasteTrain_command = "paste models/*/*/predsTrain_" + model + "* -d',' > models/predsTrain.csv ; "
                + "paste -d',' models/predsTrain.csv models/targetsTrain.csv > models/predictionsMatrixTrain.csv";
        Process pasteTrain_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", pasteTrain_command});
        pasteTrain_process.waitFor();
        
        CSVDataJava testSet = new CSVDataJava(dataPathTest);
        double[] targetsTest = testSet.getTargetValues();
        String pathTargetsTest = "models/targetsTest.csv";
        BufferedWriter bwTest = new BufferedWriter(new FileWriter(pathTargetsTest));
        PrintWriter printWriterTe = new PrintWriter(bwTest);
        for(int i=0;i<targetsTest.length;i++){
            printWriterTe.println(targetsTest[i]);
        }
        printWriterTe.flush();
        printWriterTe.close();
        
        // paste predictions
        String pasteTest_command = "paste models/*/*/predsTest_" + model + "* -d',' > models/predsTest.csv ; "
                + "paste -d',' models/predsTest.csv models/targetsTest.csv > models/predictionsMatrixTest.csv";
        Process pasteTest_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", pasteTest_command});
        pasteTest_process.waitFor();
    }

    /**
     * Set default vote to the majority class
     * @throws IOException 
     */
    public void setMajorityVote() throws IOException{
        predsTrain = new CSVDataJava(pathToPredsTrain);
        predsTest = new CSVDataJava(pathToPredsTest);
        int numClassifiers = predsTest.getNumFeatures();
        filteredClassifiers = new boolean[numClassifiers];

        double[] trueLabels = predsTrain.getTargetValues();
        int counter0 = 0;
        int counter1 = 0;
        for(int i=0;i<predsTrain.getNumExemplars();i++){
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
        System.out.println("MAJORITY CLASS IS: " + majorityClass);
    }
        
    /**
     * Set the cost of the naive classifier that always predicts the majority class
     */
    public void setMajorityCost(){
        int numFitnessCasesTrain = predsTrain.getNumExemplars();
        double[] trueLabels = predsTrain.getTargetValues();
        
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
        System.out.println("npt: " + numPositiveTarget);
        System.out.println("nnt: " + numNegativeTarget);
        majorityCost = FN_weight*falseNegativeRate + FP_weight*falsePositiveRate;
        System.out.println("MAJORITY CLASS DUMMY CLASSIFIER IS: " + majorityCost);
    }
  
    /**
     * Filter models that are worse than a naive classifier that always
     * predicts the majority class
     */
    public void filterModels() {
        
        int numClassifiers = predsTrain.getNumFeatures();
        int numFitnessCasesTrain = predsTrain.getNumExemplars();

        double[][] predsTrainMatrix = predsTrain.getInputValues();
        double[] trueLabels = predsTrain.getTargetValues();
        
        for(int j=0;j<numClassifiers;j++){
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
                if(predsTrainMatrix[i][j]==1){
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
            if(cost<majorityCost){
                filteredClassifiers[j] = true;
                System.out.println("MODEL " + j + "\t cost " + cost + " < dummy classifier --> survives");
            }else{
                filteredClassifiers[j] = false;
                System.out.println("MODEL " + j + "\t cost: " + cost + " > dummy classifier --> discarded");
            }
        }
    }

    /**
     * Compute the cost of all the classifiers on train and test data
     */
    public void computeCostAll() {
        
        int numClassifiers = predsTrain.getNumFeatures();
        int numFitnessCasesTrain = predsTrain.getNumExemplars();
        // | cost on train | cost on test | fp train | fp test | fn train | fn test
        double[][] costs = new double[numClassifiers][6];
        
        // COMPUTE COST FOR ALL CLASSIFIERS ON TRAIN SET
        double[][] predsTrainMatrix = predsTrain.getInputValues();
        double[] trueLabels = predsTrain.getTargetValues();
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
            costs[j][2] = falsePositiveRate;
            costs[j][4] = falseNegativeRate;
        }
        
        // COMPUTE COST FOR ALL CLASSIFIERS ON TEST SET
        double[][] predsTestMatrix = predsTest.getInputValues();
        double[] trueTest = predsTest.getTargetValues();
        int numFitnessCasesTest = predsTest.getNumExemplars();
        for(int j=0;j<numClassifiers;j++){
            double numPositiveTarget = 0;
            double numNegativeTarget = 0;
            double numFalsePositives = 0;
            double numFalseNegatives = 0;
            for(int i=0;i<numFitnessCasesTest;i++){
                boolean val = false;
                if(predsTestMatrix[i][j]==1){
                    val = true;
                }
                boolean target = false;
                if(trueTest[i]==1) target = true;
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
            costs[j][1] = cost;
            costs[j][3] = falsePositiveRate;
            costs[j][5] = falseNegativeRate;
        }
        
        for(int j=0;j<numClassifiers;j++){
            System.out.println(costs[j][0] + "," + costs[j][1] + "," + costs[j][2] + "," + costs[j][3] + "," + costs[j][4] + "," + costs[j][5]);
        }
    }
    
    /**
     * Compute the cost of the filtered classifiers on train and test data
     * @throws IOException 
     */
    public void computeCostFiltered() throws IOException {
        setMajorityVote();
        setMajorityCost();
        filterModels();
        int numClassifiers = predsTrain.getNumFeatures();
        int numFitnessCasesTrain = predsTrain.getNumExemplars();
        // | cost on train | cost on test | fp train | fp test | fn train | fn test
        double[][] costs = new double[numClassifiers][6];
        
        // COMPUTE COST FOR ALL CLASSIFIERS ON TRAIN SET
        double[][] predsTrainMatrix = predsTrain.getInputValues();
        double[] trueLabels = predsTrain.getTargetValues();
        for(int j=0;j<numClassifiers;j++){
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
                if(predsTrainMatrix[i][j]==1){
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
            costs[j][0] = cost;
            costs[j][2] = falsePositiveRate;
            costs[j][4] = falseNegativeRate;
        }
        
        // COMPUTE COST FOR ALL CLASSIFIERS ON TEST SET
        int numFitnessCasesTest = predsTest.getNumExemplars();
        double[][] predsTestMatrix = predsTest.getInputValues();
        double[] trueTest = predsTest.getTargetValues();
        for(int j=0;j<numClassifiers;j++){
            double numPositiveTarget = 0;
            double numNegativeTarget = 0;
            double numFalsePositives = 0;
            double numFalseNegatives = 0;
            for(int i=0;i<numFitnessCasesTest;i++){
                boolean val = false;
                if(predsTestMatrix[i][j]==1){
                    val = true;
                }
                boolean target = false;
                if(trueTest[i]==1) target = true;
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
            costs[j][1] = cost;
            costs[j][3] = falsePositiveRate;
            costs[j][5] = falseNegativeRate;
        }
        
        for(int j=0;j<numClassifiers;j++){
            if(filteredClassifiers[j]==true){
                System.out.println(costs[j][0] + "," + costs[j][1] + "," + costs[j][2] + "," + costs[j][3] + "," + costs[j][4] + "," + costs[j][5]);
            }
        }
    }
        
        
    /**
     * Save filtered predictions and indices to csv files
     */
    public void saveFilteredModels() {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(pathToFilterTrain));
            PrintWriter printWriter = new PrintWriter(bw);
            double[][] predsTrainMatrix = predsTrain.getInputValues();
            for(int i=0;i<predsTrain.getNumExemplars();i++){
                for(int j=0;j<predsTrain.getNumFeatures();j++){
                    if(filteredClassifiers[j]==true || (predsTrain.getNumFeatures()!=predsTest.getNumFeatures())){
                        printWriter.write((int)predsTrainMatrix[i][j] + ",");
                    }
                }
                printWriter.write((int)predsTrain.getTargetValues()[i] + "\n");
            }
            printWriter.flush();
            printWriter.close();
            
            BufferedWriter bwTest = new BufferedWriter(new FileWriter(pathToFilterTest));
            PrintWriter printWriterTest = new PrintWriter(bwTest);
            double[][] predsTestMatrix = predsTest.getInputValues();
            for(int i=0;i<predsTest.getNumExemplars();i++){
                for(int j=0;j<predsTest.getNumFeatures();j++){
                    if(filteredClassifiers[j]==true || (predsTrain.getNumFeatures()!=predsTest.getNumFeatures())){
                        printWriterTest.write((int)predsTestMatrix[i][j] + ",");
                    }
                }
                printWriterTest.write((int)predsTest.getTargetValues()[i] + "\n");
            }
            printWriterTest.flush();
            printWriterTest.close();
            
            BufferedWriter bwIndices = new BufferedWriter(new FileWriter(pathToIndices));
            PrintWriter printWriterIndices = new PrintWriter(bwIndices);
            for(int j=0;j<predsTest.getNumFeatures();j++){
                if(filteredClassifiers[j]==true){
                    printWriterIndices.write(j + "\n");
                }
            }
            printWriterIndices.flush();
            printWriterIndices.close();
        } catch (IOException e) {
            System.err.println("IO Exception in method saveFilteredModels of class FilterPredictions");
        }
    }

}