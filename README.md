# force-metadata-jdbc-driver

## Introduction

This Java code allows a Salesforce org's schema to be exported via the wonderful [SchemaSpy](http://schemaspy.sourceforge.net/) that produces elegantly formatted ERDs linked together by web pages containing all the detail. It was originally shared via this [Google Code Project](https://code.google.com/archive/p/force-metadata-jdbc-driver/), but that moved into an archive mode in 2016.

An example diagram:

![Sample ERD](sample-erd.png)

## Building

Use Ant to generate the Jar file: the script merges all the classes into one Jar file. Or use your favourite Java IDE to accomplish the same thing.

## Using

[SchemaSpy](http://schemaspy.sourceforge.net/) is a Java program and so can be run in a variety of ways. Details are given below for using it from Ant. SchemaSpy itself delegates to the (non-Java) dot executable from [Graphviz](http://www.graphviz.org/) so that needs to be installed on the machine you are running SchemaSpy on and added to the path. (Check that this is working by running "dot -V" from the command-line which should just output some version information.)

Salesforce objects are treated as tables and Salesforce fields as columns. Note that formula fields return a size of 1300, the maximum size of the formula string. Picklist values, record type names and the "to many" relationship name are displayed in the SchemaSpy comments column.
