FROM eclipse-temurin:25-jre
WORKDIR /app
COPY target/ramkrishna-sales-service-1.0.0.jar app.jar
RUN mkdir -p /app/data/documents /app/logs
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
