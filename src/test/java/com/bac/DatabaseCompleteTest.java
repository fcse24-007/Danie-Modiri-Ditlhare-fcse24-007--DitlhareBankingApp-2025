// DatabaseCompleteTest.java
package com.bac;

import database.*;
import model.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DatabaseCompleteTest {
    public static void main(String[] args) {
        try {
            System.out.println("=== STARTING DATABASE TEST ===\n");
            
            // Just call getConnection() - it will handle initialization
            DatabaseConnection.getConnection();
            
            // Test in separate methods to avoid connection conflicts
            testBasicOperations();
            
            System.out.println("\n=== ALL DATABASE TESTS COMPLETED SUCCESSFULLY ===");

        } catch (Exception e) {
            System.err.println("Database test failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close connection at the end
            DatabaseConnection.closeConnection();
        }
    }

    private static void testBasicOperations() {
        System.out.println("--- Testing Basic Database Operations ---");
        
        // Test 1: User Operations
        testUserOperations();
        
        // Test 2: Customer Operations  
        testCustomerOperations();
        
        // Test 3: Account Operations
        testAccountOperations();
        
        // Test 4: Transaction Operations
        testTransactionOperations();
        
        // Test 5: Audit Operations
        testAuditOperations();
    }

    private static void testUserOperations() {
        System.out.println("\n1. Testing User Operations:");
        UserDAO userDAO = new UserDAO();
        
        // Create test user
        User testUser = new User("test001", "testuser", "password123", UserRole.CUSTOMER);
        
        if (userDAO.save(testUser)) {
            System.out.println("✓ User saved successfully");
        }
        
        // Find user
        var foundUser = userDAO.findByUsername("testuser");
        if (foundUser.isPresent()) {
            System.out.println("✓ User found: " + foundUser.get().getUsername());
        }
        
        // Test authentication
        boolean authResult = userDAO.authenticate("testuser", "password123");
        System.out.println("✓ Authentication test: " + (authResult ? "PASSED" : "FAILED"));
    }

    private static void testCustomerOperations() {
        System.out.println("\n2. Testing Customer Operations:");
        CustomerDAO customerDAO = new CustomerDAO();
        
        Customer testCustomer = new Customer(
            "user002", "jane_doe", "password456", 
            "cust002", "Jane", "Doe", "456 Oak St", 
            "555-0456", "jane@email.com", CustomerType.INDIVIDUAL
        );
        
        if (customerDAO.save(testCustomer)) {
            System.out.println("✓ Customer saved successfully");
        }
        
        var foundCustomer = customerDAO.findById("cust002");
        if (foundCustomer.isPresent()) {
            System.out.println("✓ Customer found: " + foundCustomer.get().getFirstName());
        }
    }

    private static void testAccountOperations() {
        System.out.println("\n3. Testing Account Operations:");
        AccountDAO accountDAO = new AccountDAO();
        CustomerDAO customerDAO = new CustomerDAO();
        
        // Get existing customer
        var customer = customerDAO.findById("cust002");
        if (customer.isPresent()) {
            SavingsAccount savingsAccount = new SavingsAccount(
                "ACC002", 1500.0, LocalDate.now(), LocalDate.now(), 
                customer.get(), AccountStatus.ACTIVE
            );
            
            if (accountDAO.save(savingsAccount)) {
                System.out.println("✓ Account saved successfully");
            }
            
            var foundAccounts = accountDAO.findByCustomerId("cust002");
            System.out.println("✓ Found " + foundAccounts.size() + " accounts for customer");
        }
    }

    private static void testTransactionOperations() {
        System.out.println("\n4. Testing Transaction Operations:");
        TransactionDAO transactionDAO = new TransactionDAO();
        
        // Create a minimal account for transaction
        Account minimalAccount = new Account(
            "ACC002", 0.0, LocalDate.now(), LocalDate.now(), null, AccountStatus.ACTIVE
        );
        
        Transaction transaction = new Transaction(
            "TXN002", 
            TransactionType.DEPOSIT, 
            200.0, 
            LocalDateTime.now(),
            "Test deposit", 
            minimalAccount
        );
        
        if (transactionDAO.save(transaction)) {
            System.out.println("✓ Transaction saved successfully");
        }
        
        var foundTransactions = transactionDAO.findByAccountNumber("ACC002");
        System.out.println("✓ Found " + foundTransactions.size() + " transactions");
    }

    private static void testAuditOperations() {
        System.out.println("\n5. Testing Audit Operations:");
        AuditDAO auditDAO = new AuditDAO();
        
        auditDAO.recordAudit("test001", "LOGIN", "Test user logged in");
        System.out.println("✓ Audit entry recorded");
        
        var foundAudits = auditDAO.findByUserId("test001");
        System.out.println("✓ Found " + foundAudits.size() + " audit entries");
    }
}
