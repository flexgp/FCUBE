<%-- 
    Document   : index
    Created on : Sep 11, 2014, 1:41:51 PM
    Author     : nacho
--%>

<%@page import="MenuManager.EC2JSPInterface"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <title>FCUBE: run monitoring</title>

    <link rel="stylesheet" href="stylesheets/styles.css">
    <link rel="stylesheet" href="stylesheets/pygment_trac.css">
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no">
    <!--[if lt IE 9]>
    <script src="//html5shiv.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
  </head>
  <body>
    <div class="wrapper">
      <header>
        <h1><a target="_blank" href="http://flexgp.github.io/FCUBE/">FCUBE</a></h1>
        <p></p>
        <p class="view"><a target="_blank" href="https://github.com/flexgp/FCUBE">View the Project on GitHub</a></p>
      </header>

      <section>

<%
    
    String trainingFolder = request.getParameter("trainingfolder");
    String fusionTraining = request.getParameter("fusiontraining");
    String testData = request.getParameter("testdata");
    String numInstances = request.getParameter("numinstances");
    String dataPercentage = request.getParameter("datapercentage");
    String learnerName = request.getParameter("learnername");
    String learningTime = request.getParameter("learningtime");
    String firstUpdate = request.getParameter("firstupdate");
    String countRetrievedModels = request.getParameter("countretrievedmodels");
    String runningInstances = request.getParameter("runninginstances");
    
    String countEvalModels = "0";
    String filteredModels = "0";
    String auc = "0";
    String accuracy = "0";
    String truePositiveRate = "0";
    String falsePositiveRate = "0";
    String downloadButton = " disabled=\"disabled\" ";
            
    if(firstUpdate.equals("true")){
        EC2JSPInterface ec2interface = new EC2JSPInterface();
        ec2interface.copyFusionAndTestFiles(fusionTraining,testData);
        ec2interface.filterFuseModels(learnerName);
    }else{
        EC2JSPInterface ec2interface = new EC2JSPInterface();
        countEvalModels = ec2interface.countEvalModels(learnerName);
        filteredModels = ec2interface.countFilteredModels();
        String[] stats = new String[4];
        ec2interface.readFusedStats(stats);
        auc = stats[0];
        accuracy = stats[1];
        truePositiveRate = stats[2];
        falsePositiveRate = stats[3];
        
        // check if download should be available
        //      prepare zip with models
        downloadButton = ec2interface.zipModel();
    }
    
  %>
    
    
<h1><a name="monitor" class="anchor" href="#monitor"><span class="octicon octicon-link"></span></a>FCUBE run monitoring</h1>
<br>
<form method="get" action="monitor.jsp">
<br>
<table>
<tr><td><label for="trainingfolder">Training Folder:</label> <td><label for="trainingfolder"><%= trainingFolder %></label>
<tr><td><label for="learnername">Learner Name:</label> <td><label for="learnername"><%= learnerName %></label>
<tr><td><label for="datapercentage">Data per Instance:</label> <td><label for="datapercentage"><%= dataPercentage %></label>        
<tr><td><label for="launchedinstances">Launched Instances:</label> <td><label for="launchedinstances"><%= runningInstances %>/<%= numInstances %></label>
<tr><td><label for="learningtime">Learning time:</label> <td><label for="learningtime"><%= learningTime %> minutes</label>
<tr><td><label for="remainingtime">Approximated remaining time:</label> <td><label for="remainingtime">0 minutes</label>
<tr><td><label for="retrievedmodels">Retrieved Models:</label> <td><label for="retrievedmodels"><%= countRetrievedModels %>/<%= numInstances %></label>        
</table>
<div class="buttonHolder" style="text-align: center"><input type="submit" value="Update" style="height:30px; width:200px" disabled="disabled"> </div>
</form>
<br>
<br>
<br>

<h1><a name="fuse" class="anchor" href="#fuse"><span class="octicon octicon-link"></span></a>Fused model statistics</h1>
<br>
<form method="get" action="fusion.jsp">
    <input type="hidden" name="trainingfolder" value="<%= trainingFolder%>">
    <input type="hidden" name="fusiontraining" value="<%= fusionTraining%>">
    <input type="hidden" name="testdata" value="<%= testData%>">
    <input type="hidden" name="learnername" value="<%= learnerName%>">
    <input type="hidden" name="numinstances" value="<%= numInstances%>">
    <input type="hidden" name="datapercentage" value="<%= dataPercentage%>">
    <input type="hidden" name="learningtime" value="<%= learningTime%>">
    <input type="hidden" name="countretrievedmodels" value="<%= countRetrievedModels%>">
    <input type="hidden" name="runninginstances" value="<%= runningInstances%>">
    <input type="hidden" name="firstupdate" value="false">
    
<table>
<tr><td><label for="fusiontrainingdata">Fusion Training Data:</label> <td><label for="fusiontrainingdata"><%= fusionTraining %></label>    
<tr><td><label for="testingdata">Test Data:</label> <td><label for="testingdata"><%= testData %></label>
<tr><td><label for="evaluatedmodels">Evaluated Models:</label> <td><label for="evaluatedmodels"><%= countEvalModels %>/<%= countRetrievedModels %></label>
<tr>        
<tr><td><label for="filteredmodels">Filtered Models:</label> <td><label for="filteredmodels"><%= filteredModels %></label>
<tr><td><label for="auc">Area under the curve:</label> <td><label for="auc"><%= auc %></label>        
<tr><td><label for="accuracy">Accuracy (threshold=0.5):</label> <td><label for="accuracy"><%= accuracy %></label>
<tr><td><label for="tpr">True Positive Rate (threshold=0.5):</label> <td><label for="tpr"><%= truePositiveRate %></label>
<tr><td><label for="fpr">False Positive Rate (threshold=0.5):</label> <td><label for="fpr"><%= falsePositiveRate %></label>        
</table>
<div class="buttonHolder" style="text-align: center"><input type="submit" value="Update" style="height:30px; width:200px" > </div>
<!-- 
<div class="buttonHolder" style="text-align: center"><input type="submit" value="Download Fused Model" style="height:30px; width:200px" <%= downloadButton %>> </div>
-->
</form>
<form method="get" action="download.zip">
    <div class="buttonHolder" style="text-align: center"><input type="submit" value="Download Fused Model" style="height:30px; width:200px" <%= downloadButton %>> </div>
</form>
<br>
<br>
<br>

<p>This project is developed is by the <a target="_blank" href="http://groups.csail.mit.edu/EVO-DesignOpt/groupWebSite/">Any-Scale Learning For All (ALFA)</a> group at MIT.</p>
<p>Contact us by email at <a target="_blank" href="mailto:iarnaldo@mit.edu">iarnaldo@mit.edu</a></p>
<center><img src="images/ALFA-logo-lousy.png" alt="ALFA" align="middle"></center></p>

</section>


<footer>
<p>This project is maintained by <a target="_blank" href="https://github.com/flexgp">flexgp</a></p>
</footer>
    </div>
    <script src="javascripts/scale.fix.js"></script>
    
  </body>
</html>