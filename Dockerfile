FROM openjdk:alpine
WORKDIR /usr/src
RUN git clone git://github.com/noud02/Akatsuki.git akatsuki --recursive && \
    cd akatsuki && \
    ./gradlew build
WORKDIR /usr/src/akatsuki
COPY ./config.yml /usr/src/akatsuki
CMD ["java", "-jar", "./build/libs/Akatsuki.jar"]
