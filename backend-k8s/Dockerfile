FROM eclipse-temurin:17-jdk AS stage1

WORKDIR /app

COPY gradle gradle
COPY src src
COPY build.gradle .
COPY gradlew .
COPY settings.gradle .

RUN chmod +x gradlew
RUN ./gradlew bootJar

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=stage1 /app/build/libs/*.jar moin.jar
ENTRYPOINT ["java", "-jar", "moin.jar"]
