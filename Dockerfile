# 第一阶段：构建阶段
FROM maven:3.8.6-eclipse-temurin-8 AS build
WORKDIR /app

# 复制 pom.xml 和源码
COPY pom.xml .
COPY src ./src

# 执行打包，assembly 插件会生成 *-jar-with-dependencies.jar
RUN mvn clean package -DskipTests

# 第二阶段：运行阶段
FROM eclipse-temurin:8-jre
WORKDIR /app

# 创建数据文件夹，确保 SQLite 数据库有地方放
RUN mkdir -p data

# 从构建阶段复制生成的“带依赖”的 JAR 包
# 注意：这里的 jar 文件名必须与你 pom.xml 里的 artifactId 和 version 对应
COPY --from=build /app/target/service-reseau-2026-0.0.1-SNAPSHOT-jar-with-dependencies.jar app.jar

# 暴露 Web 服务器端口
EXPOSE 8080

# 直接运行 Fat JAR
ENTRYPOINT ["java", "-jar", "app.jar"]