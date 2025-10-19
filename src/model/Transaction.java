package model;

import java.time.LocalDateTime;

public class Transaction implements Auditable {
    private String transactionId;
    private TransactionType transactionType;
    private double amount;
    private LocalDateTime timeStamp;
    private String description;
    private Account account;

    public Transaction(String transactionId, TransactionType transactionType,
                       double amount, LocalDateTime timeStamp,
                       String description, Account account) {
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.timeStamp = timeStamp;
        this.description = description;
        this.account = account;
    }

    public void recordTransaction() {
        // Implementation for recording transaction
    }

    public Transaction getTransactionDetails() {
        // Implementation for getting transaction details
        return this;
    }

    @Override
    public void recordAudit(Action action, String details) {
        // Implementation for recording audit
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "account=" + account +
                ", transactionId='" + transactionId + '\'' +
                ", transactionType=" + transactionType +
                ", amount=" + amount +
                ", timeStamp=" + timeStamp +
                ", description='" + description + '\'' +
                '}';
    }
}
