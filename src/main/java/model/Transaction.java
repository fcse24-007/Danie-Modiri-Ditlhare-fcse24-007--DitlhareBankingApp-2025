package model;

import java.time.LocalDateTime;

public class Transaction implements Auditable {
    private String transactionId;
    private TransactionType transactionType;
    private double amount;
    private LocalDateTime timeStamp;
    private String description;
    private Account account;
    private static int transactionCounter = 0;

    public Transaction(String transactionId, TransactionType transactionType,
                       double amount, LocalDateTime timeStamp,
                       String description, Account account) {
        setTransactionId(transactionId);
        setTransactionType(transactionType);
        setAmount(amount);
        setTimeStamp(timeStamp);
        setDescription(description);
        setAccount(account);
    }
    
    public Transaction(String transactionId, TransactionType transactionType,
                      double amount, String description, Account account) {
        this(transactionId, transactionType, amount, LocalDateTime.now(), description, account);
    }

    public Transaction(TransactionType transactionType, double amount, 
                       String description, Account account) {
        this(generateTransactionId(), transactionType, amount, 
             LocalDateTime.now(), description, account);
    }

    public void recordTransaction() {
        System.out.println("Recording transaction: " + this);
        
        // Record audit trail
        recordAudit(Action.valueOf(transactionType.toString()), 
            String.format("Transaction %s recorded for account %s", 
                transactionId, account.getAccountNumber()));
    }

    public Transaction getTransactionDetails() {
        return this;
    }

    @Override
    public void recordAudit(Action action, String details) {
        // In a real system, this would add to an audit log
        System.out.printf("AUDIT: %s - %s - %s%n", 
            LocalDateTime.now(), action, details);
    }

    public boolean process() {
        try {
            switch (transactionType) {
                case DEPOSIT:
                    account.deposit(amount);
                    break;
                case WITHDRAWAL:
                    account.withdraw(amount);
                    break;
                case INTEREST_PAYMENT:
                    account.deposit(amount);
                    break;
                case TRANSFER_INTERNAL:
                case TRANSFER_EXTERNAL:
                    // Transfers are handled differently between accounts
                    break;
            }
            recordTransaction();
            return true;
        } catch (Exception e) {
            System.err.println("Transaction failed: " + e.getMessage());
            return false;
        }
    }

    private static String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + "_" + (transactionCounter++);
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID cannot be null or empty");
        }
        this.transactionId = transactionId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        if (transactionType == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }
        this.transactionType = transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
        this.amount = amount;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        if (timeStamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        this.timeStamp = timeStamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        this.account = account;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", transactionType=" + transactionType +
                ", amount=" + amount +
                ", timeStamp=" + timeStamp +
                ", description='" + description + '\'' +
                ", account=" + account.getAccountNumber() +
                '}';
    }
}
