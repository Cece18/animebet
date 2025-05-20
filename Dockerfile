# Use a Java 17 base image with Maven pre-installed
FROM maven:3.9.6-eclipse-temurin-17 as builder

# Set working directory
WORKDIR /app

# Copy only pom.xml first to leverage Docker cache
COPY pom.xml .

# Download dependencies (cached if pom.xml hasn't changed)
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Package the application (skip tests)
RUN mvn clean package -DskipTests

# ---------------------------------------------

# Runtime image (slimmer and more secure)
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy built jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# ENV vars are optional in Dockerfile; use Render dashboard instead
# ENTRYPOINT is fine
ENTRYPOINT ["java", "-jar", "app.jar"]
