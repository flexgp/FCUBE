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
    String flavor = request.getParameter("flavor");
    String dataPercentage = request.getParameter("datapercentage");
    String learnerName = request.getParameter("learnername");
    String learningTime = request.getParameter("learningtime");
    String firstUpdate = request.getParameter("firstupdate");
    String minutesLeft,runningInstances,countRetrievedModels;
    // add logic to check that parameters are fine
    
    if(firstUpdate.equals("true")){
        // set AWS credentials
        EC2JSPInterface ec2interface = new EC2JSPInterface();
        
        // run FCUBE!!
        //ec2interface.launchDummyInstance();
        ec2interface.generateParameterOptionsFile(trainingFolder,learnerName,dataPercentage);
        ec2interface.logStartTime(learningTime);
        minutesLeft = ec2interface.getRemainingMinutes(learningTime);
        runningInstances = "0";
        countRetrievedModels = "0";
        ec2interface.launchInstances(learnerName, numInstances, learningTime,flavor);
    }else{
        EC2JSPInterface ec2interface = new EC2JSPInterface();
        minutesLeft = ec2interface.getRemainingMinutes(learningTime);
        runningInstances = ec2interface.countRunningInstances();
        ec2interface.retrieveModels(learnerName);
        countRetrievedModels = ec2interface.countRetrievedModels(learnerName);
    }
    
  %>
    
    
<h1><a name="monitor" class="anchor" href="#monitor"><span class="octicon octicon-link"></span></a>FCUBE run monitoring</h1>
<br>
<form method="get" action="monitor.jsp">
    <input type="hidden" name="trainingfolder" value="<%= trainingFolder%>">
    <input type="hidden" name="fusiontraining" value="<%= fusionTraining%>">
    <input type="hidden" name="testdata" value="<%= testData%>">
    <input type="hidden" name="numinstances" value="<%= numInstances%>">
    <input type="hidden" name="flavor" value="<%= flavor%>">
    <input type="hidden" name="datapercentage" value="<%= dataPercentage%>">
    <input type="hidden" name="learnername" value="<%= learnerName%>">
    <input type="hidden" name="learningtime" value="<%= learningTime%>">
    <input type="hidden" name="firstupdate" value="false">
<br>
<table>
<tr><td><label for="trainingfolder">Training Folder:</label> <td><label for="trainingfolder"><%= trainingFolder %></label>
<tr><td><label for="learnername">Learner Name:</label> <td><label for="learnername"><%= learnerName %></label>
<tr><td><label for="datapercentage">Data at each worker:</label> <td><label for="datapercentage"><%= dataPercentage %></label>
<tr><td><label for="launchedinstances">Launched FCUBE workers:</label> <td><label for="launchedinstances"><%= runningInstances %>/<%= numInstances %></label>
<tr><td><label for="flavor">Flavor of FCUBE workers:</label> <td><label for="flavor"><%= flavor %></label>        
<tr><td><label for="learningtime">Learning time:</label> <td><label for="learningtime"><%= learningTime %> minutes</label>
<tr><td><label for="remainingtime">Approximated remaining time:</label> <td><label for="remainingtime"><%= minutesLeft %> minutes</label>
<tr><td><label for="retrievedmodels">Retrieved Models:</label> <td><label for="retrievedmodels"><%= countRetrievedModels %>/<%= numInstances %></label>
</table>
<div class="buttonHolder" style="text-align: center"><input type="submit" value="Update" style="height:30px; width:200px" > </div>
</form>
<br>
<br>
<br>

<h1><a name="fuse" class="anchor" href="#fuse"><span class="octicon octicon-link"></span></a>Fused model statistics</h1>
<br>
<form method="get" action="fusion.jsp">
    
    <input type="hidden" name="trainingfolder" value="<%= trainingFolder%>">
    <input type="hidden" name="numinstances" value="<%= numInstances%>">
    <input type="hidden" name="flavor" value="<%= flavor%>">
    <input type="hidden" name="datapercentage" value="<%= dataPercentage%>">
    <input type="hidden" name="learningtime" value="<%= learningTime%>">
    <input type="hidden" name="fusiontraining" value="<%= fusionTraining%>">
    <input type="hidden" name="testdata" value="<%= testData%>">
    <input type="hidden" name="learnername" value="<%= learnerName%>">
    <input type="hidden" name="runninginstances" value="<%= runningInstances %>">
    <input type="hidden" name="countretrievedmodels" value="<%= countRetrievedModels %>">
    <input type="hidden" name="firstupdate" value="true">
<table>
<tr><td><label for="fusiontrainingdata">Fusion Training Data:</label> <td><label for="fusiontrainingdata"><%= fusionTraining %></label>    
<tr><td><label for="testingdata">Test Data:</label> <td><label for="testingdata"><%= testData %></label>
<tr><td><label for="evaluatedmodels">Evaluated Models:</label> <td><label for="evaluatedmodels">0/0</label>
<tr>        
<tr><td><label for="filteredmodels">Filtered Models:</label> <td><label for="filteredmodels">0</label>
<tr><td><label for="auc">Area under the curve:</label> <td><label for="auc">0</label>        
<tr><td><label for="accuracy">Accuracy (threshold=0.5):</label> <td><label for="accuracy">0</label>
<tr><td><label for="tpr">True Positive Rate (threshold=0.5):</label> <td><label for="tpr">0</label>
<tr><td><label for="fpr">False Positive Rate (threshold=0.5):</label> <td><label for="fpr">0</label>   
</table>
<div class="buttonHolder" style="text-align: center"><input type="submit" value="Fuse models" style="height:30px; width:200px" > </div>
<div class="buttonHolder" style="text-align: center"><input type="submit" value="Download Fused Model" style="height:30px; width:200px" disabled="disabled"> </div>
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