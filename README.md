# Akatsuki

#### Compiling and running the bot

```bash
# Clone and build
$ git clone https://github.com/noud02/Akatsuki && cd Akatsuki
$ ./gradlew clean build

# Set ENV
$ export AKATSUKI_TOKEN=YOUR_SUPER_SECRET_DISCORD_TOKEN
$ export AKATSUKI_DB_NAME=YOUR_POSTGRESQL_DB_NAME
$ export AKATSUKI_DB_USER=USER_THAT_HAS_ACCESS_TO_THE_DB
$ export AKATSUKI_OWNER_ID=YOUR_DISCORD_ID

# Run it
$ cd build/libs && java -jar akatsuki-VERSION-all.jar
```