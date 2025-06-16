FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the application JAR file
COPY build/libs/*.jar app.jar

# Create directory for key storage
RUN mkdir -p /app/keys

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 