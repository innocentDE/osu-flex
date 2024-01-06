FROM maven:3.8.4-openjdk-17 AS builder

WORKDIR /usr/src/app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn package

FROM arm64v8/maven:3.8.8-eclipse-temurin-17

WORKDIR /usr/src/app

COPY --from=builder /usr/src/app/target/osu-flex-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/src/app/app.jar

ENV MYSQL_DB_URL=1234567890
ENV MYSQL_DB_USERNAME=1234567890
ENV MYSQL_DB_PASSWORD=1234567890
ENV OSU_CLIENT_ID=1234567890
ENV OSU_CLIENT_SECRET=1234567890
ENV OSU_API_KEY=1234567890
ENV DISCORD_TOKEN=1234567890

CMD ["java", "-jar", "app.jar"]
