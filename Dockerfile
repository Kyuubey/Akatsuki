FROM openjdk:latest
WORKDIR /usr/src
RUN git clone git://github.com/noud02/Akatsuki.git akatsuki && \
    cd akatsuki && \
    ./gradlew build
WORKDIR /usr/src/akatsuki
COPY ./config.yml /usr/src/akatsuki
CMD ["java", "-jar", "./build/libs/Akatsuki.jar"]
