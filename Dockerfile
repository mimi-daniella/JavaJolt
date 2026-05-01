# Use official Java 21 image
FROM eclipse-temurin:21-jdk

# Set working directory inside container
WORKDIR /app

# Copy Maven wrapper and source code
COPY . .

# Build the app (skip tests for faster builds)
RUN ./mvnw clean install -DskipTests

# Expose port (Render maps automatically)
EXPOSE 8080

# Run the JAR
CMD ["java", "-jar", "target/quiz-app.jar"]
