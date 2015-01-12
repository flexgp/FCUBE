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
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * This class terminates the EC2 instances
 * @author Ignacio Arnaldo
 */
public class TerminateEC2 {
    
    String ami;
    
    /**
     * Constructor for the terminate class
     * @param anAmi 
     */
    public TerminateEC2(String anAmi){
        ami = anAmi;
    }
    
    /**
     * This method terminates the running instances
     * The external command are:
     * 
     * aws ec2 describe-instances --filters Name=image-id,Values=ami-bb9f80d2 | grep InstanceId | cut -d'|' -f'5'
     * aws ec2 terminate-instances --instance-ids i-4ed5531d
     * 
     * @throws IOException
     * @throws InterruptedException 
     */
    public void terminateInstances() throws IOException, InterruptedException{
            String getNames_cmd = "aws ec2 describe-instances --filters Name=image-id,Values=" + ami + " | grep InstanceId | cut -d'|' -f'5'";
            Process getNames_process = Runtime.getRuntime().exec(new String[]{"bash","-c",getNames_cmd});
            getNames_process.waitFor();
            BufferedReader  br = new BufferedReader(new InputStreamReader(getNames_process.getInputStream()));

            String line=null;
            while((line=br.readLine())!=null){
                line = line.trim();
                String deleteCommand = "aws ec2 terminate-instances --instance-ids " + line;
                Process delete_process = Runtime.getRuntime().exec(new String[]{"bash","-c",deleteCommand});
                delete_process.waitFor();
            }
        
    }
}
