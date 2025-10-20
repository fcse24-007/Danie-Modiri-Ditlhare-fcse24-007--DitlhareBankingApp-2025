# Danie-Modiri-Ditlhare-fcse24-007--DitlhareBankingApp-2025
This repository contains Java source files for the 2025 Java Assignment — a simple banking system implementation.

## Java runtime

This project is compiled and tested with Java 17 (LTS). Developers and CI must use JDK 17 to build and run the project.

Quick setup (Linux dev container):

```bash
# install OpenJDK 17 (or use Temurin 17 if preferred)
sudo apt-get update -y
sudo apt-get install -y openjdk-17-jdk

# if you installed Temurin 17 via Adoptium, adapt the path below to your install
# sudo ln -sfn /usr/lib/jvm/temurin-17-jdk-amd64 /usr/local/sdkman/candidates/java/current

# build
mvn -DskipTests package
```

If you need me to add a Maven toolchains.xml or CI workflow updates to pin JDK 17 for automated builds, I can add them.
