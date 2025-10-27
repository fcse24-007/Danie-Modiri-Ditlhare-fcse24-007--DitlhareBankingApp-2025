// ControllerTest.java
package com.bac;

import controller.AuthenticationController;
import controller.BankEmployeeController;
import controller.TransactionController;
import database.DatabaseConnection;
import model.*;

public class ControllerTest {
    public static void main(String[] args) {
        try {
            System.out.println("=== CONTROLLER LAYER TEST ===\n");
            
            // Initialize database
            DatabaseConnection.getConnection();
            
            // Test Authentication Controller (Matches Login Sequence Diagram)
            testAuthenticationController();
            
            // Test Transaction Controller (Matches Deposit Sequence Diagram)  
            testTransactionController();
            
            // Test Bank Employee Controller
            testBankEmployeeController();
            
            System.out.println("\n=== ALL CONTROLLER TESTS COMPLETED SUCCESSFULLY ===");

        } catch (Exception e) {
            System.err.println("Controller test failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConnection.closeConnection();
        }
    }

    private static void testAuthenticationController() {
        System.out.println("--- Testing Authentication Controller (Login Sequence) ---");
        
        AuthenticationController authController = new AuthenticationController();
        
        // Test successful login (admin user from database initialization)
        var loginResult = authController.login("admin", "admin123");
        System.out.println("Login Test Result:");
        System.out.println("  Success: " + loginResult.isSuccess());
        System.out.println("  Role: " + loginResult.getRole());
        System.out.println("  Message: " + loginResult.getMessage());
        
        if (loginResult.isSuccess()) {
            // Test logout
            authController.logout();
            System.out.println("  Logout: SUCCESS");
        }
        
        // Test failed login
        var failedLogin = authController.login("nonexistent", "wrongpassword");
        System.out.println("Failed Login Test:");
        System.out.println("  Success: " + failedLogin.isSuccess());
        System.out.println("  Message: " + failedLogin.getMessage());
    }

    private static void testTransactionController() {
        System.out.println("\n--- Testing Transaction Controller (Deposit Sequence) ---");
        
        TransactionController transactionController = new TransactionController();
        
        // Test successful deposit
        var depositResult = transactionController.processDeposit("ACC002", 500.0, "admin001");
        System.out.println("Deposit Test Result:");
        System.out.println("  Success: " + depositResult.isSuccess());
        System.out.println("  New Balance: " + depositResult.getNewBalance());
        System.out.println("  Message: " + depositResult.getMessage());
        System.out.println("  Transaction ID: " + depositResult.getTransactionId());
        
        // Test failed deposit (invalid account)
        var failedDeposit = transactionController.processDeposit("INVALID_ACC", 500.0, "admin001");
        System.out.println("Failed Deposit Test:");
        System.out.println("  Success: " + failedDeposit.isSuccess());
        System.out.println("  Message: " + failedDeposit.getMessage());
    }

    private static void testBankEmployeeController() {
        System.out.println("\n--- Testing Bank Employee Controller ---");
        
        BankEmployeeController employeeController = new BankEmployeeController();
        
        // Create a new customer
        Customer newCustomer = employeeController.createCustomer(
            "user003", "bob_smith", "password789",
            "cust003", "Bob", "Smith", "789 Pine St",
            "555-0789", "bob@email.com", CustomerType.INDIVIDUAL, "admin001"
        );
        
        System.out.println("Customer Creation Test:");
        System.out.println("  Customer ID: " + newCustomer.getCustomerId());
        System.out.println("  Name: " + newCustomer.getFirstName() + " " + newCustomer.getSurname());
        
        // Open a savings account for the customer
        Account newAccount = employeeController.openAccount(
            newCustomer, AccountType.SAVINGS, 1000.0, "admin001"
        );
        
        System.out.println("Account Opening Test:");
        System.out.println("  Account Number: " + newAccount.getAccountNumber());
        System.out.println("  Account Type: " + newAccount.getClass().getSimpleName());
        System.out.println("  Initial Balance: " + newAccount.getBalance());
        
        // Test deposit for the new account
        var depositResult = employeeController.processDepositForCustomer(
            newAccount.getAccountNumber(), 300.0, "admin001"
        );
        
        System.out.println("Employee Deposit Test:");
        System.out.println("  Success: " + depositResult.isSuccess());
        System.out.println("  New Balance: " + depositResult.getNewBalance());
    }
}