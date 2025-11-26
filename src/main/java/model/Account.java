package model;
import java.time.LocalDate;

public class Account {
    private String accountNumber;
    private double balance;
    private LocalDate dateCreated;
    private LocalDate dateOpened;
    private Customer customer;
    private AccountStatus status;

    public Account(String accountNumber, double balance, LocalDate dateCreated,
                   LocalDate dateOpened, Customer customer, AccountStatus status) {
        setAccountNumber(accountNumber);
        setBalance(balance);
        setDateCreated(dateCreated);
        setDateOpened(dateOpened);
        setCustomer(customer);
        setStatus(status);
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        if (status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot deposit to inactive account");
        }
        this.balance += amount;
        
        // Record audit trail
        if (customer != null) {
            customer.recordAudit(Action.DEPOSIT, 
                String.format("Deposited %.2f to account %s. New balance: %.2f", 
                    amount, accountNumber, balance));
        }
    }

    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot withdraw from inactive account");
        }
        if (balance < amount) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        this.balance -= amount;
        
        // Record audit trail
        if (customer != null) {
            customer.recordAudit(Action.WITHDRAWAL, 
                String.format("Withdrew %.2f from account %s. New balance: %.2f", 
                    amount, accountNumber, balance));
        }
    }

    public double getBalance() {
        return balance;
    }

    public Account getAccountDetails() {
        return this;
    }

    public void updateStatus(AccountStatus status) {
        AccountStatus oldStatus = this.status;
        this.status = status;
        
        // Record audit trail
        if (customer != null) {
            customer.recordAudit(Action.STATUS_CHANGED, 
                String.format("Account %s status changed from %s to %s", 
                    accountNumber, oldStatus, status));
        }
    }

    public double calculateInterest() {
        // Base implementation
        return 0.0;
    }

    public void transferTo(Account targetAccount, double amount) {
        if (targetAccount == null) {
            throw new IllegalArgumentException("Target account cannot be null");
        }
        if (this.equals(targetAccount)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        
        // Start transaction
        try {
            // Withdraw from source
            this.withdraw(amount);
            
            // Deposit to target
            targetAccount.deposit(amount);
            
            System.out.println("Transfer completed successfully");
            
        } catch (Exception e) {
            // If any operation fails, the entire transaction should be rolled back
            System.err.println("Transfer failed: " + e.getMessage());
            throw new RuntimeException("Transfer transaction failed", e);
        }
    }

    // Getters and Setters
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be null or empty");
        }
        this.accountNumber = accountNumber;
    }

    public void setBalance(double balance) {
        if (balance < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
        this.balance = balance;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        if (dateCreated == null) {
            throw new IllegalArgumentException("Date created cannot be null");
        }
        this.dateCreated = dateCreated;
    }

    public LocalDate getDateOpened() {
        return dateOpened;
    }

    public void setDateOpened(LocalDate dateOpened) {
        if (dateOpened == null) {
            throw new IllegalArgumentException("Date opened cannot be null");
        }
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
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = status;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                ", dateCreated=" + dateCreated +
                ", dateOpened=" + dateOpened +
                ", customer=" + (customer != null ? customer.getCustomerId() : "null") +
                ", status=" + status +
                '}';
    }
}
