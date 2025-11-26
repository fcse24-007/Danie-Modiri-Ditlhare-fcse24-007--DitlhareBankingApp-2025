// TransactionController.java
package controller;

import database.*;
import model.*;
import java.time.LocalDateTime;

public class TransactionController implements TransactionProcessing {
    private AccountDAO accountDAO;
    private TransactionDAO transactionDAO;
    private AuditDAO auditDAO;

    public TransactionController() {
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();
        this.auditDAO = new AuditDAO();
    }

    public DepositResult processDeposit(String accountNumber, double amount, String userId) {
        System.out.println("TransactionController: Processing deposit for account " + accountNumber + ", amount: " + amount);
        
        // Step 2: Validate input 
        if (!validateInput(accountNumber, amount)) {
            System.out.println("TransactionController: Input validation failed");
            return new DepositResult(false, 0, "Invalid input parameters", "");
        }

        try {
            // Step 4-5: Get account
            var accountOpt = accountDAO.findById(accountNumber);
            if (accountOpt.isEmpty()) {
                // Step 16-17: Account not found
                System.out.println("TransactionController: Account not found - " + accountNumber);
                auditDAO.recordAudit(userId, "DEPOSIT_FAILED", 
                    "Account not found: " + accountNumber);
                return new DepositResult(false, 0, "Account not found", "");
            }

            Account account = accountOpt.get();
            
            // Get current balance
            double currentBalance = account.getBalance();
            System.out.println("TransactionController: Current balance: " + currentBalance);
            
            //  Process deposit
            account.deposit(amount);
            
            // Update account balance in database
            accountDAO.updateBalance(accountNumber, account.getBalance());
            
            // Create and record transaction
            String transactionId = "TXN_" + System.currentTimeMillis();
            Transaction transaction = new Transaction(
                transactionId, 
                TransactionType.DEPOSIT, 
                amount, 
                "Deposit processed for account: " + accountNumber,
                account
            );
            transactionDAO.save(transaction);
            
            // Record audit
            auditDAO.recordAudit(userId, "DEPOSIT_SUCCESS", 
                String.format("Deposit of %.2f to account %s. Transaction: %s", 
                    amount, accountNumber, transactionId));
            
            // Get new balance
            double newBalance = account.getBalance();
            System.out.println("TransactionController: New balance: " + newBalance);
            
            // Return success
            System.out.println("TransactionController: Deposit SUCCESS - Transaction: " + transactionId);
            return new DepositResult(true, newBalance, 
                "Deposit processed successfully", transactionId);
            
        } catch (Exception e) {
            System.err.println("TransactionController: Deposit FAILED - " + e.getMessage());
            auditDAO.recordAudit(userId, "DEPOSIT_FAILED", 
                String.format("Deposit failed for account %s: %s", accountNumber, e.getMessage()));
            return new DepositResult(false, 0, "Deposit failed: " + e.getMessage(), "");
        }
    }

    private boolean validateInput(String accountNumber, double amount) {
        return accountNumber != null && !accountNumber.trim().isEmpty() && amount > 0;
    }

    @Override
    public void processDeposit(Account account, double amount) {
        processDeposit(account.getAccountNumber(), amount, "SYSTEM");
    }

    @Override
    public void processWithdrawal(Account account, double amount) {
        System.out.println("TransactionController: Processing withdrawal for account " + 
                          account.getAccountNumber() + ", amount: " + amount);
        
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        
        account.withdraw(amount);
        accountDAO.updateBalance(account.getAccountNumber(), account.getBalance());
        
        // Record transaction
        String transactionId = "TXN_" + System.currentTimeMillis();
        Transaction transaction = new Transaction(
            transactionId, 
            TransactionType.WITHDRAWAL, 
            amount, 
            "Withdrawal processed for account: " + account.getAccountNumber(),
            account
        );
        transactionDAO.save(transaction);
        
        System.out.println("TransactionController: Withdrawal SUCCESS - Transaction: " + transactionId);
    }

    @Override
    public void transferFunds(Account fromAccount, Account toAccount, double amount) {
        System.out.println("TransactionController: Processing transfer from " + 
                          fromAccount.getAccountNumber() + " to " + toAccount.getAccountNumber() + 
                          ", amount: " + amount);
        
        if (fromAccount == null || toAccount == null) {
            throw new IllegalArgumentException("Both accounts must be specified");
        }
        
        fromAccount.transferTo(toAccount, amount);
        
        // Update both accounts in database
        accountDAO.updateBalance(fromAccount.getAccountNumber(), fromAccount.getBalance());
        accountDAO.updateBalance(toAccount.getAccountNumber(), toAccount.getBalance());
        
        // Record transaction for from account
        String transactionId = "TXN_" + System.currentTimeMillis();
        Transaction transaction = new Transaction(
            transactionId, 
            TransactionType.TRANSFER_INTERNAL, 
            amount, 
            String.format("Transfer to account %s", toAccount.getAccountNumber()), 
            fromAccount
        );
        transactionDAO.save(transaction);
        
        System.out.println("TransactionController: Transfer SUCCESS - Transaction: " + transactionId);
    }

    // Helper class for deposit results that matches sequence diagram
    public static class DepositResult {
        private final boolean success;
        private final double newBalance;
        private final String message;
        private final String transactionId;

        public DepositResult(boolean success, double newBalance, String message, String transactionId) {
            this.success = success;
            this.newBalance = newBalance;
            this.message = message;
            this.transactionId = transactionId;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public double getNewBalance() { return newBalance; }
        public String getMessage() { return message; }
        public String getTransactionId() { return transactionId; }
    }
}
