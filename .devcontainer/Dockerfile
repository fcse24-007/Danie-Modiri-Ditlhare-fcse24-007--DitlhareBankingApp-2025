FROM mcr.microsoft.com/devcontainers/java:1-21

# Install additional packages
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends \
    mysql-client \
    && apt-get clean -y && rm -rf /var/lib/apt/lists/*

# Create workspace directory
RUN mkdir -p /workspace && chown -R vscode:vscode /workspace

# Set working directory
WORKDIR /workspace