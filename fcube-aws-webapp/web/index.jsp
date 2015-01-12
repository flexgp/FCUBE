<%-- 
    Document   : index
    Created on : Sep 11, 2014, 1:41:51 PM
    Author     : nacho
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <title>FCUBE: run configuration</title>

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


<h1><a name="deploy" class="anchor" href="#deploy"><span class="octicon octicon-link"></span></a>FCUBE learning service</h1>

<form method="get" action="monitor.jsp">

<h3><a name="deploy" class="anchor" href="#deploy"><span class="octicon octicon-link"></span></a>Data</h3>

<table>
<tr><td><label for="trainingfolder">Training data folder:</label> <td><input type="text" name="trainingfolder" id="trainingfolder" size="40">
<tr><td><label for="fusiontraining">Fusion training data:</label>  <td><input type="text" name="fusiontraining" id="fusiontraining" size="40">
<tr><td><label for="testdata">Test data:</label>  <td><input type="text" name="testdata" id="testdata" size="40">
<tr>
</table>
<br>

<h3><a name="deploy" class="anchor" href="#deploy"><span class="octicon octicon-link"></span></a>Learning Strategy</h3>

<table>
<tr><td><label for="numinstances">Number of FCUBE workers:</label> <td><input type="text" name="numinstances" id="numinstances" size="40">
<tr><td><label for="flavor">Flavor of FCUBE workers:</label> <td><input type="text" name="flavor" id="flavor" size="40">        
<tr><td><label for="datapercentage">Data received at each worker (0 to 1)</label>  <td><input type="text" name="datapercentage" id="datapercentage" size="40">
<tr><td><label for="learnername">Learner:</label>  <td><select name="learnername"> <option selected>gpfunction<option>mplcs<option>SBBJ<option>ruletree</select>
<tr><td><label for="learningtime">Learning time (in minutes)</label>  <td><input type="text" name="learningtime" id="learningtime" size="40">
<tr>
</table>
<br>

<input type="hidden" name="firstupdate" value="true">

<br>
<div class="buttonHolder" style="text-align: center"><input type="submit" value="Run FCUBE" style="height:50px; width:400px"> 
                                                     <input type="reset" value="Reset" style="height:50px; width:400px" ></div>

</form>
  

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
