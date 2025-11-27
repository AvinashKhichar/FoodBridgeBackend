# Dockerfile (Gradle / Kotlin Spring Boot)
# Multi-stage build: build the fat jar, then run with a minimal runtime image.

# ---- build stage ----
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

# copy Gradle wrapper and build files (cache friendly)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# ensure wrapper is executable
RUN chmod +x ./gradlew

# copy source
COPY . .

# build the jar (skip tests to speed up CI; remove -x test if you want tests)
RUN ./gradlew clean bootJar -x test --no-daemon

# ---- runtime stage ----
FROM eclipse-temurin:17-jdk-jammy AS runtime
WORKDIR /app

# copy the produced fat jar
COPY --from=build /workspace/build/libs/*.jar app.jar

# optional: set a modest heap size (adjust if needed)
ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx512m"

EXPOSE 8080

# Run the jar and bind to the PORT Render provides (default 8080 if not set)
ENTRYPOINT ["sh", "-c", "java $JAVA_TOOL_OPTIONS -jar /app/app.jar --server.port=${PORT:-8080}"]
