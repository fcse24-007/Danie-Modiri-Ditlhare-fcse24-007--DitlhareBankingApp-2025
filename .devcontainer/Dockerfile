# Base image with Linux
FROM mcr.microsoft.com/devcontainers/base:ubuntu

# Install Java, Maven, and Gradle
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk maven gradle && \
    apt-get clean

# Set Java environment variables
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# Default user
USER vscode
