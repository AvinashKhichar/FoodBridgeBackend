# ---- build stage ----
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

# Copy gradle wrapper + metadata
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts ./
COPY settings.gradle.kts ./

RUN chmod +x gradlew

# Copy all source
COPY . .

# Build jar (skip tests for faster build)
RUN ./gradlew clean bootJar -x test --no-daemon


# ---- runtime stage ----
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy only the final jar from build stage
COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
