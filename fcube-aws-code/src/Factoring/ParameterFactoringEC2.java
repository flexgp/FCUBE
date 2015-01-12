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
 * @author Andrew Song and Ignacio Arnaldo
 * 
 */

package Factoring;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class performs the factoring of data and parameters at the FCUBE instances
 * @author Ignacio Arnaldo
 */
public class ParameterFactoringEC2 {
	
    Properties initializedProps, factoredProps, finalProps;
    Scanner sc;
    Random rnd;
    String propertiesPath;
    
    /**
     * Constructor for the parameter factoring class
     * @param aFactoring_options_file_path
     * @param aPropertiesFilePath
     * @throws FileNotFoundException 
     */
    public ParameterFactoringEC2(String aFactoring_options_file_path, String aPropertiesFilePath) throws FileNotFoundException{
        propertiesPath = aPropertiesFilePath;
        initializedProps = new Properties();
        factoredProps = new Properties();
        finalProps = new Properties();
        sc = new Scanner(new File(aFactoring_options_file_path));
    }
    
    /**
     * Generate sampled data from parameter.options file
     * @throws IOException
     * @throws InterruptedException 
     */
    public void generateProperties() throws IOException, InterruptedException{
        // Read in the parameter options file
        generateParams();
        
        String accessKey = finalProps.getProperty("aws_access_key_id");
        String secretKey = finalProps.getProperty("aws_secret_access_key");
        BufferedWriter bw = new BufferedWriter(new FileWriter("/home/ec2-user/.aws/config"));
        PrintWriter printWriter = new PrintWriter(bw);
        // generate aws configuration file
        printWriter.write("[default]\n");
        printWriter.write("output = table\n");
        printWriter.write("region = us-east-1\n");
        printWriter.write("aws_access_key_id = " + accessKey + "\n");
        printWriter.write("aws_secret_access_key = " + secretKey + "\n");
        printWriter.flush();
        printWriter.close();
        
        finalProps.remove("aws_access_key_id");
        finalProps.remove("aws_secret_access_key");
        
        
        String remotePath = finalProps.getProperty("data");
        String localPath = "data/copied_data.csv";
        String selectedSplit = copySplitToLocalDiskAWS(remotePath,localPath);

        DataFactoring sample = new DataFactoring(localPath,System.currentTimeMillis());
        double data_factor_rate = Double.parseDouble(finalProps.getProperty("data_sample_rate"));
        
        double variable_sample_rate = Double.parseDouble(finalProps.getProperty("variable_sample_rate"));
        int numVariables = sample.getNumVariables();
        ArrayList<Integer> indices = getVariableIndices(numVariables,variable_sample_rate);
        
        // write sampled data and sampled variables
        Properties dataProps = new Properties();
        dataProps.put("data_split", selectedSplit);
        String indicesString = "";
        for(int i=0;i<indices.size();i++){
            indicesString+= indices.get(i) + " ";
        }
        dataProps.put("sampled_variables", indicesString);
        dataProps.put("data_sample_rate", finalProps.getProperty("data_sample_rate"));
        dataProps.put("variable_sample_rate", finalProps.getProperty("variable_sample_rate"));
        dataProps.put("data", finalProps.getProperty("data"));
        try {
            dataProps.store(new FileOutputStream("data.properties"),null);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println("IO Exception in generateProperties");
        }
        
        finalProps.remove("data_sample_rate");
        finalProps.remove("variable_sample_rate");
        finalProps.remove("data");
        try {
            finalProps.store(new FileOutputStream(propertiesPath),null);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            System.err.println("IO Exception in generateProperties");
        }
        String factoredDataPath = "data/factored_data.csv";
        sample.sampleData(factoredDataPath,data_factor_rate,indices);
        
    }
    
    /**
     * Generate Java properties file from parameter.options file
     */
    public void generateParams(){
        String line = "";
        while ( sc.hasNextLine() && (! line.contains("parameter_options start")) ){
            line = sc.nextLine();
            if(!line.equals("") && !line.startsWith("#")){
                if(line.startsWith("learner")){
                    String tokens[] = line.split(" ");
                    String key = tokens[1];
                    String value = "";
                    for(int i=2;i<tokens.length;i++){
                        value += tokens[i] + " ";
                    }
                    initializedProps.put(key, value.trim());
                }else if(line.startsWith("factoredParams")){
                    String factoredParamListPatternDelims = "(.+)\\s+\\{(.+)\\}";
                    Pattern factoredParamsPattern = Pattern.compile(factoredParamListPatternDelims);
                    Matcher factoredParamsMatcher= factoredParamsPattern.matcher(line);
                    factoredParamsMatcher.find();
                    String factoredParamsString = factoredParamsMatcher.group(2);
                    StringTokenizer st = new StringTokenizer(factoredParamsString,",");
                    while(st.hasMoreTokens()){
                        String parameter = st.nextToken().trim();
                        factoredProps.put(parameter, "");
                    }
                }
            }
        }
        finalProps.putAll(factoredProps);
        finalProps.putAll(initializedProps);         
        while ( sc.hasNextLine() ){
            line = sc.nextLine();
            if(!line.equals("") && !line.startsWith("#")){
                parseParamsOptionsLine(line);
            }
        }
    }

    private void parseParamsOptionsLine(String line){
        StringTokenizer st = new StringTokenizer(line," ");
        String paramName = st.nextToken();
        String type = st.nextToken();
        String reservedWord = st.nextToken();
        String defaultValue = getDefaultValue(st);
        System.out.println(paramName);
        if(factoredProps.containsKey(paramName)){
            String chosenValue = "";
            if(reservedWord.equals("discreteSet")){
                chosenValue = parseSet(st);
            }else if(reservedWord.equals("range")){
                if(type.equals("int")){
                    chosenValue = parseRangeInt(st);
                }else if(type.equals("float")){
                    chosenValue = parseRangeFloat(st);
                }
            }else if(reservedWord.equals("subset")){
                String originalString = initializedProps.getProperty(paramName);
                chosenValue = sampleSubset(st,originalString);
            }
            finalProps.setProperty(paramName,chosenValue.trim());
        }else{
            System.out.println(paramName);
            if(reservedWord.equals("subset")){
                defaultValue = initializedProps.getProperty(paramName);
            }
            finalProps.setProperty(paramName,defaultValue.trim());
        }
    }    
    
    /**
     * copy split to disk. The external commands are:
     * aws s3 ls s3://fcube/bp/ | cut -d' ' -f5 | sed -n '1!p'
     * aws s3 cp s3://fcube/bp/bp_train_0.csv data/fusion.csv
     * 
     * @param remotePath
     * @param localPath
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public String copySplitToLocalDiskAWS(String remotePath,String localPath) throws IOException, InterruptedException{
        //aws s3 ls s3://fcube/bp/ | cut -d' ' -f5 | sed -n '1!p'
        String s3_command = "aws s3 ls s3://" + remotePath + "/ | cut -d' ' -f5 | sed -n '1!p'" ;
        int exitValue = 255;
        String splitAux ="";
        BufferedReader  br = null;
        while((splitAux==null)|| ("".equals(splitAux))){
            System.out.println(s3_command);
            Process ls_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", s3_command});
            ls_process.waitFor();
            br = new BufferedReader(new InputStreamReader(ls_process.getInputStream()));
            splitAux = br.readLine();
            System.out.println("split: " +splitAux);
            Thread.sleep(5*1000);
        }
        ArrayList<String> splitNames = new ArrayList<String>();
        splitNames.add(splitAux);
        while((splitAux=br.readLine())!=null){
            splitNames.add(splitAux);
        }
        Collections.shuffle(splitNames);
        String selectedSplit = splitNames.get(0);
        System.out.println("Selected split is " + selectedSplit);
        int maxCPTries = 50;
        exitValue = 255;
        int numTries = 0;
        
        //aws s3 cp s3://fcube/bp/bp_train_0.csv data/fusion.csv
        //String copy_fusion_command = "/usr/bin/aws s3 cp s3://" + fusionTraining + " data/fusion.csv" ;
        String copy_command = "aws s3 cp s3://" + remotePath+"/"+selectedSplit + " " + localPath;
        while(exitValue!=0 && numTries<maxCPTries){
            System.out.println(copy_command);
            Process send_options_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", copy_command});
            exitValue = send_options_process.waitFor();
            numTries++;
            sleep( 30000 + (int)Math.pow(numTries, 2)*1000);
        }
        return selectedSplit;
    }

    /**
     * Select an option from a set
     * @param st
     * @return 
     */
    private String parseSet(StringTokenizer st){
        ArrayList<String> possibleInputs = new ArrayList<String>();
        String leftBr = st.nextToken();
        String token = st.nextToken();
        String accumulated = "";
        while(!token.equals("}")){
            if(token.equals(";")){
                possibleInputs.add(accumulated);
                accumulated = "";
            }else{
                accumulated += token + " ";
            }
            token = st.nextToken();
        }
        // add final option
        possibleInputs.add(accumulated);
        Collections.shuffle(possibleInputs);
        String chosen = possibleInputs.get(0);
        return chosen;
    }
    
    /**
     * Select an option from a range of values
     * @param st
     * @return 
     */
    private String parseRangeFloat(StringTokenizer st){
        String leftBr = st.nextToken();
        String minimum = st.nextToken();
        String col = st.nextToken();
        String step = st.nextToken();
        col = st.nextToken();
        String maximum = st.nextToken();
        String closeBr = st.nextToken();
        float min = Float.parseFloat(minimum);
        float diff = Float.parseFloat(step);
        float max = Float.parseFloat(maximum);
        
        int numPossible = (int)(((max-min) / diff) + 1);
        rnd = new Random(System.currentTimeMillis());
        int indexRandom = rnd.nextInt(numPossible);
        float chosenFloat = min + (indexRandom * diff);
        String chosenValue = Float.toString(chosenFloat);
        rnd = null;
        return chosenValue;
    }
    
    /**
     * Select an option from a range of integers
     * @param st
     * @return 
     */
    private String parseRangeInt(StringTokenizer st){
        String leftBr = st.nextToken();
        String minimum = st.nextToken();
        String col = st.nextToken();
        String step = st.nextToken();
        col = st.nextToken();
        String maximum = st.nextToken();
        String closeBr = st.nextToken();
        int min = Integer.parseInt(minimum);
        int diff = Integer.parseInt(step);
        int max = Integer.parseInt(maximum);
        int numPossible = (int)(((max-min) / diff) + 1);
        rnd = new Random(System.currentTimeMillis());
        int indexRandom = rnd.nextInt(numPossible);
        int chosenInt = min + (indexRandom * diff);
        String chosenValue = Integer.toString(chosenInt);
        rnd = null;
        return chosenValue;
    }

    /**
     * Select a subset from a set of options
     * @param st
     * @param originalString
     * @return 
     */
    private String sampleSubset(StringTokenizer st,String originalString){
        ArrayList<String> possibleInputs = new ArrayList<String>();
        String leftBr = st.nextToken();
        String token = st.nextToken();
        while(!token.equals("}")){
            if(!token.equals(";")){
                possibleInputs.add(token);
            }
            token = st.nextToken();
        }
        Collections.shuffle(possibleInputs);
        String chosenSize = possibleInputs.get(0);
        float percentage = Float.parseFloat(chosenSize);
        String[] originalArray = originalString.split(" ");
        ArrayList<String> originalAL = new ArrayList<String>();
        for(int i=0;i<originalArray.length;i++) originalAL.add(originalArray[i]);
        Collections.shuffle(originalAL);
        int limitSample = (int)(percentage * originalArray.length);
        String sampleString = "";
        for(int i=0;i<limitSample;i++) sampleString += originalAL.get(i) + " ";
        possibleInputs = null;
        
        return sampleString;
    }
    
    /**
     * Retrieve the default value for the parameter
     * @param st
     * @return 
     */
    private String getDefaultValue(StringTokenizer st){
        String defaultS = st.nextToken();
        String leftPar = st.nextToken();
        String defaultValue = "";
        String aux = st.nextToken();
        while(!aux.equals(")")){
            defaultValue += aux + " ";
            aux = st.nextToken();
        }
        return defaultValue;
                
    }

    /**
     * Get the indices of the selected variables
     * @param numVariables
     * @param variable_sample_rate
     * @return 
     */
    public ArrayList<Integer> getVariableIndices(int numVariables,double variable_sample_rate){
        ArrayList<Integer> indices = new ArrayList<Integer>();
        
        ArrayList<Integer> originalAL = new ArrayList<Integer>();
        for(int i=0;i<numVariables;i++){
            originalAL.add(i);
        }
        Collections.shuffle(originalAL);
        int limitSample = (int)(variable_sample_rate * numVariables);
        for(int i=0;i<limitSample;i++){
            indices.add(originalAL.get(i));
        }
        return indices;
    }
        
}
