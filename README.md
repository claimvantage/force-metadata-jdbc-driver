# force-metadata-jdbc-driver

## Introduction

This Java code allows a Salesforce org's schema to be exported via the wonderful [SchemaSpy](http://schemaspy.sourceforge.net/) that produces multiple elegantly formatted ERDs linked together by web pages containing all the detail. It was originally shared via this [Google Code Project](https://code.google.com/archive/p/force-metadata-jdbc-driver/), but that moved into an archive mode in 2016.

An example diagram:

![Sample ERD](sample-erd.png)

## Building

Use Ant to generate the Jar file: the script merges all the classes into one Jar file. Or use your favourite Java IDE to accomplish the same thing. The code will now compile using Java 1.8.

## Using

### Running SchemaSpy and its arguments

Here is how to generate the SchemaSpy output for your Salesforce org:

* Download the [SchemaSpy jar](http://schemaspy.sourceforge.net/)
* Download and install [Graphviz](https://graphviz.gitlab.io/download/) that is used by SchemaSpy to create the automatically laid out diagrams; multiple platforms including Windows and Mac are supported but the Windows version is the easiest to install
* Build the Force Metadata JDBC driver jar (see previous section)
* In the folder that contains the jars just enter this (replacing the arguments that start with "My" with your own values and entering it all on one line):
```
java -cp schemaSpy_5.0.0.jar;force-metadata-jdbc-driver-2.3.jar net.sourceforge.schemaspy.Main -t force -u MyUserName -p MyPasswordAndSecurityToken -font Arial -fontsize 8 -hq -norows -o doc -db MyDbName -desc "Extracted from MyDbName"
```
The SchemaSpy arguments are documented in the [SchemaSpy](http://schemaspy.sourceforge.net/) web site. The only change needed for Mac/Unix is the `-cp` argument separator changing from `;` to `:`.

### Force JDBC driver arguments

Additional information is passed to the JDBC driver via a single SchemSpy `--connprops` argument. (This awkward mechanism has to be used because SchemaSpy only passes this argument through to the JDBC driver.)

By default all custom objects are output. Here is an example that outputs two standard objects in addition to all the custom objects:
```
... -connprops excludes\=;includes\=Account,Contact
```
On Mac/Unix bash this would need to be:
```
... -connprops excludes\\=\;includes\\=Account,Contact
```
The available `-connprops` arguments are:

Name | Values | Default | Description
---- | ------ | --------| ----------- | 
custom | true or false | "true" | consider custom objects
standard | true or false | "false" | consider standard objects
excludes | comma separated list of object names | "User" | custom or standard object names that are an exact match are excluded (takes priority over includes); if you include "User" the diagram will look like a plate of spaghetti as every object is related to it
includes | comma separated list of object names | Empty | custom or standard object names that are an exact match are included (excludes takes priority over this) irrespective of the custom and standard flag settings\
url | https://test.salesforce.com/services/Soap/u/18.0 (for a sandbox) | taken from the generated web service client jar | the URL (but note that the property name is in lower case) to use to get the metadata via the Partner Web Service API 

### Java properties for proxy servers

The following system properties (that can be set using e.g. Java executable -D arguments) are used if present to configure the corresponding values in the underlying WSC configuration:

* http.auth.ntlm.domain
* http.proxyHost and http.proxyPort (both must be set)
* http.proxyUser
* http.proxyPassword

## Original Documentation

See:

* [first usage page](https://code.google.com/archive/p/force-metadata-jdbc-driver/wikis/Useage.wiki)
* [later usage page](https://code.google.com/archive/p/force-metadata-jdbc-driver/wikis/UsageForV2.wiki)
* [issues](https://code.google.com/archive/p/force-metadata-jdbc-driver/issues)
