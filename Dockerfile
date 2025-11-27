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

# ensure wrapper is executable (harmless if already set)
RUN chmod +x ./gradlew

# Optional: fix CRLF line endings (helpful if contributors use Windows)
# Uncomment the next two lines if you have had CRLF issues before:
# RUN apt-get update && apt-get install -y dos2unix
# RUN dos2unix ./gradlew || true

# copy source
COPY . .

# Build using 'sh' to avoid any exec/permission problems
RUN sh ./gradlew clean bootJar -x test --no-daemon

# ---- runtime stage ----
FROM eclipse-temurin:17-jdk-jammy AS runtime
WORKDIR /app

# copy the produced fat jar
COPY --from=build /workspace/build/libs/*.jar app.jar

# modest heap size (adjust as needed)
ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx512m"

EXPOSE 8080

# Run the jar and bind to the PORT Render provides (default 8080 if not set)
ENTRYPOINT ["sh", "-c", "java $JAVA_TOOL_OPTIONS -jar /app/app.jar --server.port=${PORT:-8080}"]
