FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the application JAR file
COPY build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8084

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 