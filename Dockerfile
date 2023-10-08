FROM openjdk:17-jdk-slim
EXPOSE 8080
WORKDIR /src
ADD build/libs/netology-cloud-storage-0.0.1-SNAPSHOT.jar cloudstorageapp.jar
ENTRYPOINT ["java","-jar","/src/cloudstorageapp.jar"]