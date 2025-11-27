# ---- build stage ----
FROM eclipse-temurin:17-jdk
WORKDIR /workspace

# copy gradle wrapper and gradle files first for caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts ./
# if using build.gradle (groovy) rename accordingly
# COPY build.gradle ./

# make wrapper executable
RUN chmod +x ./gradlew

# copy source
COPY . .

# Build the jar (skip tests for faster builds unless you want them)
RUN ./gradlew clean bootJar -x test --no-daemon

# ---- runtime stage ----
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy the fat jar produced by Spring Boot
# Adjust path if your jar has a different name or build tool creates in different path
COPY --from=build /workspace/build/libs/*.jar app.jar

# Expose (optional) — Render will set $PORT
EXPOSE 8080

# Use PORT env var Render provides. Using sh -c so $PORT is expanded at runtime.
ENTRYPOINT ["sh","-c","java -jar /app/app.jar --server.port=${PORT:-8080}"]
