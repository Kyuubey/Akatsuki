FROM openjdk:alpine
WORKDIR /usr/src
RUN apk update && \
    apk add git && \
    git clone git://github.com/noud02/Akatsuki.git akatsuki --recursive && \
    cd akatsuki && \
    ./gradlew build
WORKDIR /usr/src/akatsuki
COPY ./config.yml /usr/src/akatsuki
CMD ["java", "-jar", "./build/libs/Akatsuki.jar"]
