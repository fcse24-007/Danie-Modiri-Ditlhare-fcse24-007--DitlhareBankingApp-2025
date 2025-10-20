CREATE DATABASE IF NOT EXISTS bankdb;
USE bankdb;

-- Users table with proper password hash size
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(50) PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('BANK_EMPLOYEE', 'CUSTOMER', 'ADMINISTRATOR') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customers table
CREATE TABLE customers (
    customer_id VARCHAR(50) PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    address TEXT,
    phone_number VARCHAR(20),
    email VARCHAR(100),
    customer_type ENUM('INDIVIDUAL', 'JOINT', 'BUSINESS') NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Accounts table
CREATE TABLE accounts (
    account_number VARCHAR(50) PRIMARY KEY,
    balance DECIMAL(15,2) DEFAULT 0.00,
    date_created DATE NOT NULL,
    date_opened DATE NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'CLOSED', 'SUSPENDED') NOT NULL,
    customer_id VARCHAR(50) NOT NULL,
    account_type ENUM('SAVINGS', 'CHEQUE', 'INVESTMENT') NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- Transactions table
CREATE TABLE transactions (
    transaction_id VARCHAR(50) PRIMARY KEY,
    type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_INTERNAL', 'TRANSFER_EXTERNAL', 'INTEREST_PAYMENT') NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    time_stamp TIMESTAMP NOT NULL,
    description TEXT,
    account_number VARCHAR(50) NOT NULL,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE CASCADE
);

-- Audit log table
CREATE TABLE audit_log (
    audit_id VARCHAR(50) PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    time_stamp TIMESTAMP NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    details TEXT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);


SHOW TABLES;
