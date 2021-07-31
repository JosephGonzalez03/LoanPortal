FROM adoptopenjdk:11.0.8_10-jre-openj9-0.21.0
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]