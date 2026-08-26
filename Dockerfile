# syntax=docker/dockerfile:1

# Stage 1: build the Spring Boot jar with the project Gradle wrapper (Java 21).
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY settings.gradle build.gradle ./
RUN chmod +x gradlew

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: run the built jar on a lightweight JRE.
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring \
	&& mkdir -p /var/lib/library/uploads \
	&& chown -R spring:spring /var/lib/library

COPY --from=build /app/build/libs/*.jar app.jar
RUN chown spring:spring app.jar

USER spring
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV FILE_STORAGE_DIRECTORY=/var/lib/library/uploads
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
