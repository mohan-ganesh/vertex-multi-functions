
FROM maven:3.8.3-openjdk-17-slim as builder

# Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build a release artifact.
RUN mvn clean package -DskipTests


FROM openjdk:17-alpine3.14

# Copy the jar to the production image from the builder stage.
COPY --from=builder /app/target/appointment-service*.jar /appointment-service.jar

# Run the web service on container startup.
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=dev", "-jar", "/appointment-service.jar"]