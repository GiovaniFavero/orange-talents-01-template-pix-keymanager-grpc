FROM adoptopenjdk/openjdk11:alpine
ARG JAR_FILE=/key-manager-grpc/orange-talents-01-template-pix-keymanager-grpc/build/libs/*all.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]