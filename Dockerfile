FROM adoptopenjdk/openjdk8:ubi
RUN mkdir /opt/birtApp
COPY ./birt-simple/target/birt-simple-0.0.7-SNAPSHOT.jar ./opt/birtApp
COPY ./birt-simple/run.sh ./opt/
COPY ./birt-engine-workspace  /birt-engine-workspace
EXPOSE 8989
CMD ["/bin/bash", "./opt/run.sh"]
