package model;

public interface TransactionProcessing {
    void processDeposit(Account account, double amount);
    void processWithdrawal(Account account, double amount);
    void transferFunds(Account fromAccount, Account toAccount, double amount);
}