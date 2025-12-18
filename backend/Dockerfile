# 建置階段
FROM gradle:8.14.3-jdk17-alpine AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true
COPY src ./src
RUN gradle build -x test --no-daemon

# 執行階段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 只複製可執行的 fat jar（不是 plain.jar）
COPY --from=build /app/build/libs/backend-0.0.1-SNAPSHOT.jar app.jar

ENV TZ=Asia/Taipei
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
