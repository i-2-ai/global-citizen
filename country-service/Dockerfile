FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the application JAR file
COPY build/libs/*.jar app.jar

# Create directory for passport storage
RUN mkdir -p /app/passports

# Expose the application port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 