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
import java.util.ArrayList;
import java.util.Properties;

/**
 *
 * Evaluates the model with external calls to the learners' -predict functionality
 * Assumes that no factoring random subspace selection (variable factoring)
 * was performed during learning
 * 
 * @author Ignacio Arnaldo
 */
public class EvalModelsCIM {
    
    String dataPathTrain, dataPathTest,model;
    ArrayList<String> listLearners;
    
    /**
     * Constructor
     * @param alLearners
     * @param aDataPathTrain
     * @param aDataPathTest
     * @param aModel 
     */
    public EvalModelsCIM(ArrayList<String> alLearners, String aDataPathTrain, String aDataPathTest, String aModel){
        listLearners = alLearners;
        dataPathTrain = aDataPathTrain;
        dataPathTest = aDataPathTest;
        model = aModel;
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
            System.err.println("IO Exception in loadProps in EvalModelsCIM");
        }
        System.out.println(props.toString());
        return props;
    }
    
    // java -jar learners/ruletree.jar -predict data/banknoteTest.csv -model models/128.52.160.123/mostAccurate.txt -o models/128.52.160.123/preds_mostAccurate.txt
    /**
     * Get predictions with the -predict functionality of the learners
     * @throws IOException
     * @throws InterruptedException 
     */
    public void getPredictions() throws IOException, InterruptedException{
           
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
                    if(filesINFolderAL.contains(model)){
                        System.out.println("EVALUATING: " + nodeFolder.getPath());

                        if(learner.equals("gpfunction") || learner.equals("ruletree") || learner.equals("rulelist")|| learner.equals("gpfunctionkde")){ // JAVA
                            String predictTrain_command = "java -jar -Xmx24000m learners/" + learner + ".jar -predict " + dataPathTrain
                                + " -model " + nodeFolder + "/" + model 
                                + " -o " + nodeFolder + "/predsTrain_" + model ;
                            Process predictTrain_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", predictTrain_command});
                            predictTrain_process.waitFor();
                            String predictTest_command =  "java -jar -Xmx24000m learners/" + learner + ".jar -predict " + dataPathTest
                                + " -model " + nodeFolder + "/" + model 
                                + " -o " + nodeFolder + "/predsTest_" + model ;
                            System.out.println(predictTest_command);
                            //System.out.flush();
                            Process predictTest_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", predictTest_command});
                            predictTest_process.waitFor();
                        }else if(learner.equals("mplcs")){// C OR C++
                            String predictTrain_command = "learners/" + learner + " -predict " + dataPathTrain
                                + " -model " + nodeFolder + "/" + model 
                                + " -o " + nodeFolder + "/predsTrain_" + model+".csv" ;
                            Process predictTrain_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", predictTrain_command});
                            predictTrain_process.waitFor();
                            String predictTest_command = "learners/" + learner + " -predict " + dataPathTest
                                + " -model " + nodeFolder + "/" + model 
                                + " -o " + nodeFolder + "/predsTest_" + model+".csv" ;
                            //System.out.println(predictTest_command);
                            //System.out.flush();
                            Process predictTest_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", predictTest_command});
                            predictTest_process.waitFor();
                        }else if(learner.equals("lccb")){ // Python
                            String predictTrain_command = "python learners/" + learner + ".py -predict " + dataPathTrain
                                + " -model " + nodeFolder + "/" + model 
                                + " -o " + nodeFolder + "/predsTrain_" + model+".csv" ;
                            Process predictTrain_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", predictTrain_command});
                            predictTrain_process.waitFor();
                            String predictTest_command = "python learners/" + learner + ".py -predict " + dataPathTest
                                + " -model " + nodeFolder + "/" + model 
                                + " -o " + nodeFolder + "/predsTest_" + model+".csv" ;
                            System.out.println(predictTest_command);
                            //System.out.flush();
                            Process predictTest_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", predictTest_command});
                            predictTest_process.waitFor();
                        }else if(learner.equals("SBBJ")){ // JAVA
                            String predictTrain_command = "java -jar -Xmx24000m learners/" + learner + ".jar -predict " + dataPathTrain
                                + " -model " + nodeFolder + "/" + model 
                                + " -o " + nodeFolder + "/predsTrain_" + model+".csv" ;
                            Process predictTrain_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", predictTrain_command});
                            predictTrain_process.waitFor();
                            String predictTest_command = "java -jar -Xmx24000m learners/" + learner + ".jar -predict " + dataPathTest
                                + " -model " + nodeFolder + "/" + model 
                                + " -o " + nodeFolder + "/predsTest_1_" + model+".csv" ;
                            Process predictTest_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", predictTest_command});
                            predictTest_process.waitFor();
                        }else if(learner.equals("adn-static.xeon")){// C OR C++
                            //adn-static.xeon -r /media/DATA/datasets/bp/splits/no_headers/bp_test.csv -d model -s csv.script -o predictions.csv
                            String predictTrain_command = "learners/" + learner + " -r " + dataPathTrain + " -d " + nodeFolder + "/" + model 
                                + " -s " + nodeFolder + "/parameters.properties " + " -o " + nodeFolder + "/predsTrain_" + model+".csv > /dev/null" ;
                            Process predictTrain_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", predictTrain_command});
                            predictTrain_process.waitFor();
                            String predictTest_command = "learners/" + learner + " -r " + dataPathTest + " -d " + nodeFolder + "/" + model 
                                + " -s " + nodeFolder + "/parameters.properties " + " -o " + nodeFolder + "/predsTest_" + model+".csv > /dev/null" ;
                            //System.out.println(predictTest_command);
                            //System.out.flush();
                            Process predictTest_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", predictTest_command});
                            predictTest_process.waitFor();
                        }

                    }
                }
            }
        }
    }
}
