package MenuManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.Thread.sleep;
import java.util.Scanner;


/**
 * Perform simple operations to parse the arguments entered via FCUBE's server 
 * web interface and call external commands to start the run, retrieve classifiers,
 * filter and fuse the predictions, and show performance metrics
 * 
 * @author Ignacio Arnaldo
 */
public class EC2JSPInterface{
    
    String LOG_START = "/usr/share/tomcat7/start.log";
    String LOG_LAUNCH = "/usr/share/tomcat7/launch.log";
    String LOG_RETRIEVE = "/usr/share/tomcat7/retrieve.log";
    String LOG_COPY = "/usr/share/tomcat7/copy.log";
    String LOG_FUSION = "/usr/share/tomcat7/fusion.log";
    String LOG_FUSION_LOGREG = "/usr/share/tomcat7/fusionlogreg.log";
    String KEYPAIR_NAME = "fcube-keypair";
    
    /**
     * Empty constructor
     */
    public EC2JSPInterface(){
        
    }
    
    /**
     * Set the AWS credentials
     * @param accessKey
     * @param secretKey
     * @throws IOException
     * @throws InterruptedException 
     */
    private void writeCredentials(String accessKey,String secretKey) throws IOException, InterruptedException{
        /*
        [default]
        output = table
        region = us-east-1
        aws_access_key_id = ........
        aws_secret_access_key = ..........
        */    
        BufferedWriter bw = new BufferedWriter(new FileWriter("/usr/share/tomcat7/.aws/config"));
        PrintWriter printWriter = new PrintWriter(bw);
        printWriter.write("[default]\n");
        printWriter.write("output = table\n");
        printWriter.write("region = us-east-1\n");
        printWriter.write("aws_access_key_id = " + accessKey + "\n");
        printWriter.write("aws_secret_access_key = " + secretKey + "\n");
        printWriter.flush();
        printWriter.close();
    }
    
    /**
     * Generate a parameter option file,
     * This step depends on the selected learner
     * @param trainingDataFolder
     * @param learner
     * @param dataPercentage
     * @throws IOException 
     * @throws java.lang.InterruptedException 
     */
    public void generateParameterOptionsFile(String trainingDataFolder, String learner, String dataPercentage) throws IOException, InterruptedException{

        String accessKey = "";
        String secretKey = "";
        
        // READ credentials.csv
        Scanner sc = new Scanner(new FileReader("/usr/share/tomcat7/certs/credentials.csv"));
        // skip headers
        String sAux = sc.nextLine();
        if(sc.hasNextLine()){
            sAux = sc.nextLine();
            String[] tokens = sAux.split(",");
            accessKey = tokens[1];
            secretKey = tokens[2];
        }
        writeCredentials(accessKey, secretKey);
        
        BufferedWriter bw = new BufferedWriter(new FileWriter("/usr/share/tomcat7/parameters.options"));
        PrintWriter printWriter = new PrintWriter(bw);
        // data path relative to s3 account
        printWriter.write("learner data " + trainingDataFolder + "\n");
        
        printWriter.write("learner aws_access_key_id " + accessKey + "\n");
        printWriter.write("learner aws_secret_access_key " + secretKey + "\n");
        
        if(learner.equals("gpfunction") || learner.equals("ruletree")){
            
        }else if(learner.equals("mplcs")){
            printWriter.write("learner pop_size 400\n");
            printWriter.write("learner iterations 2000\n");
            printWriter.write("learner crossover_operator 1px\n");
            printWriter.write("learner prob_crossover 0.6\n");
            printWriter.write("learner prob_individual_mutation 0.6\n");
            printWriter.write("learner selection_algorithm tournamentwor\n");
            printWriter.write("learner tournament_size 3\n");
            printWriter.write("learner default_class auto\n");
            printWriter.write("learner initialization_max_classifiers 20\n");
            printWriter.write("learner initialization_min_classifiers 20\n");
            printWriter.write("learner prob_one 0.90\n");
            printWriter.write("learner num_expressed_attributes_init 10\n");
            printWriter.write("learner smart_init 1\n");
            printWriter.write("learner class_wise_init 1\n");
            printWriter.write("learner fitness_function mdl\n");
            printWriter.write("learner hierarchical_selection_iteration 24\n");
            printWriter.write("learner hierarchical_selection_threshold 0\n");
            printWriter.write("learner mdl_initial_tl_ratio 0.075\n");
            printWriter.write("learner mdl_iteration 25\n");
            printWriter.write("learner mdl_weight_relax_factor 0.90\n");
            printWriter.write("learner penalize_individuals_with_less_classifiers_than 12\n");
            printWriter.write("learner pruning_iteration 5\n");
            printWriter.write("learner pruning_min_classifiers 15\n");
            printWriter.write("learner prob_smart_crossover 0.1\n");
            printWriter.write("learner num_parents_smart_crossover 10\n");
            printWriter.write("learner filter_smart_crossover 0.05\n");
            printWriter.write("learner repetitions_of_rule_ordering 5\n");
            printWriter.write("learner prob_local_search 0.05\n");
            printWriter.write("learner do_rule_cleaning 1\n");
            printWriter.write("learner do_rule_splitting 1\n");
            printWriter.write("learner do_rule_generalizing 1\n");
            printWriter.write("learner windowing_ilas 2\n");
            printWriter.write("learner dump_evolution_stats 1\n");
        }else if(learner.equals("SBBJ")){
            printWriter.write("learner seed 0\n");
            printWriter.write("learner envType datasetEnv\n");
            printWriter.write("learner Psize 120\n");
            printWriter.write("learner Msize 120\n");
            printWriter.write("learner pd 0.1\n");
            printWriter.write("learner pa 0.1\n");
            printWriter.write("learner mua 0.1\n");
            printWriter.write("learner omega 9\n");
            printWriter.write("learner t 1000000000\n");
            printWriter.write("learner Pgap 20\n");
            printWriter.write("learner Mgap 20\n");
            printWriter.write("learner trainSetName data/factored_data.csv\n");
            printWriter.write("learner testSetName data/factored_data.csv\n");
            printWriter.write("learner maxProgSize 48\n");
            printWriter.write("learner pBidMutate 0.1\n");
            printWriter.write("learner pBidSwap 0.1\n");
            printWriter.write("learner pBidDelete 0.1\n");
            printWriter.write("learner pBidAdd 0.1\n");
            printWriter.write("learner statMod 10\n");
        }
        printWriter.write("factoredParams {data_sample_rate, variable_sample_rate}\n");
        printWriter.write("parameter_options start\n");
        printWriter.write("data_sample_rate float discreteSet default ( 1 ) { " + dataPercentage + " ; " + dataPercentage + " }\n");
        printWriter.write("variable_sample_rate float discreteSet default ( 1 ) { 1 ; 1 }\n");
        printWriter.flush();
        printWriter.close();
    }
    
    // keypair = "keypair"; paramOptions = "parameters.options";
    /**
     * Launch instances via FCUBE's -deploy functionality
     * @param learner
     * @param numInstances
     * @param minutes
     * @throws IOException
     * @throws InterruptedException 
     */
    public void launchInstances(String learner, String numInstances,String minutes, String flavor) throws IOException, InterruptedException{
        String create_key_cmd = "/usr/bin/aws ec2 delete-key-pair --key-name " + KEYPAIR_NAME + " ; /usr/bin/aws ec2 create-key-pair --key-name " + KEYPAIR_NAME + " --query 'KeyMaterial' --output text > certs/" + KEYPAIR_NAME + ".pem &";
        Process create_key_process = Runtime.getRuntime().exec(new String[]{"bash","-c",create_key_cmd});
        create_key_process.waitFor();
        String deploy_cmd = "/usr/bin/java -jar fcube.jar -deploy " + learner + " -n " + numInstances + " -minutes " + minutes + " -key_name " + KEYPAIR_NAME + " -options parameters.options -flavor " + flavor + " > " + LOG_LAUNCH + " &";
        Process launch_process = Runtime.getRuntime().exec(new String[]{"bash","-c",deploy_cmd});
        launch_process.waitFor();
    }
    
    /**
     * Log the start time
     * @param learningTime
     * @throws IOException 
     */
    public void logStartTime(String learningTime) throws IOException{
        BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_START));
        PrintWriter printWriter = new PrintWriter(bw);
        long startTime = System.currentTimeMillis();
        printWriter.write(startTime + "\n");
        printWriter.flush();
        printWriter.close();
    }
    
    //public String getRemainingMinutes(String learningTime,String numInstancesS) throws FileNotFoundException{
    /**
     * Get remaining time
     * @param learningTime
     * @return
     * @throws FileNotFoundException 
     */
    public String getRemainingMinutes(String learningTime) throws FileNotFoundException{
        Scanner sc = new Scanner(new File(LOG_START));
        String line = "";
        String minutesLeft = "?";
        if ( sc.hasNextLine() ){
            line = sc.nextLine();
            if(!line.equals("")){
                long startTime = Long.parseLong(line);
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - startTime;
                //long numInstances = Long.parseLong(numInstancesS);
                //long launchOverheadEstimate = (numInstances/50) * 10 + 10;
                elapsedTime = elapsedTime / 1000; // in seconds
                elapsedTime = elapsedTime / 60; // in minutes
                long minutesBudget = Long.parseLong(learningTime);
                long minutesL = minutesBudget  - elapsedTime;
                if(minutesL>0){
                    minutesLeft = String.valueOf(minutesL);
                }else{
                    minutesLeft = "0";
                }
            }
        }
        return minutesLeft;
    }
    
    /**
     * Count running instances
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public String countRunningInstances() throws IOException, InterruptedException{
        String numRunningCommand =  "/usr/bin/less " + LOG_LAUNCH + " | grep FINISHED | wc -l";
        Process countRunningInstances_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", numRunningCommand});
        countRunningInstances_process.waitFor();
        BufferedReader br = new BufferedReader(new InputStreamReader(countRunningInstances_process.getInputStream()));
        String runningInstances = br.readLine();
        if (runningInstances!=null) {
            runningInstances = runningInstances.trim();
        }else{
            runningInstances = "0";
        }
        return runningInstances;
    }
    
    /**
     * Retrieve classifiers via FCUBE's -retrieve functionality
     * @param learnerName
     * @throws InterruptedException
     * @throws IOException 
     */
    public void retrieveModels(String learnerName) throws InterruptedException, IOException{
        String modelName = "";
        if(learnerName.equals("gpfunction") || learnerName.equals("ruletree")){
            modelName = "mostAccurate.txt";
        }else if(learnerName.equals("mplcs")){
            modelName = "model.txt";
        }else if(learnerName.equals("SBBJ")){
            modelName = "bestTeam.model";
        }
        String retrieve_cmd = "/usr/bin/java -jar fcube.jar -retrieve " + modelName + " -keypairPath certs/" + KEYPAIR_NAME + ".pem -learner " + learnerName + " > " + LOG_RETRIEVE;
        Process retrieve_process = Runtime.getRuntime().exec(new String[]{"bash","-c",retrieve_cmd});
        retrieve_process.waitFor();
    } 

    /**
     * Count the retrieved models
     * @param learnerName
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public String countRetrievedModels(String learnerName) throws IOException, InterruptedException{
        String modelName = "";
        if(learnerName.equals("gpfunction") || learnerName.equals("ruletree")){
            modelName = "mostAccurate.txt";
        }else if(learnerName.equals("mplcs")){
            modelName = "model.txt";
        }else if(learnerName.equals("SBBJ")){
            modelName = "bestTeam.model";
        }
        String countModelsCommand =  "/bin/ls -R models | /bin/grep " + modelName + " | /usr/bin/wc -l";
        Process countModels_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", countModelsCommand});
        countModels_process.waitFor();
        BufferedReader br = new BufferedReader(new InputStreamReader(countModels_process.getInputStream()));
        String numModels = br.readLine();
        if (numModels!=null) {
            numModels = numModels.trim();
        }else{
            numModels = "0";
        }
        return numModels;
        
    }

    /**
     * Copy fusion and test data from S3
     * @param fusionTraining
     * @param testData
     * @throws IOException
     * @throws InterruptedException 
     */
    public void copyFusionAndTestFiles(String fusionTraining,String testData) throws IOException, InterruptedException{
        BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_COPY));
        PrintWriter printWriter = new PrintWriter(bw);
        int maxCPTries = 20;
        int exitValue = 255;
        int numTries = 0;
        // /usr/bin/aws s3 cp s3://fcube/bp/bp_train_0.csv data/fusion.csv
        String copy_fusion_command = "/usr/bin/aws s3 cp s3://" + fusionTraining + " data/fusion.csv" ;
        while(exitValue!=0 && numTries<maxCPTries){
            printWriter.write(copy_fusion_command + "\n");
            Process copy_fusion_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", copy_fusion_command});
            exitValue = copy_fusion_process.waitFor();
            numTries++;
            sleep( 5000 );// 5 seconds
        }
        
        exitValue = 255;
        numTries = 0;
        String copy_test_command = "/usr/bin/aws s3 cp s3://" + testData + " data/test.csv" ;
        while(exitValue!=0 && numTries<maxCPTries){
            printWriter.write(copy_test_command + "\n");
            Process copy_test_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", copy_test_command});
            exitValue = copy_test_process.waitFor();
            numTries++;
            sleep( 5000 );// 5 seconds
        }
        
        printWriter.flush();
        printWriter.close();
    }
    
    
    /**
     * Filter and fuse classifiers via FCUBE's -filter-fuse functionality
     * @param learnerName
     * @throws InterruptedException
     * @throws IOException 
     */
    public void filterFuseModels(String learnerName) throws InterruptedException, IOException{
        String modelName = "";
        if(learnerName.equals("gpfunction") || learnerName.equals("ruletree")){
            modelName = "mostAccurate.txt";
        }else if(learnerName.equals("mplcs")){
            modelName = "model.txt";
        }else if(learnerName.equals("SBBJ")){
            modelName = "bestTeam.model";
        }
        String filterfuse_cmd = "/usr/bin/java -jar fcube.jar -filter-fuse data/fusion.csv data/test.csv -model " + modelName + " -fnweight 0.5 -learners " + learnerName + " > " + LOG_FUSION + " &";
        Process filterfuse_process = Runtime.getRuntime().exec(new String[]{"bash","-c",filterfuse_cmd});
        filterfuse_process.waitFor();
    }
    
    /**
     * Count evaluated models
     * @param learnerName
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public String countEvalModels(String learnerName) throws IOException, InterruptedException{
        String modelName = "";
        if(learnerName.equals("gpfunction") || learnerName.equals("ruletree")){
            modelName = "mostAccurate.txt";
        }else if(learnerName.equals("mplcs")){
            modelName = "model.txt";
        }else if(learnerName.equals("SBBJ")){
            modelName = "bestTeam.model";
        }
        String countEvalModelsCommand =  "/bin/ls -R models | /bin/grep " + modelName + " | /bin/grep csv | /bin/grep Test | /usr/bin/wc -l";
        Process countEvalModels_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", countEvalModelsCommand});
        countEvalModels_process.waitFor();
        BufferedReader br = new BufferedReader(new InputStreamReader(countEvalModels_process.getInputStream()));
        String numEvalModels = br.readLine();
        if (numEvalModels!=null) {
            numEvalModels = numEvalModels.trim();
        }else{
            numEvalModels = "0";
        }
        return numEvalModels;
    }
    
    // /usr/bin/less models/filteredIndices.csv | /usr/bin/wc -l
    /**
     * Count filtered models
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public String countFilteredModels() throws IOException, InterruptedException{
        String numEvalModels = "0";
        File f = new File("models/filteredIndices.csv");
        if(f.exists() && !f.isDirectory()) { 
            String countFilteredModelsCommand =  "/usr/bin/less models/filteredIndices.csv | /usr/bin/wc -l";
            Process countFilteredModels_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", countFilteredModelsCommand});
            countFilteredModels_process.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(countFilteredModels_process.getInputStream()));
            numEvalModels = br.readLine();
            if (numEvalModels!=null) {
                numEvalModels = numEvalModels.trim();
            }else{
                numEvalModels = "0";
            }
        }
        return numEvalModels;
    }
     
    //  fusionlogreg.log
    //  auc=9.021971e-01
    //  accuracy=9.834913e-01
    //  tpr=0
    //  fpr=0
    /**
     * Read the performance metrics of the fused model
     * @param stats
     * @throws FileNotFoundException 
     */
    public void readFusedStats(String[] stats) throws FileNotFoundException{
        File f = new File(LOG_FUSION_LOGREG);
        if(f.exists() && !f.isDirectory()) {
            Scanner sc = new Scanner(new FileReader(LOG_FUSION_LOGREG));
            while(sc.hasNextLine()){
                String line = sc.nextLine();
                String[] tokens = line.split("=");
                if(tokens[0].equals("auc")){
                    stats[0] = tokens[1].trim();
                }else if(tokens[0].equals("accuracy")){
                    stats[1] = tokens[1].trim();
                }else if(tokens[0].equals("tpr")){
                    stats[2] = tokens[1].trim();
                }else if(tokens[0].equals("fpr")){
                    stats[3] = tokens[1].trim();
                }
            }
        }else{
            stats[0] = "0";
            stats[1] = "0";
            stats[2] = "0";
            stats[3] = "0";
        }
    }
    
    // /usr/bin/rsync -av models/* download --exclude '*.csv'
    // cp modelWeights.csv download/
    // /usr/bin/zip -r download.zip download/
    /**
     * Zip the fused model
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public String zipModel() throws IOException, InterruptedException{
        String downloadButton = " disabled=\"disabled\" ";
        File f = new File("modelWeights.csv");
        if(f.exists() && !f.isDirectory()) {
            String rsync_cmd = "/usr/bin/rsync -av models/* download --exclude '*.csv' ; "
                    + "cp modelWeights.csv download/ ; /usr/bin/zip -r webapps/fcube-aws-server/download.zip download/ ";
            Process rsync_process = Runtime.getRuntime().exec(new String[]{"bash","-c",rsync_cmd});
            int exitValue = rsync_process.waitFor();
            if(exitValue==0) downloadButton = " ";
        }
        return downloadButton;
    }
    
    /**
     * FOR DEBUGGING ONLY: LAUNCH A DUMMY INSTANCE
     * @throws IOException
     * @throws InterruptedException 
     */
    public void launchDummyInstance() throws IOException, InterruptedException{
        String AMI = "ami-b269adda";
        String flavor = "t1.micro";
        String boot_cmd = "cd /usr/share/tomcat7/ ; ls > ls.txt ; /usr/bin/aws --profile default ec2 run-instances --image-id " + AMI + " --instance-type " + flavor  + " | grep  InstanceId | cut -d'|' -f4";
        Process launch_process = Runtime.getRuntime().exec(new String[]{"bash","-c",boot_cmd});
        launch_process.waitFor();
    }
        
}
