FROM maven:3.8-openjdk-17 as build

COPY . .

RUN mvn clean package

FROM openjdk:17-jdk-slim as runtime

EXPOSE 8080

COPY --from=build /target/todolist-1.0.0.jar app.jar

ENTRYPOINT [ "java", "-jar", "app.jar" ]