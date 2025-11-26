package com.bac;

import model.*;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Test class for different account types: Savings, Investment, and Cheque accounts
 * Tests account-specific business rules and constraints
 */
public class AccountTypesTest {
    
    private Customer customer;
    private LocalDate today;
    
    @Before
    public void setUp() {
        // Create a test customer
        customer = new Customer(
            "USER001", "testuser", "TestPass@123",
            "CUST001", "John", "Doe", "123 Main St",
            "555-1234", "john@example.com", CustomerType.INDIVIDUAL
        );
        today = LocalDate.now();
    }
    
    // ============== SAVINGS ACCOUNT TESTS ==============
    
    @Test
    public void testSavingsAccountAllowsDeposits() {
        SavingsAccount savings = new SavingsAccount(
            "SAV001", 500.0, today, today, customer, AccountStatus.ACTIVE
        );
        
        double initialBalance = savings.getBalance();
        savings.deposit(100.0);
        
        assertEquals(600.0, savings.getBalance(), 0.01);
    }
    
    @Test
    public void testSavingsAccountBlocksWithdrawals() {
        SavingsAccount savings = new SavingsAccount(
            "SAV002", 500.0, today, today, customer, AccountStatus.ACTIVE
        );
        
        try {
            savings.withdraw(100.0);
            fail("Should throw IllegalStateException for withdrawal");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Withdrawals are not permitted"));
        }
    }
    
    @Test
    public void testSavingsAccountEnforcesMinimumBalance() {
        SavingsAccount savings = new SavingsAccount(
            "SAV003", 600.0, today, today, customer, AccountStatus.ACTIVE
        );
        
        try {
            // Try to transfer more than available (balance - minimum)
            savings.transferTo(new SavingsAccount(
                "SAV004", 0.0, today, today, customer, AccountStatus.ACTIVE
            ), 200.0);
            fail("Should throw IllegalArgumentException for exceeding available balance");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Insufficient funds"));
        }
    }
    
    @Test
    public void testSavingsAccountAllowsTransfers() {
        SavingsAccount savings = new SavingsAccount(
            "SAV005", 1000.0, today, today, customer, AccountStatus.ACTIVE
        );
        Account target = new ChequeAccount(
            "CHQ001", 0.0, today, today, customer, AccountStatus.ACTIVE,
            "Employer Inc", "123 Business Ave", true
        );
        
        savings.transferTo(target, 200.0);
        
        assertEquals(800.0, savings.getBalance(), 0.01);
        assertEquals(200.0, target.getBalance(), 0.01);
    }
    
    @Test
    public void testSavingsAccountCalculatesMonthlyInterest() {
        SavingsAccount savings = new SavingsAccount(
            "SAV006", 1000.0, LocalDate.now().minusDays(35), today, customer, AccountStatus.ACTIVE
        );
        
        double interest = savings.calculateInterest();
        assertTrue("Interest should be positive after 30+ days", interest > 0);
    }
    
    @Test
    public void testSavingsAccountNoInterestBeforeMonth() {
        SavingsAccount savings = new SavingsAccount(
            "SAV007", 1000.0, today, today, customer, AccountStatus.ACTIVE
        );
        
        double interest = savings.calculateInterest();
        assertEquals(0.0, interest, 0.01);
    }
    
    // ============== INVESTMENT ACCOUNT TESTS ==============
    
    @Test
    public void testInvestmentAccountAllowsDeposits() {
        InvestmentAccount investment = new InvestmentAccount(
            "INV001", 500.0, today, today, customer, AccountStatus.ACTIVE
        );
        
        investment.deposit(250.0);
        assertEquals(750.0, investment.getBalance(), 0.01);
    }
    
    @Test
    public void testInvestmentAccountAllowsWithdrawals() {
        InvestmentAccount investment = new InvestmentAccount(
            "INV002", 1000.0, today.minusDays(31), today, customer, AccountStatus.ACTIVE
        );
        
        investment.withdrawAmount(300.0);
        assertEquals(700.0, investment.getBalance(), 0.01);
    }
    
    @Test
    public void testInvestmentAccountEnforcesMinimumBalance() {
        InvestmentAccount investment = new InvestmentAccount(
            "INV003", 600.0, today.minusDays(31), today, customer, AccountStatus.ACTIVE
        );
        
        try {
            investment.withdrawAmount(200.0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("minimum requirement"));
        }
    }
    
    @Test
    public void testInvestmentAccountRequires30DayNotice() {
        InvestmentAccount investment = new InvestmentAccount(
            "INV004", 1000.0, today, today, customer, AccountStatus.ACTIVE
        );
        
        try {
            investment.withdrawAmount(100.0);
            fail("Should throw IllegalArgumentException for 30-day requirement");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("30-day notice"));
        }
    }
    
    @Test
    public void testInvestmentAccountValidatesInitialDeposit() {
        InvestmentAccount investment = new InvestmentAccount(
            "INV005", 100.0, today, today, customer, AccountStatus.ACTIVE
        );
        
        assertFalse("Deposit of 100 should not meet minimum", 
            investment.validateInitialDeposit(100.0));
        assertTrue("Deposit of 500 should meet minimum", 
            investment.validateInitialDeposit(500.0));
    }
    
    @Test
    public void testInvestmentAccountCalculatesQuarterlyInterest() {
        LocalDate accountOpened = today.minusDays(95);
        InvestmentAccount investment = new InvestmentAccount(
            "INV006", 1000.0, accountOpened, accountOpened, customer, AccountStatus.ACTIVE
        );
        
        double interest = investment.calculateInterest();
        assertTrue("Interest should be positive after 90+ days", interest > 0);
    }
    
    // ============== CHEQUE ACCOUNT TESTS ==============
    
    @Test
    public void testChequeAccountAllowsDeposits() {
        ChequeAccount cheque = new ChequeAccount(
            "CHQ002", 500.0, today, today, customer, AccountStatus.ACTIVE,
            "TechCorp", "456 Tech Drive", true
        );
        
        cheque.deposit(150.0);
        assertEquals(650.0, cheque.getBalance(), 0.01);
    }
    
    @Test
    public void testChequeAccountAllowsWithdrawals() {
        ChequeAccount cheque = new ChequeAccount(
            "CHQ003", 800.0, today, today, customer, AccountStatus.ACTIVE,
            "TechCorp", "456 Tech Drive", true
        );
        
        cheque.withdraw(200.0);
        assertEquals(600.0, cheque.getBalance(), 0.01);
    }
    
    @Test
    public void testChequeAccountStoresEmploymentDetails() {
        ChequeAccount cheque = new ChequeAccount(
            "CHQ004", 500.0, today, today, customer, AccountStatus.ACTIVE,
            "EmployerInc", "789 Work St", true
        );
        
        assertEquals("EmployerInc", cheque.getEmployerName());
        assertEquals("789 Work St", cheque.getEmployerAddress());
        assertTrue(cheque.isEmploymentStatus());
    }
    
    @Test
    public void testChequeAccountCanUpdateEmploymentDetails() {
        ChequeAccount cheque = new ChequeAccount(
            "CHQ005", 500.0, today, today, customer, AccountStatus.ACTIVE,
            "OldCorp", "Old Address", true
        );
        
        cheque.setEmployerName("NewCorp");
        cheque.setEmployerAddress("New Address");
        cheque.setEmploymentStatus(false);
        
        assertEquals("NewCorp", cheque.getEmployerName());
        assertEquals("New Address", cheque.getEmployerAddress());
        assertFalse(cheque.isEmploymentStatus());
    }
    
    // ============== GENERAL ACCOUNT TESTS ==============
    
    @Test
    public void testAccountBlocksNegativeDeposit() {
        Account account = new SavingsAccount(
            "ACC001", 500.0, today, today, customer, AccountStatus.ACTIVE
        );
        
        try {
            account.deposit(-100.0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("positive"));
        }
    }
    
    @Test
    public void testAccountBlocksInsufficientWithdrawal() {
        Account account = new ChequeAccount(
            "CHQ006", 100.0, today, today, customer, AccountStatus.ACTIVE,
            "Corp", "Address", true
        );
        
        try {
            account.withdraw(500.0);
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Insufficient funds"));
        }
    }
    
    @Test
    public void testAccountCannotOperateWhenInactive() {
        Account account = new SavingsAccount(
            "SAV008", 500.0, today, today, customer, AccountStatus.CLOSED
        );
        
        try {
            account.deposit(100.0);
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("inactive"));
        }
    }
}
