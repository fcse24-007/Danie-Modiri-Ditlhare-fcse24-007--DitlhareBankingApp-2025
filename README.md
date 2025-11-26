# DitlhareBankingApp

A comprehensive banking system application built with Java, demonstrating Object-Oriented Analysis and Development principles. This OOAD assignment implements a full-featured banking platform with user authentication, transaction management, and role-based access control.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Building the Application](#building-the-application)
- [Running the Application](#running-the-application)
- [Usage](#usage)
- [Architecture](#architecture)
- [Testing](#testing)
- [Contributing](#contributing)

## Overview

DitlhareBankingApp is a Java-based banking system developed as part of the 2025 Object Oriented Analysis and Development with Java (OOAD) assignment. The system provides secure account management, transaction processing, and administrative features for customers, employees, and system administrators.

## Features

### Authentication & Authorization
- Secure login system with Argon2 password hashing
- Role-based access control (Customer, Bank Employee, System Administrator)
- User session management

### Customer Features
- Account management (create, view, close accounts)
- Multiple account types (Savings, Cheque, Investment)
- Transaction history and tracking
- Balance inquiries
- Interest-bearing account support

### Bank Employee Features
- Customer management and lookup
- Account administration
- Transaction verification and processing
- Customer update functionality

### System Administrator Features
- User and employee management
- System-wide audit logging
- Administrative dashboard
- System configuration

### Advanced Capabilities
- Multiple account types with specific behaviors
- Transaction processing and validation
- Interest calculation for savings accounts
- Comprehensive audit trail for compliance
- SQLite database persistence

## Technology Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| **Java** | 17 | Programming language |
| **JavaFX** | 17.0.2 | GUI framework |
| **SQLite** | 3.44.1.0 | Database |
| **Argon2** | 2.11 | Password hashing |
| **Maven** | Latest | Build automation |
| **JUnit** | 4.13.2 | Unit testing |

## Project Structure

```
DitlhareBankingApp/
├── src/
│   ├── main/java/
│   │   └── com/bac/
│   │       ├── bankingapp/
│   │       │   └── App.java              # Entry point
│   │       ├── controller/
│   │       │   ├── AuthenticationController.java
│   │       │   ├── BankEmployeeController.java
│   │       │   └── TransactionController.java
│   │       ├── database/
│   │       │   ├── DatabaseConnection.java
│   │       │   ├── DAO.java              # Base DAO interface
│   │       │   ├── UserDAO.java
│   │       │   ├── CustomerDAO.java
│   │       │   ├── AccountDAO.java
│   │       │   ├── TransactionDAO.java
│   │       │   └── AuditDAO.java
│   │       ├── model/
│   │       │   ├── User.java
│   │       │   ├── Customer.java
│   │       │   ├── BankEmployee.java
│   │       │   ├── SystemAdministrator.java
│   │       │   ├── Account.java
│   │       │   ├── SavingsAccount.java
│   │       │   ├── ChequeAccount.java
│   │       │   ├── InvestmentAccount.java
│   │       │   ├── Transaction.java
│   │       │   ├── AuditEntry.java
│   │       │   ├── AccountType.java
│   │       │   ├── TransactionType.java
│   │       │   ├── UserRole.java
│   │       │   └── [Other domain models]
│   │       ├── service/
│   │       │   ├── IDGeneratorService.java
│   │       │   └── InterestService.java
│   │       ├── util/
│   │       │   └── Passwords.java        # Security utilities
│   │       └── view/
│   │           ├── BankingApplication.java # Main JavaFX app
│   │           ├── LoginView.java
│   │           ├── CustomerDashboard.java
│   │           ├── BankEmployeeDashboard.java
│   │           ├── AdminDashboard.java
│   │           └── [Other UI components]
│   └── test/java/
│       └── com/bac/
│           ├── AccountTypesTest.java
│           └── TransactionControllerTest.java
├── data/                                 # Database files
├── pom.xml                              # Maven configuration
├── README.md                            # This file
└── TODO.md                              # Development tasks

```

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK)** 17 or higher
  - Download from [Oracle](https://www.oracle.com/java/technologies/downloads/#java17) or use OpenJDK
  - Verify installation: `java -version`

- **Maven** 3.6+
  - Download from [maven.apache.org](https://maven.apache.org/download.cgi)
  - Verify installation: `mvn -version`

- **Git** (optional, for version control)

## Installation & Setup

1. **Clone the repository** (if using Git):
   ```bash
   git clone https://github.com/yourusername/DitlhareBankingApp.git
   cd DitlhareBankingApp
   ```

2. **Navigate to project directory**:
   ```bash
   cd path/to/DitlhareBankingApp
   ```

3. **Verify Maven setup**:
   ```bash
   mvn clean validate
   ```

## Building the Application

Build the project using Maven:

```bash
mvn clean compile
```

To create a compiled JAR package:

```bash
mvn clean package
```

## Running the Application

### Option 1: Using Maven (Recommended)

Run the JavaFX application directly:

```bash
mvn javafx:run
```

### Option 2: Using Exec Maven Plugin

```bash
mvn exec:java
```

### Option 3: Execute JAR File

After packaging:

```bash
java -jar target/banking-system-1.0-SNAPSHOT.jar
```

## Usage

### Login Credentials

The application uses role-based authentication. Default test credentials (if configured in database):

- **Customer**: Username and password set during customer registration
- **Bank Employee**: Credentials provided by system administrator
- **System Administrator**: Initial admin credentials set during database initialization

### Workflow Examples

#### For Customers:
1. Login with customer credentials
2. View account information and balance
3. Perform transactions (transfers, deposits, withdrawals)
4. Check transaction history

#### For Bank Employees:
1. Login with employee credentials
2. Search and manage customer accounts
3. Update customer information
4. Verify and process transactions

#### For Administrators:
1. Login with admin credentials
2. Manage system users and employees
3. View audit logs
4. Configure system settings

## Architecture

### Design Patterns Used

- **Model-View-Controller (MVC)**: Separation of UI logic from business logic
- **Data Access Object (DAO)**: Database abstraction layer
- **Service Layer**: Business logic encapsulation
- **Strategy Pattern**: Different account types and transaction processing
- **Factory Pattern**: Object creation for accounts and transactions

### Database Design

The application uses SQLite with the following main entities:

- **Users**: Authentication and user profiles
- **Customers**: Customer information and details
- **Accounts**: Account information and balances
- **Transactions**: Transaction history and records
- **AuditLog**: System audit trail

### Layer Architecture

```
┌─────────────────────────────┐
│      View Layer (JavaFX)    │
├─────────────────────────────┤
│    Controller Layer         │
├─────────────────────────────┤
│    Service Layer            │
├─────────────────────────────┤
│    DAO Layer                │
├─────────────────────────────┤
│    Database (SQLite)        │
└─────────────────────────────┘
```

## Testing

Run unit tests using Maven:

```bash
mvn test
```

Run specific test class:

```bash
mvn test -Dtest=AccountTypesTest
```

Run with coverage report:

```bash
mvn test jacoco:report
```

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Create a feature branch: `git checkout -b feature/YourFeatureName`
2. Commit changes: `git commit -m 'Add some feature'`
3. Push to branch: `git push origin feature/YourFeatureName`
4. Submit a pull request

## Development Notes

- **Java Version**: Project requires Java 17+ for compatibility with JavaFX 17.0.2
- **Database**: SQLite database file is stored in the `data/` directory
- **Passwords**: All passwords are hashed using Argon2 for security
- **UI Framework**: JavaFX FXML is used for building the GUI

## Troubleshooting

### Issue: "JavaFX modules not found"
**Solution**: Ensure Java 17 is installed and Maven can access JavaFX dependencies. Run `mvn clean compile` to download dependencies.

### Issue: "Database connection failed"
**Solution**: Verify that the `data/` directory exists and has write permissions. The application will create the SQLite database automatically on first run.

### Issue: "Cannot find main class"
**Solution**: Ensure you've compiled the project with `mvn clean compile` before running.

## License

This project is submitted as part of the FCSE24-007 OOAD assignment for 2025.

## Contact & Support

For questions or issues regarding this project, please contact the fcse24@thuto.bac.ac.bw.
