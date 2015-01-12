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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

/**
 * Evaluates the model with external calls to the learners' -predict functionality
 * @author Ignacio Arnaldo
 */
public class EvalModels {
    
    String dataPathTrain, dataPathTest,model;
    ArrayList<String> listLearners;
    
    /**
     * Constructor
     * @param alLearners
     * @param aDataPathTrain
     * @param aDataPathTest
     * @param aModel 
     */
    public EvalModels(ArrayList<String> alLearners, String aDataPathTrain, String aDataPathTest, String aModel){
        listLearners = alLearners;
        dataPathTrain = aDataPathTrain;
        dataPathTest = aDataPathTest;
        model = aModel;
    }
    
    /**
     * Split the data files in columns- necessary to match data used for learning
     * @throws IOException
     * @throws InterruptedException 
     */
    public void splitColsDataFiles() throws IOException, InterruptedException{
        String mkdir_command = "mkdir models/temp";
        Process mkdir_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", mkdir_command});
        mkdir_process.waitFor();
        
        String countColsCommand = "head -n1 " + dataPathTrain + " | tr -cd \",\" | wc -c";
        Process countCols_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", countColsCommand});
        countCols_process.waitFor();
        BufferedReader  br = new BufferedReader(new InputStreamReader(countCols_process.getInputStream()));
        String numFeaturesS = br.readLine();
        int numFeatures = Integer.parseInt(numFeaturesS);
        
        for(int i=1;i<=numFeatures;i++){
            String cutColTr_command = "cut -d',' -f" + i + " " + dataPathTrain + " > models/temp/tr_" + i + ".csv";
            Process cutColTr_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", cutColTr_command});
            cutColTr_process.waitFor();
            String cutColTe_command = "cut -d',' -f" + i + " " + dataPathTest + " > models/temp/te_" + i + ".csv";
            Process cutColTe_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", cutColTe_command});
            cutColTe_process.waitFor();
        }
        int targetsIndex = numFeatures + 1;
        String cutColTr_command = "cut -d',' -f" + targetsIndex + " " + dataPathTrain + " > models/temp/tr_targets.csv";
        Process cutColTr_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", cutColTr_command});
        cutColTr_process.waitFor();
        String cutColTe_command = "cut -d',' -f" + targetsIndex + " " + dataPathTest + " > models/temp/te_targets.csv";
        Process cutColTe_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", cutColTe_command});
        cutColTe_process.waitFor();
    }
    
    /**
     * Load properties used for learning
     * @param propFile
     * @return 
     */
    public Properties loadProps(String propFile) {
        Properties props = new Properties();
        BufferedReader f;
        try {
            f = new BufferedReader(new FileReader(propFile));
        } catch (FileNotFoundException e) {
            return null;
        }
        try {
            props.load(f);
        } catch (IOException e) {
            System.err.println("IO EXception in loadProps in EvalModels class");
        }
        System.out.println(props.toString());
        return props;
    }
    
    /**
     * Get predictions with the -predict functionality of the learners
     * @throws IOException
     * @throws InterruptedException 
     */
    public void getPredictions() throws IOException, InterruptedException{
        //splitColsDataFiles();
        for(String learner:listLearners){
            File modelsFolder = new File("models/"+learner);
            File[] IPFolders = modelsFolder.listFiles();
            for (File nodeFolder : IPFolders) {
                if (nodeFolder.isDirectory()) {
                    File[] filesInFolder = nodeFolder.listFiles();
                    ArrayList<String> filesINFolderAL = new ArrayList<String>();
                    for(int i=0;i<filesInFolder.length;i++){
                        filesINFolderAL.add(filesInFolder[i].getName());
                    }
                    if(filesINFolderAL.contains(model) && filesINFolderAL.contains("data.properties")){
                        System.out.println("Generating data for: " + nodeFolder.getPath());
                        // read properties file
                        // get indices of variables
                        // paste appropriate cols in models/temp
                        // change commands below with appropriate data
                        Properties dataProps = loadProps(nodeFolder.getPath()+"/data.properties");
                        ArrayList<Integer> indices = new ArrayList<Integer>();
                        String featuresS = dataProps.getProperty("sampled_variables");
                        if(featuresS!=null){
                            String[] featuresArray = featuresS.split(" ");
                            for(int i=0;i<featuresArray.length;i++){
                                indices.add(i, Integer.parseInt(featuresArray[i]));
                            }
                            // the order of indices is important
                            Collections.sort(indices);

                            String pasteTrainTempCommand = "paste -d',' ";
                            for(int i=0;i<indices.size();i++){
                                pasteTrainTempCommand += " models/temp/tr_" + (indices.get(i)+1) + ".csv";
                            }
                            pasteTrainTempCommand += " models/temp/tr_targets.csv > models/temp/tr_temp.csv";
                            System.out.println(pasteTrainTempCommand);
                            Process pasteTrainTemp_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", pasteTrainTempCommand});
                            pasteTrainTemp_process.waitFor();


                            String pasteTestTempCommand = "paste -d',' ";
                            for(int i=0;i<indices.size();i++){
                                pasteTestTempCommand += " models/temp/te_" + (indices.get(i)+1) + ".csv";
                            }
                            pasteTestTempCommand += " models/temp/te_targets.csv > models/temp/te_temp.csv";
                            System.out.println(pasteTestTempCommand);
                            Process pasteTestTemp_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", pasteTestTempCommand});
                            pasteTestTemp_process.waitFor();
                            
                            // Add a new case when adding a new learner
                            if(learner.equals("gpfunction") || learner.equals("ruletree") || learner.equals("rulelist")){ // JAVA
                                String predictTrain_command = "java -jar learners/" + learner + ".jar -predict models/temp/tr_temp.csv"
                                    + " -model " + nodeFolder + "/" + model 
                                    + " -o " + nodeFolder + "/predsTrain_" + model ;
                                Process predictTrain_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", predictTrain_command});
                                predictTrain_process.waitFor();
                                String predictTest_command = "java -jar learners/" + learner + ".jar -predict models/temp/te_temp.csv"
                                    + " -model " + nodeFolder + "/" + model 
                                    + " -o " + nodeFolder + "/predsTest_" + model ;
                                Process predictTest_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", predictTest_command});
                                predictTest_process.waitFor();
                            }else if(learner.equals("mplcs")){// C OR C++
                                String predictTrain_command = "learners/" + learner + " -predict models/temp/tr_temp.csv"
                                    + " -model " + nodeFolder + "/" + model 
                                    + " -o " + nodeFolder + "/predsTrain_" + model+".csv" ;
                                Process predictTrain_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", predictTrain_command});
                                predictTrain_process.waitFor();
                                String predictTest_command = "learners/" + learner + " -predict models/temp/te_temp.csv"
                                    + " -model " + nodeFolder + "/" + model 
                                    + " -o " + nodeFolder + "/predsTest_" + model+".csv" ;
                                Process predictTest_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", predictTest_command});
                                predictTest_process.waitFor();
                            }if(learner.equals("lccb")){ // Python
                                String predictTrain_command = "python learners/" + learner + ".py -predict models/temp/tr_temp.csv"
                                    + " -model " + nodeFolder + "/" + model 
                                    + " -o " + nodeFolder + "/predsTrain_" + model+".csv" ;
                                Process predictTrain_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", predictTrain_command});
                                predictTrain_process.waitFor();
                                String predictTest_command = "python learners/" + learner + ".py -predict models/temp/te_temp.csv"
                                    + " -model " + nodeFolder + "/" + model 
                                    + " -o " + nodeFolder + "/predsTest_" + model+".csv" ;
                                System.out.println(predictTest_command);
                                Process predictTest_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", predictTest_command});
                                predictTest_process.waitFor();
                            }if(learner.equals("SBBJ")){ // JAVA
                                String predictTrain_command = "java -jar learners/" + learner + ".jar -predict models/temp/tr_temp.csv"
                                    + " -model " + nodeFolder + "/" + model 
                                    + " -o " + nodeFolder + "/predsTrain_" + model+".csv" ;
                                Process predictTrain_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", predictTrain_command});
                                predictTrain_process.waitFor();
                                String predictTest_command = "java -jar learners/" + learner + ".jar -predict models/temp/te_temp.csv"
                                    + " -model " + nodeFolder + "/" + model 
                                    + " -o " + nodeFolder + "/predsTest_" + model+".csv" ;
                                Process predictTest_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", predictTest_command});
                                predictTest_process.waitFor();
                            }
                            
                        }
                    }
                }
            }
        }
    }
}
