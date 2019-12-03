FROM maven:3.6.2-jdk-8 AS build

WORKDIR /app

#use "cached" pom so that packages are not downloaded every time docker image is built
#very useful in development
COPY pom.xml.cache ./pom.xml
COPY src/main/liberty/config/server.xml src/main/liberty/config/server.xml
RUN mvn liberty:create
RUN mvn package || true
RUN mvn clean

#copy source code and pom.xml
COPY src ./src
COPY pom.xml ./pom.xml

#create, build and package Liberty sever
RUN mvn liberty:create
RUN mvn package
RUN mvn liberty:deploy
RUN mvn liberty:package

#extract Liberty package 
WORKDIR /deploy
RUN mv /app/target/*.zip .
RUN unzip *.zip

#actual image
FROM openjdk:8u212-jre-alpine3.9
#FROM openjdk:8u212-jdk-alpine3.9

ENV KEYSTORE_REQUIRED "false"
EXPOSE 9080

#create user to run as non-root
RUN addgroup -g 1001 -S liberty && adduser -u 1001 -S liberty -G liberty

#copy wlp directory from build image
COPY --from=build --chown=liberty:liberty /deploy/wlp /wlp
USER liberty
CMD ["/wlp/bin/server","run"]
