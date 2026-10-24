FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add a volume pointing to /tmp
VOLUME /tmp

# Install curl for container healthcheck
RUN apk add --no-cache curl

# Make port 8080 available to the world outside this container
EXPOSE 8080

# The application's jar file
ARG JAR_FILE=target/japanese-learning-api-0.0.1-SNAPSHOT.jar

# Add the application's jar to the container
COPY ${JAR_FILE} app.jar

# Container-level healthcheck using Actuator liveness probe
# - interval: check every 30 seconds
# - timeout: fail if no response within 10 seconds
# - start-period: wait 60 seconds for app to start before checking
# - retries: mark unhealthy after 3 consecutive failures
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health/liveness || exit 1

# Run the jar file
# We use ENTRYPOINT to ensure the process runs as PID 1
# SPRING_PROFILES_ACTIVE is expected to be passed as an environment variable (e.g. prod or dev)
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
