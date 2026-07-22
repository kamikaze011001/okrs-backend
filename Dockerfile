FROM maven:3.9.6-eclipse-temurin-17 AS builder
ARG app_path=/app
WORKDIR $app_path

COPY pom.xml .
COPY core/pom.xml core/
COPY core/core-api-exception/pom.xml core/core-api-exception/
COPY core/core-audit/pom.xml core/core-audit/
COPY core/core-authentication/pom.xml core/core-authentication/
COPY core/core-email/pom.xml core/core-email/
COPY core/core-exception/pom.xml core/core-exception/
COPY core/core-redis/pom.xml core/core-redis/
COPY core/core-swagger/pom.xml core/core-swagger/
COPY core/core-upload/pom.xml core/core-upload/
COPY core/core-util/pom.xml core/core-util/

COPY okrs-core/pom.xml okrs-core/
COPY okrs-api/pom.xml okrs-api/

RUN mvn dependency:go-offline dependency:resolve-plugins -B

COPY core/ core/
COPY okrs-core/ okrs-core/
COPY okrs-api/ okrs-api/

RUN mvn clean package -pl okrs-api -am -DskipTests -B

FROM eclipse-temurin:11-jre-alpine
ARG app_path=/app
WORKDIR $app_path

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder $app_path/okrs-api/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]