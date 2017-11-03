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