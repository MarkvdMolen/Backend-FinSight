# ---------- Stage 1: Build de app met Maven ----------
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Stel de werkdirectory in
WORKDIR /app

# Kopieer alle projectbestanden naar de container
COPY . .

# Bouw het project en sla tests over
RUN mvn clean package -DskipTests

# ---------- Stage 2: Maak een slanke runtime image ----------
FROM eclipse-temurin:21-jre

# Stel werkdirectory in
WORKDIR /app

# Kopieer alleen het gegenereerde .jar bestand uit de builder stage
COPY --from=builder /app/target/*.jar app.jar

COPY .env .env

# Stel de poort in waarop de app luistert
EXPOSE 8080

# Start de applicatie
CMD ["java", "-jar", "app.jar"]