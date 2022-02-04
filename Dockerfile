ARG java_version
ARG maven_version

#FROM  openjdk:$java_version
 
FROM maven:${maven_version}-openjdk-${java_version}-slim

#RUN javac -version
#COPY . .
#WORKDIR .
#RUN javac Main.java

RUN mvn -f pom.xml  package

#COPY  *.jar .

CMD ["mvn", "--version"]



#FROM maven:3.8.4-openjdk-11-slim 