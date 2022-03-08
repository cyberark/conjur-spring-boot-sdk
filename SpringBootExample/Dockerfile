ARG java_version
ARG maven_version

FROM maven:${maven_version}-openjdk-${java_version}-slim

RUN mkdir /app

COPY target/*.jar /app

ENTRYPOINT ["/bin/bash", "-c", "java -jar /app/*.jar"]
