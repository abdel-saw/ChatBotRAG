# ==================== Build Stage ====================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# === Copie du Maven Wrapper en premier (très important) ===
COPY .mvn .mvn
COPY mvnw mvnw
COPY mvnw.cmd mvnw.cmd

# Donne les droits d'exécution
RUN chmod +x mvnw

# Copie le pom.xml et télécharge les dépendances (cache optimisé)
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

# Copie le code source et compile
COPY src ./src
RUN ./mvnw clean package -DskipTests

# ==================== Runtime Stage ====================
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "app.jar"]