FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/my-app.jar ecommerce-app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "ecommerce-app.jar"]
