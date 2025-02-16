FROM openjdk:21-jdk 
COPY target/servermanager-1.0-SNAPSHOT.jar /servermanager.jar 
ENTRYPOINT ["java", "-jar", "/servermanager.jar"]