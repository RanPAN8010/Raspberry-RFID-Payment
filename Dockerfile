# 使用维护更好的 Eclipse Temurin 镜像
FROM maven:3.8.6-eclipse-temurin-8 AS build
WORKDIR /app

# 直接复制所有项目文件并编译
# 这样即便部分依赖下载慢，Maven 也会重试，而不是直接卡在 go-offline
COPY . .
RUN mvn clean package -DskipTests

# 运行阶段
FROM eclipse-temurin:8-jre
WORKDIR /app

# 复制编译好的 target 文件夹和 pom.xml
COPY --from=build /app/target /app/target
COPY --from=build /app/pom.xml /app/pom.xml

# 暴露端口并运行主类
EXPOSE 8080
ENTRYPOINT ["java", "-cp", "target/classes:target/dependency/*", "Raspberry.APP"]