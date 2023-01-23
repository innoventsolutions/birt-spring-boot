#!/bin/sh
java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000 -jar "./opt/birtApp/birt-simple-0.0.7-SNAPSHOT.jar"