# ─── Stage 1: Build ───────────────────────────────────────────────────────
# eclipse-temurin:17-jdk es compatible con amd64 Y arm64 (Apple Silicon M1/M2)
# Se evita la variante -alpine porque no tiene soporte multi-arch en todas
# las versiones. La imagen base eclipse-temurin tiene soporte oficial arm64.
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Instalar Maven dentro del contenedor
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

COPY pom.xml .
# Descarga dependencias en capa separada (cache de Docker)
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────
# eclipse-temurin:17-jre también es multi-arch (amd64 + arm64)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
