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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This utility class shuffles the data with given random seed info and samples
 * a certain ratio of the dataset
 * @author Ignacio Arnaldo and Andrew Song
 */
public class DataFactoring {
    
    private Map<String,ArrayList<Integer>> partitioned_indexes; 
    private Random rnd;
    private int numExemplars, numVariables;
    private ArrayList<float[]> dataMatrix;
    boolean hasHeader;
    String[] headerLine;
    
    /**
     * Empty constructor
     */
    public DataFactoring(){}

    /**
     * Constructor that parses a dataset
     * @param dataPath
     * @param seed 
     */
    public DataFactoring(String dataPath, long seed){
        rnd = new Random(seed);
        BufferedReader f;
        dataMatrix = new ArrayList<float[]>();
        partitioned_indexes = new HashMap<String,ArrayList<Integer>>();
        numVariables = -1;
        numExemplars = 0;
        headerLine=null;
        String classLabel = "";
        float[] exemplar;
        try {
            f = new BufferedReader(new FileReader(dataPath));
            
            String line = f.readLine();
            String[] tokens = line.split("\\s*,\\s*|\\s+");
            numVariables = tokens.length-1;
            if(tokens[0].startsWith("f_") || tokens[0].startsWith("r_") || tokens[0].startsWith("i_") || tokens[0].startsWith("n_")){// headers!
                hasHeader = true;
                headerLine = new String[numVariables+1];
                for(int i=0;i<tokens.length;i++){
                    headerLine[i] = tokens[i];
                }
            }else{
                exemplar = new float[numVariables+1];
                for(int i=0;i<tokens.length;i++){
                    exemplar[i] = Float.parseFloat(tokens[i]);
                }
                classLabel = tokens[tokens.length-1];
                dataMatrix.add(numExemplars,exemplar);
                ArrayList<Integer> newClassData = new ArrayList<Integer>();
                newClassData.add(numExemplars);
                partitioned_indexes.put(classLabel,newClassData);
                numExemplars++;
            }
            while((line=f.readLine())!=null){
            	tokens = line.split("\\s*,\\s*|\\s+");
            	classLabel=tokens[tokens.length-1];
                exemplar = new float[numVariables+1];
                for(int i=0;i<tokens.length;i++){
                    exemplar[i] = Float.parseFloat(tokens[i]);
                }
                dataMatrix.add(numExemplars,exemplar);
            	if (!partitioned_indexes.containsKey(classLabel)){
                    ArrayList<Integer> auxClassData = new ArrayList<Integer>();
                    auxClassData.add(numExemplars);
                    partitioned_indexes.put(classLabel,auxClassData);
            	}else{
                    partitioned_indexes.get(classLabel).add(numExemplars);
            	}
                numExemplars++;
            }
        }catch(IOException ex){
            System.err.println("IO Exception in DataFactoring constructor");
        }

        //Debug
        for(String key:partitioned_indexes.keySet()){
            Collections.shuffle(partitioned_indexes.get(key), rnd);
            System.out.println("Partitioned Class "+key+": "+partitioned_indexes.get(key).size());
        }	
    }
	
    /**
     * Sample certain percentage of data
     * @param factoredDataPath
     * @param percentage
     * @param indices
     */
    public void sampleData(String factoredDataPath,double percentage,ArrayList<Integer> indices){
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(factoredDataPath));
            PrintWriter printWriter = new PrintWriter(bw);
            if(hasHeader){
                for(int j=0;j<headerLine.length-1;j++){
                    if(indices.contains(j)){
                        printWriter.write(headerLine[j] + ",");
                    }
                }
                printWriter.write(headerLine[headerLine.length-1] + "\n");
            }
            for(String key:partitioned_indexes.keySet()){
                ArrayList<Integer> listIndexesClass=partitioned_indexes.get(key);
                for(int i=0;i<listIndexesClass.size()*percentage;i++){
                    int index = listIndexesClass.get(i);
                    float[] exemplarAux = dataMatrix.get(index);
                    for(int j=0;j<exemplarAux.length-1;j++){
                        if(indices.contains(j)){
                            printWriter.write(exemplarAux[j] + ",");
                        }
                    }
                    int labelAux = (int)exemplarAux[exemplarAux.length-1];
                    printWriter.write(labelAux + "\n");
                }
                System.out.println("Factored size " + key + ": "+listIndexesClass.size()*percentage);
            }
            printWriter.flush();
            printWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(DataFactoring.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Sample certain percentage of data
     * @param dataPath
     * @param numSplits
     */
    public void splitData(String dataPath,int numSplits){
        for(int i=0;i<numSplits;i++){
            int indexExtension = dataPath.lastIndexOf(".");
            String prefix = dataPath.substring(0, indexExtension);
            String splitDataPath = prefix + "_" + i + ".csv";
            BufferedWriter bw;
            try {
                bw = new BufferedWriter(new FileWriter(splitDataPath));
                PrintWriter printWriter = new PrintWriter(bw);
                for(String key:partitioned_indexes.keySet()){
                    ArrayList<Integer> listIndexesClass=partitioned_indexes.get(key);
                    int linesPerSplit = listIndexesClass.size() / numSplits;
                    int indexInit = i*linesPerSplit;
                    int indexEnd = (i+1)*linesPerSplit;
                    if(indexEnd>listIndexesClass.size()){
                        indexEnd = listIndexesClass.size();
                    }
                    for(int j=indexInit;j<indexEnd;j++){
                        int index = listIndexesClass.get(j);
                        float[] exemplarAux = dataMatrix.get(index);
                        for(int k=0;k<exemplarAux.length-1;k++){
                            printWriter.write(exemplarAux[k] + ",");
                        }
                        printWriter.write(exemplarAux[exemplarAux.length-1] + "\n");
                    }
                    System.out.println("Factored size "+key+": ");
                }
                printWriter.flush();
                printWriter.close();
            } catch (IOException ex) {
                Logger.getLogger(DataFactoring.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }

    /**
     *
     * @return
     */
    public int getNumVariables(){
        return numVariables;
    }
    
    /**
     *
     * @return
     */
    public int getnumExemplars(){
        return numExemplars;
    }
}
