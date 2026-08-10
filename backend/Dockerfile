FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port 8080 available to the world outside this container
EXPOSE 8080

# The application's jar file
ARG JAR_FILE=target/japanese-learning-api-0.0.1-SNAPSHOT.jar

# Add the application's jar to the container
COPY ${JAR_FILE} app.jar

# Run the jar file
# We use ENTRYPOINT to ensure the process runs as PID 1
# SPRING_PROFILES_ACTIVE is expected to be passed as an environment variable (e.g. prod or dev)
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
