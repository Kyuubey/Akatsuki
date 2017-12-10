FROM openjdk:alpine
WORKDIR /usr/src/akatsuki
COPY . /usr/src/akatsuki
RUN ./gradlew build
CMD ["java", "-jar", "./build/libs/Akatsuki.jar"]
