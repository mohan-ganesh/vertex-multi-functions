
FROM maven:3.9.9-amazoncorretto-21 as builder

# Copy local code to the container image.
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build a release artifact.
RUN mvn clean package -DskipTests


FROM amazoncorretto:21.0.5-al2-generic


# Copy the jar to the production image from the builder stage.
COPY --from=builder /app/target/appointment-service*.jar /appointment-service.jar

# Run the web service on container startup.
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=dev", "-jar", "/appointment-service.jar"]