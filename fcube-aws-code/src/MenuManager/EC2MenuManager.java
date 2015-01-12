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

package MenuManager;

import BootAndTerminate.BootEC2;
import BootAndTerminate.TerminateEC2;
import java.io.IOException;
import Factoring.DataFactoring;
import Factoring.ParameterFactoringEC2;
import FilterAndFuse.CostAllCIM;
import FilterAndFuse.EvalModelsCIM;
import FilterAndFuse.FilterPredictions;
import FilterAndFuse.FusePredictions;
import FilterAndFuse.RetrieveModelsEC2;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * This class wraps provides a command line interface to start and terminate 
 * instances, retrieve models, and filter and fuse them from the server. 
 * It is also used to perform the factoring at the FCUBE instances.
 * 
 * @author Ignacio Arnaldo
 */
public class EC2MenuManager{
    
    //private String image = "fcube-worker-13";
    private String AMI = "ami-a06e1fc8";
    
    //private String flavor = "c3.large";
    //private String flavor = "m3.medium";
    //private String flavor = "t1.micro";
    
    /**
     * Empty Constructor
     */
    public EC2MenuManager(){
        
    }
    
    /**
     * Print usage for the command line interface
     */
    public void printUsage(){
        System.err.println();
        System.err.println("USAGE:");
        System.err.println();
        System.err.println("LAUNCH NODES");
        System.err.println("java -jar fcube.jar -deploy learner -n numInstances -minutes min -key_name keypair -options factoring.options");
        System.err.println();
        //System.err.println("FACTOR PARAMETERS AND DATA");
        //System.err.println("java -jar fcube.jar -factor factoring.options");
        //System.err.println();
        System.err.println("RETRIEVE MODELS:");
        System.err.println("java -jar fcube.jar -retrieve modelFile -keypairPath keypair -learner learner_1");
        System.err.println();
        System.err.println("FILTER AND FUSE MODELS:");
        System.err.println("java -jar fcube.jar -filter-fuse dataTrainPath dataTestPath -model modelFile -fnweight fnw -learners learner_1 ... learner_m");
        System.err.println();
        System.err.println("TERMINATE INSTANCES:");
        System.err.println("java -jar fcube.jar -terminate");
        System.err.println();
    }

    
    /**
     * Parses the deploy command
     * @param args
     * @throws IOException
     * @throws InterruptedException 
     */
    public void parseDeploy(String[] args) throws IOException, InterruptedException {
        
        if(args.length==12){
            String learner = args[1];
            if(args[2].equals("-n")){
                int numInstances = Integer.parseInt(args[3]);
                if(args[4].equals("-minutes")){
                    int minutes = Integer.parseInt(args[5]);
                    if(args[6].equals("-key_name")){
                        String keypair = args[7];
                        if(args[8].equals("-options")){
                            String factOptions = args[9];
                            if(args[10].equals("-flavor")){
                                String flavor = args[11];
                                BootEC2 bec2 = new BootEC2(AMI, keypair, flavor,learner,factOptions);
                                bec2.deployFCUBE(numInstances,minutes);
                            }
                            
                        }else{
                            System.err.println("Error: wrong argument. Expected -options flag");
                            printUsage();
                        }
                    }else{
                        System.err.println("Error: wrong argument. Expected -key flag");
                        printUsage();
                    }
                }else{
                    System.err.println("Error: wrong argument. Expected -minutes flag");
                    printUsage();
                }
            }else{
                System.err.println("Error: wrong argument. Expected -n flag");
                printUsage();
            }

        }else{
            System.err.println("Wrong number of arguments!");
            printUsage();
        }
        
    }
    
    /**
     * Parses the factor command
     * @param args
     * @throws InterruptedException
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void parseFactorParameters(String[] args) throws InterruptedException, FileNotFoundException, IOException{
    	if(args.length==2){
            String factoredProps = "parameters.properties";
            ParameterFactoringEC2 pf = new ParameterFactoringEC2(args[1],factoredProps);
            pf.generateProperties();
        }else{
            System.err.println("Wrong number of arguments!");
            printUsage();
        }
    }
    
    /**
     * Parses the split command
     * @param args
     * @throws InterruptedException
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void parseSplitParameters(String[] args) throws InterruptedException, FileNotFoundException, IOException{
    	if(args.length==3){
            String dataPath = args[1];
            int numSplits = Integer.parseInt(args[2]);
            DataFactoring df = new DataFactoring(dataPath, System.currentTimeMillis());
            df.splitData(dataPath,numSplits);
        }else{
            System.err.println("Wrong number of arguments!");
            printUsage();
        }
    }

    /**
     * Parses the retrieve command
     * @param args
     * @throws IOException
     * @throws InterruptedException 
     */
    public void parseRetrieveModels(String[] args) throws IOException, InterruptedException{
        if(args.length==6){
            String modelPath = args[1];
            if(args[2].equals("-keypairPath")){
                String keypair = args[3];
                if(args[4].equals("-learner")){
                    String learner = args[5];
                    RetrieveModelsEC2 rm = new RetrieveModelsEC2(modelPath,learner,keypair);
                    rm.retrieveModels();
                }else{
                    System.err.println("Error: wrong argument. Expected -learners flag");
                    printUsage();
                }
            }else{
                System.err.println("Error: wrong argument. Expected -key flag");
                printUsage();
            }
        }else{
            System.err.println("Wrong number of arguments!");
            printUsage();
        }
    }

    
    /**
     * parses the filter-fuse command
     * @param args
     * @throws IOException
     * @throws InterruptedException 
     */
    public void parseFilterAndFuseModels(String args[]) throws IOException, InterruptedException{
        if(args.length>=9){
            String dataPathTrain = args[1];
            String dataPathTest = args[2];
            if(args[3].equals("-model")){
                String model = args[4];
                if(args[5].equals("-fnweight")){
                    double fnw = Double.parseDouble(args[6]);
                    if(args[7].equals("-learners")){
                        ArrayList<String>  alLearners = new ArrayList<String>();
                        for(int i=8;i<args.length;i++){
                            alLearners.add(args[i]);
                        }
                        
                        System.out.println("OBTAINING PREDICTIONS");
                        System.out.println();
                        //EvalModels em = new EvalModels(alLearners,dataPathTrain,dataPathTest,model);
                        EvalModelsCIM em = new EvalModelsCIM(alLearners,dataPathTrain,dataPathTest,model);
                        em.getPredictions();
                        System.out.println("FILTERING MODELS ");
                        System.out.println();
                        FilterPredictions filterpreds = new FilterPredictions(dataPathTrain,dataPathTest,model,fnw);
                        filterpreds.getPredictionsMatrices();
                        filterpreds.setMajorityVote();
                        filterpreds.setMajorityCost();
                        filterpreds.filterModels();
                        filterpreds.saveFilteredModels();
                        System.out.println("FUSED MODEL STATS: ");
                        FusePredictions fusepreds = new FusePredictions(fnw);
                        fusepreds.computeStatsMajorityVote();
                        fusepreds.logisticRegressionMatlab();                        
                    }else{
                        System.err.println("Error: wrong argument. Expected -learners flag");
                        printUsage();
                    }
                }else{
                    System.err.println("Error: wrong argument. Expected -fnweight flag");
                    printUsage();
                }
            }else{
                System.err.println("Error: wrong argument. Expected -model flag");
                printUsage();
            }

        }else{
            System.err.println("Error: wrong number of arguments");
            printUsage();
        }
    }
    
    /**
     * Parses the terminate command
     * @param args
     * @throws IOException
     * @throws InterruptedException 
     */
    public void parseTerminateInstances(String[] args) throws IOException, InterruptedException{
        if(args.length==1){
            TerminateEC2 tosn = new TerminateEC2(AMI);
            tosn.terminateInstances();
        }else{
            System.err.println("Error: wrong number of arguments");
            printUsage();
        }
    }
    
    /**
     * Parses the costall command
     * @param args
     * @throws IOException
     * @throws InterruptedException 
     */
    public void parseComputeCostAllModels(String args[]) throws IOException, InterruptedException{
        if(args.length==3){

            double fnw = Double.parseDouble(args[1]);
            String pathMatrix = args[2];
            CostAllCIM costall = new CostAllCIM(fnw,pathMatrix);

            costall.setMajorityVote();
            costall.setMajorityCost();
            costall.computeCostAll();
        }else{
            System.err.println("Error: wrong number of arguments");
            printUsage();
        }
    }    
    
    /**
     * Entry point for the command line interface of FCUBE
     * @param args
     * @throws IOException
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws IOException, InterruptedException{
        EC2MenuManager nmm = new EC2MenuManager();
        if (args.length == 0) {
            System.err.println("Error: too few arguments");
            nmm.printUsage();
            System.exit(-1);
        }else{
            if (args[0].equals("-deploy")) {
                nmm.parseDeploy(args);
            }else if (args[0].equals("-factor")) {
                nmm.parseFactorParameters(args);
            }else if (args[0].equals("-split")) {
                nmm.parseSplitParameters(args);
            }else if(args[0].equals("-retrieve")){
                nmm.parseRetrieveModels(args);
            }else if(args[0].equals("-filter-fuse")){
                nmm.parseFilterAndFuseModels(args);
            }else if(args[0].equals("-terminate")){
                nmm.parseTerminateInstances(args);
            }else if(args[0].equals("-costAll")){
                nmm.parseComputeCostAllModels(args);
            }else{
                System.err.println("Error: unknown argument");
                nmm.printUsage();
                System.exit(-1);
            }
        }
    }
}
