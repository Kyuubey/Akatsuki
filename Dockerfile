FROM openjdk:latest
ENV AKATSUKI_TOKEN=""
ENV AKATSUKI_DB_NAME=""
ENV AKATSUKI_DB_USER=""
ENV AKATSUKI_DB_PASS=""
ENV AKATSUKI_GOOGLE_KEY=""
ENV AKATSUKI_OWNER_ID=""
WORKDIR /usr/src
RUN git clone git://github.com/noud02/Akatsuki.git akatsuki && \
    cd akatsuki && \
    ./gradlew build
WORKDIR /usr/src/akatsuki
CMD ["java", "-jar", "./build/libs/Akatsuki.jar"]
