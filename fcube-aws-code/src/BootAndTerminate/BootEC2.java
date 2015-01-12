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


package BootAndTerminate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class calls external commands to boot, instances,
 * send configuration files, and trigger the start of the learning algorithms.
 * The boot process is performed in parallel in a multi-threaded fashion.
 * @author Ignacio Arnaldo
 */
public class BootEC2 {
    
    String ami, keypair, flavor, learner, factoringOptions;
    int maxBootThreads = 50;
    int maxSSHTries = 200;
    
    Random r;
    
    /**
     * Constructor for the Boot-up class
     * @param anImage
     * @param aKeypair
     * @param aFlavor
     * @param aLearner
     * @param aFactOptions 
     */
    public BootEC2(String anImage, String aKeypair, String aFlavor, String aLearner, String aFactOptions){
        ami = anImage;
        keypair = aKeypair;
        flavor = aFlavor;
        learner = aLearner;
        factoringOptions = aFactOptions;
        r = new Random(System.currentTimeMillis());
    }
    
   /**
     * method to launch new EC instances
     * The external command is:
     * nova boot --key_name nachokey --image fcube-01 --flavor m1.4core newNova4
     * aws ec2 run-instances --image-id ami-bb9f80d2 --instance-type m3.large
     * @param mapIPs
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public String bootInstance(HashMap<String,String> mapIPs) throws IOException, InterruptedException{
        // LAUNCH ONE INSTANCE
        String boot_cmd = "/usr/bin/aws ec2 run-instances --image-id " + ami + " --instance-type " + flavor  + " --key-name " + keypair + "  | grep  InstanceId | cut -d'|' -f4";
        Process launch_process = Runtime.getRuntime().exec(new String[]{"bash","-c",boot_cmd});
        launch_process.waitFor();
        BufferedReader brLaunch = new BufferedReader(new InputStreamReader(launch_process.getInputStream()));
        String instanceId = brLaunch.readLine();
        instanceId = instanceId.trim();
        // STORE IP OF THE LAUNCHED INSTANCE
        String ip_address="";
        while((ip_address==null)|| ("".equals(ip_address))){
            //aws ec2 describe-instances --instance-ids i-566f8104 | grep PublicIpAddress | cut -d'|' -f5
            String getIP_cmd = "/usr/bin/aws ec2 describe-instances --instance-ids " + instanceId + " | grep PublicIpAddress | cut -d'|' -f5";
            Process getIP_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", getIP_cmd});
            getIP_process.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(getIP_process.getInputStream()));
            ip_address = br.readLine();
            Thread.sleep(30*1000);
        }
        mapIPs.put(instanceId, ip_address.trim());  
        System.out.println("STARTING INSTANCE " + instanceId + " with IP: " + ip_address.trim());
        System.out.flush();
        return instanceId;
    }
     
    /**
     * method to send configuration files to the EC instances
     * The external command is:
     * scp -i certs/key.pem -o StrictHostKeyChecking=no ruletree_factoring.options ruletree_parameters.options ec2-user@128.52.160.106:~/
     * @param instanceId
     * @param mapIPs
     * @throws IOException
     * @throws InterruptedException 
     */
    public void sendOptionFiles(String instanceId,HashMap<String,String> mapIPs) throws IOException, InterruptedException{
        
        if (!mapIPs.containsKey(instanceId)) return;
        String instanceIP = mapIPs.get(instanceId);
        String checkActiveCommand =  "/usr/bin/aws ec2 describe-instance-status --instance-ids " + instanceId + " | head -n18 | tail -n1 | cut -d'|' -f5";
        String state = "";
        while(!(state.equals("ok"))){
            System.out.println(checkActiveCommand);
            Process checkActive_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", checkActiveCommand});
            checkActive_process.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(checkActive_process.getInputStream()));
            state = br.readLine();
            if (state!=null) state=state.trim();
            System.out.println(state);
            if(state==null || state.trim().equals("") || state.trim().equals("initializing") ){
                state="";
                Thread.sleep(30*1000);
            }
        }
        
        //scp -i certs/nachopk.pem ruletree_factoring.options ec2-user@54.80.246.86:~
        String keypairPath = "certs/"+keypair+".pem";
        String send_options_command = "/bin/rm .ssh/known_hosts ; /bin/chmod og-rwx " + keypairPath + "  ; /usr/bin/scp -i " + keypairPath + " -o StrictHostKeyChecking=no " + factoringOptions + " ec2-user@" + instanceIP + ":~";
       
        int exitValue = 255;
        int numTries = 0;
        while(exitValue!=0 && numTries<maxSSHTries){
            System.out.println(send_options_command);
            Process send_options_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", send_options_command});
            exitValue = send_options_process.waitFor();
            numTries++;
            System.out.println(numTries+"th try for SCP "+ instanceId + " with exitValue "+exitValue);
            Thread.sleep(30*1000);
        }
        
        System.out.println("FINISHED SET UP INSTANCE " + instanceId);
        if(exitValue!=0){
            mapIPs.remove(instanceId);
            System.out.println("Failed to launch(SCP) " + instanceId + " " + instanceIP );
            System.out.flush();
        }
    }
    
        
    
    /**
     * method to perform the factoring of data and parameters at each node and to start the learning process
     * The external command are:
     * ssh -o StrictHostKeyChecking=no ec2-user@128.52.160.221 -i certs/nachokey.pem 'java -jar f3.jar -factor ruletree_factoring.options'
     * ssh -o StrictHostKeyChecking=no ec2-user@128.52.160.221 -i certs/nachokey.pem 'java -jar fcube/ruletree.jar -train data/banknote/banknoteTrain.csv -minutes 2 -properties factored.properties > logRun.txt &'
     * @param instanceId
     * @param mapIPs
     * @param timeBudget
     * @throws IOException
     * @throws InterruptedException 
     */
    public void factorAndStartLearningInstance(String instanceId,HashMap<String,String> mapIPs,int timeBudget) throws IOException, InterruptedException{
        // If the node failed in sending optionsfile, just abort
        if (!mapIPs.containsKey(instanceId)) return;
        
        String instanceIP = mapIPs.get(instanceId);
        
        String keypairPath = "certs/"+keypair+".pem";
        String fcubeExecutablePath = "fcube.jar";
        String factorCommand = "java -jar -Xmx2000m " + fcubeExecutablePath + " -factor " + factoringOptions + " > logFactoring.txt";
        String learnerCommand= "";
        
        // call the -train command for each learner
        // an additional case needs to be added when new learners are added to FCUBE
        if(learner.equals("gpfunction") || learner.equals("ruletree") || learner.equals("rulelist") || learner.equals("gpfunctionkde") ){ // ALFA JAVA
            String learnerExecutablePath = "learners/"+learner+".jar";
            learnerCommand = "java -jar -Xmx3000m " + learnerExecutablePath + " -train data/factored_data.csv -minutes " + timeBudget + " -properties parameters.properties > logRun.txt";
        }else if(learner.equals("mplcs")){// C OR C++
            String learnerBinary = "learners/" + learner;
            learnerCommand = learnerBinary + " -train data/factored_data.csv -minutes " + timeBudget + " -properties parameters.properties -model model.txt > logRun.txt";
        }else if(learner.equals("lccb")){// python
            String learnerExecutablePath = "learners/" + learner+".py";
            learnerCommand = "python " + learnerExecutablePath + " -train data/factored_data.csv -minutes " + timeBudget + " -properties parameters.properties > logRun.txt";
        }else if(learner.equals("SBBJ")){
            // append train set size to properties file
            //"echo trainSetSize $( less /media/DATA/datasets/higgs/no_headers/v7_higgs-alfa_0.csv | wc -l)"
            // append data dimensionality to properties file
            //echo setDim $(head -n1 /media/DATA/datasets/higgs/no_headers/v7_higgs-alfa_0.csv | cut --complement -d',' -f1 | tr "," "\n" | wc -l) >> parameters.properties"
            String learnerExecutablePath = "learners/"+learner+".jar";
            learnerCommand = "/home/ec2-user/append_properties.sh "
                            + " && java -jar -Xmx3000m " + learnerExecutablePath + " -train data/factored_data.csv -minutes " + timeBudget + " -properties parameters.properties > logRun.txt";
        }else if(learner.equals("adn-static.xeon")){// adn
            // ./adn-static.xeon -t /media/DATA/datasets/bp/splits/no_headers/bp_train_0.csv -m 1 -s sample.script
            String learnerBinary = "learners/" + learner;
            learnerCommand = learnerBinary + " -t data/factored_data.csv -m " + timeBudget + " -s parameters.properties > logRun.txt";
        }
        if(instanceIP!=null){
            // ssh -i certs/nachopk.pem ec2-user@54.80.246.86 "( ls > ls.txt && pwd > pwd.txt ) >/dev/null 2>&1 & ";
            String sshCommand = "/usr/bin/ssh -i " + keypairPath + " -o StrictHostKeyChecking=no ec2-user@" + instanceIP
                    + " \"( "  + factorCommand + " "+ " && " + learnerCommand + " ) >/dev/null 2>&1 & \"";
            System.out.println(sshCommand);
            System.out.flush();
            int exitValue = 255;
            int numTries = 0;
            while(exitValue!=0 && numTries<maxSSHTries){
                Process ssh_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", sshCommand});
                //this.saveText(LOG_PATH, sshCommand + "\n", true);
                ssh_process.waitFor();
                exitValue = ssh_process.exitValue();
                numTries++;
                System.out.println(instanceId + " Factoring");
                sleep(30*1000);
            }
            if(exitValue!=0){
                mapIPs.remove(instanceId);
                System.out.println("Failed to launch (FactorandLearn) " + instanceId + " " + instanceIP );
                System.out.flush();
            }
        }
    }
    
    /**
     * This method starts threads that will independently perform the bootup, configuration
     * and start of FCUBE instances.
     * @param numInstances
     * @param timeBudget
     * @throws IOException
     * @throws InterruptedException 
     */
    public void deployFCUBE(int numInstances,int timeBudget) throws IOException, InterruptedException{
        ArrayList<BootInstanceThread> alThreads = new ArrayList<BootInstanceThread>();
        for(int i=0;i<maxBootThreads;i++){
            BootInstanceThread threadAux = new BootInstanceThread(i,maxBootThreads,numInstances,learner,timeBudget);
            alThreads.add(threadAux);
        }for(int i=0;i<maxBootThreads;i++){
            BootInstanceThread threadAux = alThreads.get(i);
            threadAux.start();
        }
        HashMap<String,String> mapIPs = new HashMap<String,String>();
        for(int i=0;i<maxBootThreads;i++){
            BootInstanceThread threadAux = alThreads.get(i);
            try {
                threadAux.join();
                HashMap<String,String> hmAux = threadAux.getIPMap();
                mapIPs.putAll(hmAux);
            } catch (InterruptedException ex) {
                Logger.getLogger(BootEC2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Launched " + mapIPs.size() + " instances");
        System.out.flush();
    }

    /**
     * This method wraps the calls to the bootup method, the method that sends 
     * the configuration file, and the method that starts the learning process.
     * 
     * @param timeBudget
     * @param mapIPs
     * @return 
     */
    public String setupInstance(int timeBudget, HashMap<String,String> mapIPs){
    	String instanceId ="";
        try {
            instanceId = bootInstance(mapIPs);
        } catch (IOException ex) {
            Logger.getLogger(BootEC2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(BootEC2.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            sendOptionFiles(instanceId, mapIPs);
        } catch (IOException ex) {
            Logger.getLogger(BootEC2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(BootEC2.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            factorAndStartLearningInstance(instanceId,mapIPs,timeBudget);
        } catch (IOException ex) {
            Logger.getLogger(BootEC2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(BootEC2.class.getName()).log(Level.SEVERE, null, ex);
        }
        return instanceId;
    }
    
    /**
     * This method saves a String in the specified file.
     * @param filepath
     * @param text
     * @param append 
     */
    protected void saveText(String filepath, String text, Boolean append) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filepath,append));
            PrintWriter printWriter = new PrintWriter(bw);
            printWriter.write(text);
            printWriter.flush();
            printWriter.close();
        } catch (IOException e) {
            System.err.println("Exception in saveTest in class BootEC2");
        }
    }
    
    /**
     * Auxiliary thread class used to perform the setup of new FCUBE instances
     * in parallel. The indices and number of threads are used to determine 
     * how to split the bootup between the threads.
     */
    public class BootInstanceThread extends Thread{
        
        private int indexThread, totalThreads,numInstances, timeBudget;
        String learner;
        private HashMap<String,String> mapIPs;
        
        /**
         * Constructor for the thread class
         * @param anIndex
         * @param aTotalThreads
         * @param aNumInstances
         * @param aLearner
         * @param aTimeBudget 
         */
        public BootInstanceThread(int anIndex, int aTotalThreads, int aNumInstances,String aLearner,int aTimeBudget){
            indexThread = anIndex;
            totalThreads = aTotalThreads;
            numInstances = aNumInstances;
            timeBudget = aTimeBudget;
            learner = aLearner;
            mapIPs = new HashMap<String,String>();
        }
           
        @Override
        public void run(){
            for (int i=0;i<numInstances;i++) {
                if(i%totalThreads==indexThread){
                    System.out.println(indexThread+" Before setting up "+i);
                    setupInstance(timeBudget,mapIPs);
                    System.out.println(indexThread+" After setting up "+i);
                }
            }           
        }

        /**
         * Return the hashmap linking image ids to IPs
         * @return hashmap linking image ids to IPs
         */
        public HashMap<String,String> getIPMap(){
            return mapIPs;
        }
                
     }
    
}

 