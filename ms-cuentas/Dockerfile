# eclipse-temurin:17-jdk compatible con amd64 y arm64 (Apple Silicon M1/M2)
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# eclipse-temurin:17-jre es multi-arch, no necesita -alpine
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
