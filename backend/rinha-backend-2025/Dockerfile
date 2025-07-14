# Estágio de build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio final da imagem
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/rinha-backend-2025-0.0.1-SNAPSHOT.jar .
EXPOSE 9999
CMD ["java", "-jar", "rinha-backend-2025-0.0.1-SNAPSHOT.jar"]