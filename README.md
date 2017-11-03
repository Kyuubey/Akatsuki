# Akatsuki [![Discord](https://img.shields.io/discord/246729077128429568.svg?style=flat-square)](https://discord.gg/qngdWCZ) [![Travis](https://img.shields.io/travis/noud02/Akatsuki.svg?style=flat-square)](https://travis-ci.org/noud02/Akatsuki)

A multipurpose Discord bot written in Kotlin.

## Compiling and running the bot

```bash
# Clone and build
$ git clone https://github.com/noud02/Akatsuki && cd Akatsuki
$ ./gradlew clean build

# Config
$ echo 'token: "YOUR_SUPER_SECRET_DISCORD_TOKEN"' >  config.yml
$ echo 'owners: ["YOUR_DISCORD_ID"]'              >> config.yml
$ echo 'prefixes: ["UNIQUE_PREFIX"]'              >> config.yml
$ echo 'games: ["STATUS"]'                        >> config.yml
$ echo 'database:'                                >> config.yml
$ echo '  name: "YOUR_POSTGRESQL_DB_NAME"'        >> config.yml
$ echo '  user: "USER_THAT_HAS_ACCESS_TO_THE_DB"' >> config.yml
$ echo '  pass: ""'                               >> config.yml
$ echo 'api:'                                     >> config.yml
$ echo '  google: "YOUR_GOOGLE_API_KEY"'          >> config.yml

# Run it
$ java -jar builds/libs/Akatsuki.jar
```