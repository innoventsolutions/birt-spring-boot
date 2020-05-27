# birt-spring-boot-starter
Project to make it easy to add Eclipse Business Intelligence and Reporting Tools (BIRT) functionality to Spring Boot apps.
The core project consists of three projects
 - birt-engine {core BIRT functions and REST interfaces to use those functions}
 - birt-engine-autoconfigure {configuration to make birt-engine just work through inclusion in gradle or pom}
 - birt-spring-boot-starter {the starter for the project}
 
We have also provide a few sample projects that show how to use the birt-spring-boot-starter.
 - birt-engine-sample-app shows a simple include in the pom.xml which allows users to start using birt
 
BIRT needs to be able to access resources to run reports. Things like report designs, property files, libraries, and css can be used by BIRT. BIRT will also produce output documents (e.g. .rptdocuments, .pdf, .doc, .xslx). To keep things simple the birt-starter uses simple file system access with a pre-defined folder structure. The location and structure of this file system can be seen in the birt-engine-workspace project. The directory locations can be adjusted through application.properties files as needed.

