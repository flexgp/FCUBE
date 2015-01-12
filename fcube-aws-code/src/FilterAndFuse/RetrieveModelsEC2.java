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
 * This class retrieves the classifiers learned at the FCUBE instances
 * @author Ignacio Arnaldo
 */
public class RetrieveModelsEC2 {
    
    String keypairPath, modelPath;
    String learner;
    
    /**
     * Constructor
     * @param aModelPath
     * @param aLearner
     * @param aKeypair 
     */
    public RetrieveModelsEC2(String aModelPath, String aLearner, String aKeypair){
        modelPath = aModelPath;
        keypairPath = aKeypair;
        learner = aLearner;
    }
    
    /**
     * Method to retrieve the models, it calls the following external commands:
     * nova list --name fcube-instance | grep inet | cut -d' ' -f8 | cut -d'=' -f2
     * and several calls to pslurp
     * @throws IOException
     * @throws InterruptedException 
     */
    public void retrieveModels() throws IOException, InterruptedException{

        String getIPs_cmd = "less launch.log | grep STARTING | cut -d':' -f2 | cut -d' ' -f2 > ips.txt";
        Process getIPs_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", getIPs_cmd});
        getIPs_process.waitFor();

        String parallelSlurpModelCommand = "/usr/bin/pslurp -OStrictHostKeyChecking=no -OCheckHostIP=no "
            + "-OIdentityFile=" + keypairPath + " -t 100 -h ips.txt -l ec2-user "
            + "-L models/" + learner + " /home/ec2-user/" + modelPath + " " + modelPath;
        System.out.println(parallelSlurpModelCommand);
        Process parallelSlurp_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", parallelSlurpModelCommand});
        parallelSlurp_process.waitFor();

        String parallelSlurpParamsPropsCommand = "/usr/bin/pslurp -OStrictHostKeyChecking=no -OCheckHostIP=no "
            + "-OIdentityFile=" + keypairPath + " -t 10 -h ips.txt -l ec2-user "
            + "-L models/" + learner + " /home/ec2-user/parameters.properties parameters.properties";
        
        Process parallelSlurpParamsProps_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", parallelSlurpParamsPropsCommand});
        parallelSlurpParamsProps_process.waitFor();

        String parallelSlurpDataPropsCommand = "/usr/bin/pslurp -OStrictHostKeyChecking=no -OCheckHostIP=no "
            + "-OIdentityFile=" + keypairPath + " -t 10 -h ips.txt -l ec2-user "
            + "-L models/" + learner + " /home/ec2-user/data.properties data.properties";
        
        Process parallelSlurpDataProps_process = Runtime.getRuntime().exec(new String[]{"bash" , "-c", parallelSlurpDataPropsCommand});
        parallelSlurpDataProps_process.waitFor();

    }
    
}
