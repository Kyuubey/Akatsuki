# Akatsuki [![Discord](https://img.shields.io/discord/246729077128429568.svg?style=flat-square)](https://discord.gg/qngdWCZ) [![Travis](https://img.shields.io/travis/noud02/Akatsuki.svg?style=flat-square)](https://travis-ci.org/noud02/Akatsuki)

A multipurpose Discord bot written in Kotlin.

## Compiling and running the bot

```bash
# Clone and build
$ git clone https://github.com/noud02/Akatsuki && cd Akatsuki
$ ./gradlew clean build

# Config
$ cp config.example.yml config.yml
# use your favorite editor to edit the YAML file

# Run it
$ java -jar builds/libs/Akatsuki.jar
```

### Docker

Same steps as above except instead of building it with gradle and running it with java run the following command

```bash
# Build and run it
$ sudo docker-compose up -d

# View all containers
$ sudo docker-compose ps
# CONTAINER ID        IMAGE               COMMAND                  CREATED              STATUS              PORTS               NAMES
# abcdefg12345        postgres:alpine     "docker-entrypoint.s…"   ...                  ...                 5432/tcp            akatsuki_db_1
# hijklmn67890        akatsuki_akatsuki   "java -jar ./build/l…"   ...                  ...                                     akatsuki_akatsuki_1

```

### Setting up the image API

```bash
# Clone the repo
$ git clone https://github.com/noud02/Akatsuki-Backend && cd Akatsuki-Backend
$ ./main.py
# Server listens on port 5050
```