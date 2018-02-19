# Akatsuki Dockerfile
# Copyright 2018 (c) Noud Kerver
FROM openjdk:alpine
MAINTAINER Noud Kerver <me@noud02.me>
MAINTAINER Ayane Satomi <enra@headbow.stream>

ENV DATABSE_NAME ""
ENV DATABASE_USER ""
ENV DATABASE_PASS ""
ENV DATABASE_HOST ""
ENV GOOGLE_API_KEY ""
ENV WEEBSH_API_KEY ""
ENV DISCORDBOTSORG_API_KEY ""
ENV DISCORDBOTS_API_KEY ""
ENV SENTRY_DSN ""
ENV MAL_USER ""
ENV MAL_PASSWORD ""
ENV OSU_API_KEY ""
ENV SITE_HOST ""
ENV SITE_SSL ""
ENV SITE_PORT 1991
ENV BACKEND_HOST ""
ENV BACKEND_SSL ""
ENV BACKEND_PORT 5050


RUN addgroup -g 1000 java \
&& adduser -u 1000 -G java -s /bin/sh -D java;

RUN apk update && \
    apk upgrade && \
    mkdir -p /opt/app && \
    chown -R java:root /opt/app && \
    chmod g+rw /opt && \
    chgrp root /opt && \
    find /home/java -type d -exec chmod g+x {} +

COPY Akatsuki.jar /opt/app/

EXPOSE 5050 1991

USER 1000

WORKDIR /opt/app

CMD ["/bin/sh", "-c", "java", "-jar", "./Akatsuki.jar"]
