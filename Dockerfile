FROM openjdk:alpine
WORKDIR /usr/src/build
COPY . /usr/src/build
RUN ./gradlew build
WORKDIR /usr/src/bot
RUN cp -v /usr/src/build/build/libs/Akatsuki.jar . && \
    # cp -v /usr/src/build/{config,games}.yml . && \
    rm -vrf /usr/src/build
CMD ["java", "-jar", "./Akatsuki.jar"]
