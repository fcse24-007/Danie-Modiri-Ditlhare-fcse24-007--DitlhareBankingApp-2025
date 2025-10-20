#!/bin/bash

echo "🚀 Initializing Banking System Environment..."

# Start MySQL service
sudo service mysql start

# Wait for MySQL to be ready
echo "⏳ Waiting for MySQL to start..."
sleep 10

# Initialize database
echo "🗄️ Creating database and tables..."
mysql -u root -e "SOURCE /workspace/.devcontainer/database-init.sql"

# Build the project
echo "🔨 Building Java project..."
mvn clean compile -q

echo "✅ Environment setup complete!"
echo "📊 Database: bankdb (MySQL)"
echo "☕ Java: 21"
echo "🛠️ Maven: Ready"