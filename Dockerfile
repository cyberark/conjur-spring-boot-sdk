FROM maven:${maven_version}-openjdk-${java_version}-slim 

RUN mkdir /app

COPY target/*.jar /app

ENTRYPOINT ['java', '-jar', '/app/*.jar']