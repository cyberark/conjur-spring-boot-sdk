ARG java_version
ARG maven_version

#FROM  openjdk:$java_version
#FROM maven:3.8.4-openjdk-11-slim
FROM maven:${maven_version}-openjdk-${java_version}-slim
