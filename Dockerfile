FROM eclipse-temurin:17-jdk-jammy

RUN apt-get update && apt-get install -y \
    libgomp1 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

RUN mkdir -p /app/models
ENV SPRING_AI_EMBEDDING_TRANSFORMER_CACHE_DIRECTORY=/app/models

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xmx2g", "-jar", "app.jar"]