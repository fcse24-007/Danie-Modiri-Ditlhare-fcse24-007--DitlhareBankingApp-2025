# Danie-Modiri-Ditlhare-fcse24-007--DitlhareBankingApp-2025
This repository contains Java source files for the 2025 Java Assignment — a simple banking system implementation.

## Java runtime

This project is compiled and tested with Java 21 (LTS). Developers and CI must use JDK 21 to build and run the project.

Quick setup (Linux dev container):

```bash
# install Temurin 21 (Adoptium) repository and JDK
sudo curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo gpg --dearmor -o /usr/share/keyrings/adoptium.gpg
CODENAME=$(grep VERSION_CODENAME /etc/os-release | cut -d= -f2 || echo "bookworm")
echo "deb [signed-by=/usr/share/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $CODENAME main" | sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt-get update -y
sudo apt-get install -y temurin-21-jdk

# if you use SDKMAN, point its 'current' symlink (example)
sudo ln -sfn /usr/lib/jvm/temurin-21-jdk-amd64 /usr/local/sdkman/candidates/java/current

# build
mvn -DskipTests package
```

If you need me to add a Maven toolchains.xml or CI workflow updates to pin JDK 21 for automated builds, I can add them.
