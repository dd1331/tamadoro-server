# ======== Build stage ========
FROM gradle:8.9-jdk21-alpine AS build

WORKDIR /workspace

# Copy only Gradle files first to leverage layer caching
COPY build.gradle.kts settings.gradle.kts gradlew /workspace/
COPY gradle /workspace/gradle

# Download dependencies
RUN ./gradlew --version && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# Copy sources
COPY src /workspace/src

# Build application (skip tests for faster container build; run tests in CI separately)
RUN ./gradlew --no-daemon clean bootJar -x test

# ======== Runtime stage ========
FROM eclipse-temurin:21-jre-alpine AS runtime

ENV APP_HOME=/app \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS=""

WORKDIR ${APP_HOME}

# Copy built jar
COPY --from=build /workspace/build/libs/*.jar ${APP_HOME}/app.jar

EXPOSE 8080

# Optional basic healthcheck (adjust path if needed)
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s CMD wget -qO- http://localhost:8080/actuator/health | grep '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE}" ]


