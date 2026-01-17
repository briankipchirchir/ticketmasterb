# Use Java 17
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy Maven built jar
COPY target/ticket-backend-1.0.0.jar app.jar

# Create uploads directory
RUN mkdir -p /app/uploads

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
