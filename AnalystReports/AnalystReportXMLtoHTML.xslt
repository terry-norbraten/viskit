<?xml version="1.0" encoding="UTF-8"?>
<!--
  <head>
   <meta name="filename"    content="AnalystReportXMLtoHTML.xslt" />
   <meta name="author"      content="Patrick Sullivan" />
   <meta name="created"     content="21 July 2006" />
   <meta name="description" content="XSLT stylesheet, converts AnalystReportXML output into xhtml format>
   <meta name="version"     content="$Id: AnalystReportXMLtoHTML.xslt 1620 2007-11-20 07:23:43Z tdnorbra $"/>
  </head>  
-->
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:java="http://xml.apache.org/xslt/java">
    <xsl:strip-space elements="*"/>
    <xsl:output method="xml"
                encoding="UTF-8"
                omit-xml-declaration="no"
                doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
                doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"
                cdata-section-elements="Script"
                indent="yes"
                media-type="text/html"
                xalan:indent-amount="2"/>
    
    <!-- Attempt to establish an incremental counter variable -->
    <!-- From: http://osdir.com/ml/text.xml.xalan.java.user/2006-05/msg00014.html -->
    <xsl:variable name="javaCounter" select="java:java.util.ArrayList.new()"/>    
    <xsl:template match="/">               
        <html>
            <xsl:comment>
                <xsl:text>Generated using XSLT processor: </xsl:text>
                <xsl:value-of select="system-property('xsl:vendor')"/>
            </xsl:comment> 
            <head>                
                <title>Viskit Simulation Analysis Report</title>
                <meta http-equiv="Content-Language" content="en-us"/>
                <meta http-equiv="Content-Type" content="text/html; charset=windows-1252"/>
                <link rel="shortcut icon" href="Viskit.ico" type="image/x-icon"/>
                <style type="text/css">
                    <!-- Limit the width of the entire HTML page to be printer friendly -->
                    body {width: 1024px} 
                </style>
            </head>
            <body>
                <!-- Link anchor for the top of the page -->
                <a name="top"/>
                <!--Title information-->
                <xsl:apply-templates select="/AnalystReport"/>
                <!-- Bookmarks for various sections of the report -->
                <center>
                    <p>
                        <font size="-1">
                            <a href="#ExecutiveSummary">Executive Summary</a> |
                            <a href="#SimulationLocation">Simulation Location</a> |
                            <a href="#AssemblyConfiguration">Assembly Configuration for Viskit Simulation</a> |
                            <a href="#EntityParameters">Entity Parameters</a> |
                            <a href="#BehaviorDescriptions">Behavior Descriptions</a> |
                            <a href="#StatisticalResults">Statistical Results</a> |
                            <a href="#Conclusions">Conclusions and Recommendations</a>
                        </font>
                    </p>
                </center>
                <hr/>
                <!--Executive Summary: NOTE - omitted if analyst did not include comments  -->
                <xsl:apply-templates select="//ExecutiveSummary"/>
                <p/>
                <p/>
                <hr/>
                <!--Simulation Location-->
                <xsl:apply-templates select="//SimulationLocation"/>
                <p/>
                <p/>
                <hr/>
                <!--Simulation Configuration-->
                <xsl:apply-templates select="//SimulationConfiguration" mode="ConfigHeader"/>
                <p/>
                <p/>
                <hr/>
                <!--Entity Parameters -->
                <xsl:apply-templates select="//EntityParameters" mode="ParamHeader"/>
                <xsl:apply-templates select="//EntityParameters/ParameterTables/EntityParameterTable"/>
                <p/>
                <p/>
                <hr/>
                <!--Behavior Descriptions -->
                <xsl:apply-templates select="//BehaviorDescriptions" mode="BehaviorHeader"/>
                <xsl:apply-templates select="//BehaviorDescriptions/BehaviorList"/>
                <p/>
                <p/>
                <hr/>
                <!--Statistical Reports-->
                <xsl:apply-templates select="//StatisticalResults" mode="StatsHeader"/>
                <xsl:apply-templates select="//StatisticalResults/ReplicationReports/SimEntity" mode="RepStats"/>
                <xsl:apply-templates select="//StatisticalResults/SummaryReport" mode="SumStats"/>
                <p/>
                <p/>
                <hr/>
                <!--Conclusions Recommendations -->
                <xsl:apply-templates select="//ConclusionsRecommendations"/>
                <!-- add Viskit/Simkit credit footer -->
                <p/>
                <p/>
                <hr/>
                <p>
                    This report was autogenerated by the Viskit Event Graph and Assembly 
                    modeling tool using Simkit discrete-event simulation (DES) libraries.  
                    Online at <a href="https://diana.nps.edu/Viskit">https://diana.nps.edu/Viskit</a>
                    and <a href="https://diana.nps.edu/Simkit">https://diana.nps.edu/Simkit</a>.
                </p>
            </body>
        </html>
    </xsl:template>
    
    <!--Title information template-->
    <xsl:template match="AnalystReport">
        <p align="center">
            <font size="2">
                <b>***</b>
                THIS REPORT IS:
                <b>
                    <b>
                        <xsl:value-of select="@classification"/>
                        ***
                    </b>
                </b>
            </font>
        </p>
        <p align="center">
            <font size="6">
                <xsl:value-of select="@name"/>
            </font>
        </p>
        <p align="center">Analyst:
            <b>
                <xsl:value-of select="@author"/>
            </b>
        </p>
        <p align="center">Analysis date:
            <b>
                <xsl:value-of select="@date"/>
            </b>
        </p>        
    </xsl:template>
    
    <!--Executive Summary template -->
    <xsl:template match="ESComments">
        <p align="left">
            <b><a name="ExecutiveSummary">Executive Summary</a></b>
        </p>
        <p align="left">            
            <i>Analyst Executive Summary.</i>            
            <font color="#00006C">
                <xsl:value-of select="@text"/>
            </font>
        </p>
        <a href="#top"><font size="-1" color="#990000">Back to top</font></a>
    </xsl:template>
    
    <!--Simulation Location templates -->
    <xsl:template match="SLComments">
        <p align="left">
            <b><a name="SimulationLocation">Simulation Location</a></b>
        </p>
        <p align="left">            
            <i>Description of Location Features.</i>            
            <font color="#00006C">
                <xsl:value-of select="@text"/>
            </font>
        </p>
    </xsl:template>
    <xsl:template match="SLConclusions">
        <p align="left">            
            <i>Post-Experiment Analysis of Significant Location Features.</i>            
            <font color="#00006C">
                <xsl:value-of select="@text"/>
            </font>
        </p>
    </xsl:template>
    <xsl:template match="LocationImage">
        <div align="center">
            <xsl:element name="a">
                <xsl:attribute name="href">
                    <xsl:text disable-output-escaping="yes">file:///</xsl:text>
                    <xsl:value-of select="@dir"/>
                </xsl:attribute>
                <xsl:attribute name="style">
                    <xsl:text>border:0</xsl:text>
                </xsl:attribute>
                <xsl:element name="img">
                    <xsl:attribute name="border">
                        <xsl:text>1</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="src">
                        <xsl:text disable-output-escaping="yes">file:///</xsl:text>
                        <xsl:value-of select="@dir"/>
                    </xsl:attribute>
                    <xsl:attribute name="description">
                        <!-- TODO:  more info here -->
                        <xsl:text>location</xsl:text>
                    </xsl:attribute>
                </xsl:element>
            </xsl:element>
            
            <!-- add an index to the array -->
            <xsl:variable name="addIndex" select="java:add($javaCounter, '1')"/>            
            <p>Figure <xsl:number value="java:size($javaCounter)" format="1"/>: 2D Overview of Study Area</p>
        </div>
        <a href="#top"><font size="-1" color="#990000">Back to top</font></a>
    </xsl:template>
    <xsl:template match="ChartImage">
        
        <!-- DNC views have to be manually screen captured.  Test for existence first -->
        <xsl:if test="@dir != ''">
            <p/>
            <p/>
            <div align="center">
                <xsl:element name="a">
                    <xsl:attribute name="href">
                        <xsl:text disable-output-escaping="yes">file:///</xsl:text>
                        <xsl:value-of select="@dir"/>
                    </xsl:attribute>
                    <xsl:attribute name="style">
                        <xsl:text>border:0</xsl:text>
                    </xsl:attribute>
                    <xsl:element name="img">
                        <xsl:attribute name="border">
                            <xsl:text>1</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="src">
                            <xsl:text disable-output-escaping="yes">file:///</xsl:text>
                            <xsl:value-of select="@dir"/>
                        </xsl:attribute>
                        
                        <!-- Clamp the width of potentially big chart images -->
                        <xsl:attribute name="width">
                            <xsl:text>800px</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="description">
                            <!-- TODO:  more info here -->
                            <xsl:text>location</xsl:text>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:element>
            
                <!-- add an index to the array -->
                <xsl:variable name="addIndex" select="java:add($javaCounter, '1')"/>            
                <p>Figure <xsl:number value="java:size($javaCounter)" format="1"/>: Digital Nautical Chart (DNC) View of Study Area</p>
            </div>
            <a href="#top"><font size="-1" color="#990000">Back to top</font></a>
        </xsl:if>
    </xsl:template>
    
    <!-- SimulationConfiguration templates -->
    <xsl:template match="SCComments" mode="ConfigHeader">
        <p align="left">
            <b><a name="AssemblyConfiguration">Assembly Configuration for Viskit Simulation</a></b>
        </p>
        <p align="left">            
            <i>Assembly Design Considerations.</i>            
            <font color="#00006C">
                <xsl:value-of select="@text"/>
            </font>
        </p>
    </xsl:template>
    <xsl:template match="SCConclusions" mode="ConfigHeader">
        <p align="left">            
            <i>Post-Experiment Analysis of Simulation Assembly Design.</i>            
            <font color="#000099">
                <xsl:value-of select="@text"/>
            </font>
        </p>
        <p align="center">
            <b>Summary of Simulation Entities</b>
        </p>
        <div align="center">
            <table border="1">
                <tr>
                    <td bgcolor="#FFFFCC">Simulation Entity</td>
                    <td bgcolor="#FFFFCC">Behavior Definitions</td>
                </tr>
                <xsl:apply-templates select="//SimulationConfiguration/EntityTable/SimEntity" mode="EntitiesTable"/>
            </table>
        </div>
        <p/>
        <p/>
    </xsl:template>
    <xsl:template match="AssemblyImage" mode="ConfigHeader">
        <div align="center">
            <xsl:element name="a">
                <xsl:attribute name="href">
                    <xsl:text disable-output-escaping="yes">file:///</xsl:text>
                    <xsl:value-of select="@dir"/>
                </xsl:attribute>
                <xsl:attribute name="style">
                    <xsl:text>border:0</xsl:text>
                </xsl:attribute>
                <xsl:element name="img">
                    <xsl:attribute name="border">
                        <xsl:text>1</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="src">
                        <xsl:text disable-output-escaping="yes">file:///</xsl:text>
                        <xsl:value-of select="@dir"/>
                    </xsl:attribute>
                    <xsl:attribute name="title">
                        <xsl:text>Assembly graph</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="alt">
                        <xsl:text>Assembly graph</xsl:text>
                    </xsl:attribute>
                </xsl:element>
            </xsl:element>
            
            <!-- add an index to the array -->
            <xsl:variable name="addIndex" select="java:add($javaCounter, '1')"/>            
            <p>Figure <xsl:number value="java:size($javaCounter)" format="1"/>: Simulation Assembly</p>
        </div>
        <a href="#top"><font size="-1" color="#990000">Back to top</font></a>
    </xsl:template>  
    <xsl:template match="SimEntity" mode="EntitiesTable">
        <tr>
            <td>
                <!-- Now link to each Simulation Entity in this Table -->
                <xsl:element name="a">
                    <xsl:attribute name="href">
                        <xsl:text>#</xsl:text>
                        <xsl:value-of select="@name"/>
                    </xsl:attribute>
                    <xsl:value-of select="@name"/>
                </xsl:element>
            </td>
            <td>                
                <!-- then link to each Behavior Definition -->
                <xsl:element name="a">
                    <xsl:attribute name="href">
                        <xsl:text>#</xsl:text>
                        <xsl:value-of select="@fullyQualifiedName"/>
                    </xsl:attribute>
                    <xsl:value-of select="@fullyQualifiedName"/>
                </xsl:element>
            </td>
        </tr>
    </xsl:template>
    
    <!--EntityParameter templates-->
    <xsl:template match="EPComments" mode="ParamHeader">
        <p align="left">
            <b><a name="EntityParameters">Entity Parameters</a></b>
        </p>
        <p align="left">            
            <i>Entity Parameters Overview.</i>            
            <font color="#00006C">
                <xsl:value-of select="@text"/>
            </font>
        </p>
    </xsl:template>
    <!-- not used
  <xsl:template match="EPConclusions" mode="ParamHeader">
    <p align="left">      
        <i>Post-Experiment Analysis of Entity Behaviors.</i>      
      <font color="#00006C">
        <xsl:value-of select="@text"/>
      </font>
    </p>
  </xsl:template>
  -->

    <!-- Entity Parameter Tables -->
    <xsl:template match="EntityParameterTable">
        <p/>
        <p/>
        <p/>
        <xsl:text>Simulation Parameters for initializing </xsl:text>
        <b>
            <a>
                <xsl:attribute name="name">
                    <xsl:value-of select="@name"/>
                </xsl:attribute>
                <xsl:value-of select="@name"/>
            </a>
        </b>
        <!--  TODO:  add uniquely identifying information for this header -->
        <table border="1" width="75%" cellpadding="0" cellspacing="1">
            
            <!-- Classification Values -->
            <xsl:for-each select="Classification">
                <tr>
                    <td width="132" bgcolor="#CCCCCC">Classification</td>
                    <td width="132" bgcolor="#CCCCCC"/>
                    <td width="190" bgcolor="#CCCCCC"/>
                    <td width="132" bgcolor="#CCCCCC">Candidate Factor</td>
                </tr>
                <xsl:for-each select="parameter">
                    <tr>
                        <td width="132"/>
                        <td width="190">
                            <xsl:value-of select="@name"/>
                        </td>
                        <td>
                            <xsl:value-of select="@value"/>
                        </td>
                        <td width="132"/>
                    </tr>
                </xsl:for-each>
            </xsl:for-each>
            
            <!--Identification Values -->
            <xsl:for-each select="Identification">
                <tr>
                    <td width="132" bgcolor="#CCCCCC">Identification</td>
                    <td width="132" bgcolor="#CCCCCC"/>
                    <td width="190" bgcolor="#CCCCCC"/>
                    <td width="132" bgcolor="#CCCCCC"/>                    
                </tr>
                <xsl:for-each select="parameter">
                    <tr>
                        <td width="132"/>
                        <td width="190">
                            <xsl:value-of select="@name"/>
                        </td>
                        <td>
                            <xsl:value-of select="@value"/>
                        </td>
                        <td width="132"/>
                    </tr>
                </xsl:for-each>
            </xsl:for-each>
            
            <!--Physical Constraints -->
            <xsl:for-each select="PhysicalConstraints">
                <tr>
                    <td width="132" bgcolor="#CCCCCC">Physical Constraints</td>
                    <td width="132" bgcolor="#CCCCCC"/>
                    <td width="190" bgcolor="#CCCCCC"/>
                    <td width="132" bgcolor="#CCCCCC"/>
                </tr>
                <xsl:for-each select="parameter">
                    <tr>
                        <td width="132"/>
                        <td width="190">
                            <xsl:value-of select="@name"/>
                        </td>
                        <td>
                            <xsl:value-of select="@value"/>
                        </td>
                        <td width="132"/>
                    </tr>
                </xsl:for-each>
            </xsl:for-each>
            
            <!-- Dynamic Response Constraints -->
            <xsl:for-each select="DynamicResponseConstraints">
                <tr>
                    <td width="132" bgcolor="#CCCCCC">Dynamic Response Constraints</td>
                    <td width="132" bgcolor="#CCCCCC"/>
                    <td width="190" bgcolor="#CCCCCC"/>
                    <td width="132" bgcolor="#CCCCCC"/>
                </tr>
                <xsl:for-each select="parameter">
                    <tr>
                        <td width="132"></td>
                        <td width="190">
                            <xsl:value-of select="@name"/>
                        </td>
                        <td>
                            <xsl:value-of select="@value"/>
                        </td>
                        <td width="132"></td>
                    </tr>
                </xsl:for-each>
            </xsl:for-each>
            
            <!--Tactical Constraints -->
            <xsl:for-each select="TacticalConstraints">
                <tr>
                    <td width="132" bgcolor="#CCCCCC">Tactical Constraints</td>
                    <td width="132" bgcolor="#CCCCCC"/>
                    <td width="190" bgcolor="#CCCCCC"/>
                    <td width="132" bgcolor="#CCCCCC"/>
                </tr>
                <xsl:for-each select="parameter">
                    <tr>
                        <td width="132"></td>
                        <td width="190">
                            <xsl:value-of select="@name"/>
                        </td>
                        <td>
                            <xsl:value-of select="@value"/>
                        </td>
                        <td width="132"></td>
                    </tr>
                </xsl:for-each>
            </xsl:for-each>
        </table>
        <p/>
        <p/>        
        <a href="#top"><font size="-1" color="#990000">Back to top</font></a>
    </xsl:template>
    
    <!--Behavior Description templates-->
    <xsl:template match="BDComments" mode="BehaviorHeader">
        <p align="left">
            <b><a name="BehaviorDescriptions">Behavior Descriptions</a></b>
        </p>
        <p align="left">
            <i>Description of Behavior Design.</i>
            <font color="#00006C">
                <xsl:value-of select="@text"/>
            </font>
        </p>
    </xsl:template>
    <xsl:template match="BDConclusions" mode="BehaviorHeader">
        <p align="left">
            <i>Post-Experiment Analysis of Entity Behaviors.</i>
            <font color="#00006C">
                <xsl:value-of select="@text"/>
            </font>
        </p>
    </xsl:template>
    
    <!-- Event Graph image and details -->
    <xsl:template match="Behavior">
        
        <!-- Capture this Behavior's name for the Figure Caption -->
        <xsl:variable name="behavior" select="@name"/>
        <p/>
        <p/>
        <p align="left">
            <b>Behavior: </b>
            <xsl:element name="a">
                <xsl:attribute name="name">
                    <xsl:value-of select="@name"/>
                </xsl:attribute>
                <xsl:value-of select="@name"/>
            </xsl:element>
        </p>
        
        <!--Add the description -->
        <xsl:for-each select="description">
            <p align="left">
                <b>Description: </b>
                <xsl:value-of select="@text"/>
            </p>
        </xsl:for-each>
        
        <!-- Add the image of the Event Ggraph -->
        <xsl:for-each select="EventGraphImage">
            <div align="center">
                <xsl:element name="a">
                    <xsl:attribute name="href">
                        <xsl:text disable-output-escaping="yes">file:///</xsl:text>
                        <xsl:value-of select="@dir"/>
                    </xsl:attribute>
                    <xsl:attribute name="style">
                        <xsl:text>border:0</xsl:text>
                    </xsl:attribute>
                    <xsl:element name="img">
                        <xsl:attribute name="border">
                            <xsl:text>1</xsl:text>
                        </xsl:attribute>
                        <xsl:attribute name="src">
                            <xsl:text disable-output-escaping="yes">file:///</xsl:text>              
                            <xsl:value-of select="@dir"/>
                        </xsl:attribute>
                        <xsl:attribute name="description">
                            <!-- TODO:  more info here -->
                            <xsl:text>location</xsl:text>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:element>
               
                <!-- add an index to the array -->
                <xsl:variable name="addIndex" select="java:add($javaCounter, '1')"/>            
                <p>Figure <xsl:number value="java:size($javaCounter)" format="1"/>: Event Graph for <xsl:value-of select="$behavior"/></p>
            </div>
        </xsl:for-each>
        <div align="center">
            <table border="1">
                <tr>
                    <td bgcolor="#FFFFCC">Parameter</td>
                    <td bgcolor="#FFFFCC">Parameter Type</td>
                    <td bgcolor="#FFFFCC">Description</td>
                </tr>
                <!--Add parameter and state variable table for each event graph-->
                <xsl:for-each select="parameter">
                    <tr>
                        <td>
                            <xsl:value-of select="@name"/>
                        </td>
                        <td>
                            <xsl:value-of select="@type"/>
                        </td>
                        <td>
                            <xsl:value-of select="@description"/>
                        </td>
                    </tr>
                </xsl:for-each>
            </table>
        </div>
        <p/>
        <p/>
        <div align="center">
            <table border="1">
                <tr>
                    <td bgcolor="#FFFFCC">State Variable</td>
                    <td bgcolor="#FFFFCC">Variable Type</td>
                    <td bgcolor="#FFFFCC">Description</td>
                </tr>
                <xsl:for-each select="stateVariable">
                    <tr>
                        <td>
                            <xsl:value-of select="@name"/>
                        </td>
                        <td>
                            <xsl:value-of select="@type"/>
                        </td>
                        <td>
                            <xsl:value-of select="@description"/>
                        </td>
                    </tr>
                </xsl:for-each>
            </table>
        </div>
        <p/>
        <p/>
        <a href="#top"><font size="-1" color="#990000">Back to top</font></a>
    </xsl:template>
    
    <!-- Statistical Results -->
    <xsl:template match="SRComments" mode="StatsHeader">
        <p align="left">
            <b><a name="StatisticalResults">Statistical Results</a></b>
        </p>
        <p align="left">            
            <i>Description of Expected Results.</i>            
            <font color="#00006C">
                <xsl:value-of select="@text"/>
            </font>
        </p>
    </xsl:template>
    <xsl:template match="SRConclusions" mode="StatsHeader">
        <p align="left">            
            <i>Analysis of Experimental Results.</i>            
            <font color="#00006C">
                <xsl:value-of select="@text"/>
            </font>
        </p>
    </xsl:template>
    <xsl:template match="SimEntity" mode="RepStats">
                
        <!-- Capture this Entity's name for its Figure Caption -->
        <xsl:variable name="entityProperty" select="@property"/>
        <p/>
        <p align="left">            
            <b>Replication Report</b>            
        </p>
        <p align="left">Entity:
            <xsl:value-of select="@name"/>
            <p align="left">Property:
                <xsl:value-of select="@property"/>
            </p>
            <xsl:for-each select="chartURL">
                <div align="center">
                    <xsl:element name="a">
                        <xsl:attribute name="href">
                            <xsl:value-of select="@dir"/>
                        </xsl:attribute>
                        <xsl:attribute name="style">
                            <xsl:text>border:0</xsl:text>
                        </xsl:attribute>
                        <xsl:element name="img">
                            <xsl:attribute name="border">
                                <xsl:text>1</xsl:text>
                            </xsl:attribute>
                            <xsl:attribute name="src">
                                <xsl:value-of select="@dir"/>
                            </xsl:attribute>
                            <xsl:attribute name="description">
                                <!-- TODO:  better description -->
                                <xsl:text>statistical output</xsl:text>
                            </xsl:attribute>
                        </xsl:element>
                    </xsl:element>
                    
                    <!-- add an index to the array -->
                    <xsl:variable name="addIndex" select="java:add($javaCounter, '1')"/>            
                    <p>Figure <xsl:number value="java:size($javaCounter)" format="1"/>: Replication Statistics for <xsl:value-of select="$entityProperty"/></p>
                </div>
            </xsl:for-each>
            <div align="center">
                <table border="1" width="60%">
                    <tr>
                        <td bgcolor="#FFFFCC">
                            <b>Run#</b>
                        </td>
                        <td bgcolor="#FFFFCC">
                            <b>Count</b>
                        </td>
                        <td bgcolor="#FFFFCC">
                            <b>Min</b>
                        </td>
                        <td bgcolor="#FFFFCC">
                            <b>Max</b>
                        </td>
                        <td bgcolor="#FFFFCC">
                            <b>Mean</b>
                        </td>
                        <td bgcolor="#FFFFCC">
                            <b>StdDev</b>
                        </td>
                        <td bgcolor="#FFFFCC">
                            <b>Variance</b>
                        </td>
                    </tr>
                    <xsl:for-each select="Replication">
                        <tr>
                            <td>
                                <xsl:value-of select="@number"/>
                            </td>
                            <td>
                                <xsl:value-of select="@count"/>
                            </td>
                            <td>
                                <xsl:value-of select="@minObs"/>
                            </td>
                            <td>
                                <xsl:value-of select="@maxObs"/>
                            </td>
                            <td>
                                <xsl:value-of select="@mean"/>
                            </td>
                            <td>
                                <xsl:value-of select="@stdDeviation"/>
                            </td>
                            <td>
                                <xsl:value-of select="@variance"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                </table>
            </div>
        </p>
        <p/>
        <a href="#top"><font size="-1" color="#990000">Back to top</font></a>
    </xsl:template>
    <xsl:template match="SummaryReport" mode="SumStats">
        <p/>
        <p align="left">            
            <b><a name="SummaryReport">Summary Report</a></b>            
        </p>
        <div align="left">
            <table border="1" width="80%">
                <tr>
                    <td bgcolor="#FFFFCC">
                        <b>Entity</b>
                    </td>
                    <td bgcolor="#FFFFCC">
                        <b>Property</b>
                    </td>
                    <td bgcolor="#FFFFCC">
                        <b>Count</b>
                    </td>
                    <td bgcolor="#FFFFCC">
                        <b>Min</b>
                    </td>
                    <td bgcolor="#FFFFCC">
                        <b>Max</b>
                    </td>
                    <td bgcolor="#FFFFCC">
                        <b>Mean</b>
                    </td>
                    <td bgcolor="#FFFFCC">
                        <b>StdDev</b>
                    </td>
                    <td bgcolor="#FFFFCC">
                        <b>Variance</b>
                    </td>
                </tr>
                <xsl:for-each select="SummaryRecord">
                    <tr>
                        <td>
                            <xsl:value-of select="@entity"/>
                        </td>
                        <td>
                            <xsl:value-of select="@property"/>
                        </td>
                        <td>
                            <xsl:value-of select="@count"/>
                        </td>
                        <td>
                            <xsl:value-of select="@minObs"/>
                        </td>
                        <td>
                            <xsl:value-of select="@maxObs"/>
                        </td>
                        <td>
                            <xsl:value-of select="@mean"/>
                        </td>
                        <td>
                            <xsl:value-of select="@stdDeviation"/>
                        </td>
                        <td>
                            <xsl:value-of select="@variance"/>
                        </td>
                    </tr>
                </xsl:for-each>
            </table>
        </div>
        <p/>
        <a href="#top"><font size="-1" color="#990000">Back to top</font></a>
    </xsl:template>
    
    <!--Conclusions Recommendations -->
    <xsl:template match="CRComments">
        <p align="left">
            <b><a name="Conclusions">Conclusions and Recommendations</a></b>
        </p>
        <p align="left">            
            <i>Conclusions.</i>            
            <font color="#00006C">
                <xsl:value-of select="@text"/>
            </font>
        </p>
    </xsl:template>
    <xsl:template match="CRConclusions">
        <p align="left">            
            <i>Recommendations for Future Work.</i>            
            <font color="#00006C">
                <xsl:value-of select="@text"/>
            </font>
        </p>
        <a href="#top"><font size="-1" color="#990000">Back to top</font></a>
    </xsl:template>    
</xsl:stylesheet>