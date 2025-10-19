package model;
import java.time.LocalDate;
import java.util.List;

public class Account {
    private String accountNumber;
    private double balance;
    private LocalDate dateCreated;
    private LocalDate dateOpened;
    private Customer customer;
    private AccountStatus status;

    public Account(String accountNumber, double balance, LocalDate dateCreated,
                   LocalDate dateOpened, Customer customer, AccountStatus status) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.dateCreated = dateCreated;
        this.dateOpened = dateOpened;
        this.customer = customer;
        this.status = status;
    }

    public void deposit(double amount) {
        // Implementation for deposit
        this.balance += amount;
    }

    public double getBalance() {
        return balance;
    }

    public Account getAccountDetails() {
        // Implementation for getting account details
        return this;
    }

    public void updateStatus(AccountStatus status) {
        // Implementation for updating account status
        this.status = status;
    }

    public double calculateInterest() {
        // Implementation for calculating interest
        return 0.0;
    }

    public Account initAccount(String accountNumber, double initialBalance) {
        // Implementation for initializing account
        return new Account(accountNumber, initialBalance, LocalDate.now(),
                LocalDate.now(), null, AccountStatus.ACTIVE);
    }

    // Getters and Setters
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDate getDateOpened() {
        return dateOpened;
    }

    public void setDateOpened(LocalDate dateOpened) {
        this.dateOpened = dateOpened;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                ", dateCreated=" + dateCreated +
                ", dateOpened=" + dateOpened +
                ", customer=" + customer +
                ", status=" + status +
                '}';
    }
}