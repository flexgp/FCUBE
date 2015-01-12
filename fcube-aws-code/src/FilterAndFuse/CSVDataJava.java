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
 * @author Owen Derby and Ignacio Arnaldo
 * 
 */

package FilterAndFuse;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


/**
 * Class for ingesting data from a Comma Separated Value (CSV) text file. All
 * data is assumed to be doubles.
 * 
 */
public class CSVDataJava  {

    String dataPath;
    int numColumns,numRows;
    private int numFeatures;
    private int numExemplars;
    boolean hasHeader;
    double[][] fitnessCases;
    double[] target;
    
    /**
     * Parse given csvfile into set of input and target values.
     * 
     * @param csvfile file of comma-separated values, last value in each line is
     *        the target value
     * @throws java.io.IOException
     */
    public CSVDataJava(String csvfile) throws IOException {
        dataPath = csvfile;
        setNumberOfColumnsAndRows();
        numFeatures = numColumns-1;
        if(hasHeader){
            numExemplars = numRows -1;
        }else{
            numExemplars = numRows;
        }
        fitnessCases = new double[numExemplars][numFeatures];
        this.target = new double[numExemplars];
        BufferedReader f = new BufferedReader(new FileReader(dataPath));

        if(hasHeader)f.readLine();
        
        int fitnessCaseIndex = 0;
        while (f.ready() && fitnessCaseIndex < numExemplars) {
            String[] token = f.readLine().split(",");
            for (int i = 0; i < token.length - 1; i++) {
                this.fitnessCases[fitnessCaseIndex][i] = Double.valueOf(token[i]);
            }
            Double val = Double.valueOf(token[token.length - 1]);
            target[fitnessCaseIndex] = val;
            fitnessCaseIndex++;
        }
        f.close();
    }

    /**
     * Count number of cols and rows of the data
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void setNumberOfColumnsAndRows() throws FileNotFoundException, IOException {
        numRows = 0;
        BufferedReader f= new BufferedReader(new FileReader(dataPath));
        String line = f.readLine();
        numRows++;
        String[] tokens = line.split("\\s*,\\s*|\\s+");
        numColumns = tokens.length;
        if(tokens[0].startsWith("f_") || tokens[0].startsWith("r_") || tokens[0].startsWith("i_") || tokens[0].startsWith("n_")){// headers!
            hasHeader = true;
        }
        while((line=f.readLine())!=null){
            numRows++;
        }
        f.close();
    }
    
    /**
     * Return true/target values
     * @return target vector
     */
    public double[] getTargetValues(){
        return target;
    }

    /**
     * Display length/width of data
     */
    public void printDataInfo() {
            System.out.println("We have " + this.numExemplars + " fitness cases and " + this.numFeatures+ " values");
    }

    /**
     * @return the numFeatures
     */
    public int getNumFeatures() {
        return numFeatures;
    }

    /**
     * @return the numExemplars
     */
    public int getNumExemplars() {
        return numExemplars;
    }
        
    /**
     * Returns the data matrix
     * @return exemplars
     */
    public double[][] getInputValues(){
        return fitnessCases;
    }
        
}