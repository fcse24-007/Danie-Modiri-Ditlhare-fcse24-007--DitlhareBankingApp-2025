package com.bac;

import controller.TransactionController;
import controller.TransactionController.DepositResult;
import database.AccountDAO;
import database.TransactionDAO;
import database.AuditDAO;
import model.*;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.time.LocalDate;

/**
 * Test class for TransactionController
 * Tests deposit, withdrawal, and transfer operations with proper validation
 */
public class TransactionControllerTest {
    
    private TransactionController transactionController;
    private Customer customer1;
    private Customer customer2;
    private LocalDate today;
    
    @Before
    public void setUp() {
        transactionController = new TransactionController();
        today = LocalDate.now();
        
        // Create test customers
        customer1 = new Customer(
            "USER001", "customer1", "TestPass@123",
            "CUST001", "John", "Doe", "123 Main St",
            "555-1234", "john@example.com", CustomerType.INDIVIDUAL
        );
        
        customer2 = new Customer(
            "USER002", "customer2", "TestPass@123",
            "CUST002", "Jane", "Smith", "456 Elm St",
            "555-5678", "jane@example.com", CustomerType.INDIVIDUAL
        );
    }
    
    // ============== DEPOSIT TESTS ==============
    
    @Test
    public void testProcessDepositWithValidAmount() {
        SavingsAccount account = new SavingsAccount(
            "SAV001", 500.0, today, today, customer1, AccountStatus.ACTIVE
        );
        
        transactionController.processDeposit(account, 150.0);
        
        assertEquals(650.0, account.getBalance(), 0.01);
    }
    
    @Test
    public void testProcessDepositBlocksNegativeAmount() {
        SavingsAccount account = new SavingsAccount(
            "SAV002", 500.0, today, today, customer1, AccountStatus.ACTIVE
        );
        
        try {
            transactionController.processDeposit(account, -100.0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("positive"));
        }
    }
    
    @Test
    public void testProcessDepositBlocksNullAccount() {
        try {
            transactionController.processDeposit(null, 100.0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("null"));
        }
    }
    
    // ============== WITHDRAWAL TESTS ==============
    
    @Test
    public void testProcessWithdrawalFromChequeAccount() {
        ChequeAccount cheque = new ChequeAccount(
            "CHQ001", 800.0, today, today, customer1, AccountStatus.ACTIVE,
            "Employer", "Address", true
        );
        
        transactionController.processWithdrawal(cheque, 200.0);
        
        assertEquals(600.0, cheque.getBalance(), 0.01);
    }
    
    @Test
    public void testProcessWithdrawalBlocksFromSavingsAccount() {
        SavingsAccount savings = new SavingsAccount(
            "SAV003", 500.0, today, today, customer1, AccountStatus.ACTIVE
        );
        
        try {
            transactionController.processWithdrawal(savings, 100.0);
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("not permitted"));
        }
    }
    
    @Test
    public void testProcessWithdrawalBlocksInsufficientFunds() {
        ChequeAccount cheque = new ChequeAccount(
            "CHQ002", 100.0, today, today, customer1, AccountStatus.ACTIVE,
            "Employer", "Address", true
        );
        
        try {
            transactionController.processWithdrawal(cheque, 500.0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Insufficient"));
        }
    }
    
    @Test
    public void testProcessWithdrawalBlocksNullAccount() {
        try {
            transactionController.processWithdrawal(null, 100.0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("null"));
        }
    }
    
    // ============== TRANSFER TESTS ==============
    
    @Test
    public void testTransferBetweenAccounts() {
        Account fromAccount = new ChequeAccount(
            "CHQ003", 1000.0, today, today, customer1, AccountStatus.ACTIVE,
            "Employer", "Address", true
        );
        
        Account toAccount = new SavingsAccount(
            "SAV004", 500.0, today, today, customer2, AccountStatus.ACTIVE
        );
        
        transactionController.transferFunds(fromAccount, toAccount, 300.0);
        
        assertEquals(700.0, fromAccount.getBalance(), 0.01);
        assertEquals(800.0, toAccount.getBalance(), 0.01);
    }
    
    @Test
    public void testTransferFromSavingsAccountWithinMinimumBalance() {
        SavingsAccount savings = new SavingsAccount(
            "SAV005", 1000.0, today, today, customer1, AccountStatus.ACTIVE
        );
        
        Account target = new ChequeAccount(
            "CHQ004", 0.0, today, today, customer2, AccountStatus.ACTIVE,
            "Employer", "Address", true
        );
        
        transactionController.transferFunds(savings, target, 400.0);
        
        assertEquals(600.0, savings.getBalance(), 0.01);
        assertEquals(400.0, target.getBalance(), 0.01);
    }
    
    @Test
    public void testTransferBlocksExceedingSavingsMinimum() {
        SavingsAccount savings = new SavingsAccount(
            "SAV006", 700.0, today, today, customer1, AccountStatus.ACTIVE
        );
        
        Account target = new ChequeAccount(
            "CHQ005", 0.0, today, today, customer2, AccountStatus.ACTIVE,
            "Employer", "Address", true
        );
        
        try {
            // Try to transfer more than available (700 - 500 minimum = 200 available)
            transactionController.transferFunds(savings, target, 300.0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Insufficient"));
        }
    }
    
    @Test
    public void testTransferBlocksToSameAccount() {
        Account account = new ChequeAccount(
            "CHQ006", 1000.0, today, today, customer1, AccountStatus.ACTIVE,
            "Employer", "Address", true
        );
        
        try {
            transactionController.transferFunds(account, account, 100.0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("same account"));
        }
    }
    
    @Test
    public void testTransferBlocksNullFromAccount() {
        Account toAccount = new ChequeAccount(
            "CHQ007", 500.0, today, today, customer2, AccountStatus.ACTIVE,
            "Employer", "Address", true
        );
        
        try {
            transactionController.transferFunds(null, toAccount, 100.0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("specified"));
        }
    }
    
    @Test
    public void testTransferBlocksNullToAccount() {
        Account fromAccount = new ChequeAccount(
            "CHQ008", 1000.0, today, today, customer1, AccountStatus.ACTIVE,
            "Employer", "Address", true
        );
        
        try {
            transactionController.transferFunds(fromAccount, null, 100.0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("specified"));
        }
    }
    
    @Test
    public void testTransferBlocksNegativeAmount() {
        Account fromAccount = new ChequeAccount(
            "CHQ009", 1000.0, today, today, customer1, AccountStatus.ACTIVE,
            "Employer", "Address", true
        );
        
        Account toAccount = new ChequeAccount(
            "CHQ010", 500.0, today, today, customer2, AccountStatus.ACTIVE,
            "Employer", "Address", true
        );
        
        try {
            transactionController.transferFunds(fromAccount, toAccount, -100.0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("positive"));
        }
    }
    
    @Test
    public void testTransferBlocksZeroAmount() {
        Account fromAccount = new ChequeAccount(
            "CHQ011", 1000.0, today, today, customer1, AccountStatus.ACTIVE,
            "Employer", "Address", true
        );
        
        Account toAccount = new ChequeAccount(
            "CHQ012", 500.0, today, today, customer2, AccountStatus.ACTIVE,
            "Employer", "Address", true
        );
        
        try {
            transactionController.transferFunds(fromAccount, toAccount, 0.0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("positive"));
        }
    }
    
    @Test
    public void testMultipleTransfersAreTracked() {
        ChequeAccount account1 = new ChequeAccount(
            "CHQ013", 2000.0, today, today, customer1, AccountStatus.ACTIVE,
            "Employer", "Address", true
        );
        
        SavingsAccount account2 = new SavingsAccount(
            "SAV007", 500.0, today, today, customer2, AccountStatus.ACTIVE
        );
        
        ChequeAccount account3 = new ChequeAccount(
            "CHQ014", 0.0, today, today, customer2, AccountStatus.ACTIVE,
            "Employer", "Address", true
        );
        
        // First transfer
        transactionController.transferFunds(account1, account2, 200.0);
        assertEquals(1800.0, account1.getBalance(), 0.01);
        assertEquals(700.0, account2.getBalance(), 0.01);
        
        // Second transfer from same source
        transactionController.transferFunds(account1, account3, 500.0);
        assertEquals(1300.0, account1.getBalance(), 0.01);
        assertEquals(500.0, account3.getBalance(), 0.01);
    }
}
