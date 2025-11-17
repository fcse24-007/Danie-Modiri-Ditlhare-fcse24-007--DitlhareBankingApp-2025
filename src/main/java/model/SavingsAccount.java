// SavingsAccount.java - ENHANCED VERSION
package model;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class SavingsAccount extends Account implements InterestBearing {
    private double interestRate = 0.025; // 2.5% annual interest rate
    private LocalDate lastInterestApplied;
    private double minimumBalance = 100.0; // Minimum balance requirement

    public SavingsAccount(String accountNumber, double balance, LocalDate dateCreated,
                          LocalDate dateOpened, Customer customer, AccountStatus status) {
        super(accountNumber, balance, dateCreated, dateOpened, customer, status);
        this.lastInterestApplied = dateOpened;
    }

    @Override
    public double calculateInterest() {
        if (getStatus() != AccountStatus.ACTIVE) {
            return 0.0;
        }
        
        long daysSinceLastInterest = ChronoUnit.DAYS.between(
            lastInterestApplied, LocalDate.now());
        
        if (daysSinceLastInterest < 30) { // Apply interest monthly
            return 0.0;
        }
        
        double dailyInterestRate = interestRate / 365;
        return getBalance() * dailyInterestRate * daysSinceLastInterest;
    }

    @Override
    public void applyInterest() {
        if (getStatus() != AccountStatus.ACTIVE) {
            return;
        }
        
        double interest = calculateInterest();
        if (interest > 0) {
            deposit(interest);
            lastInterestApplied = LocalDate.now();
            
            // Record audit trail
            if (getCustomer() != null) {
                getCustomer().recordAudit(Action.DEPOSIT, 
                    String.format("Interest applied: $%.2f to savings account %s", 
                        interest, getAccountNumber()));
            }
        }
    }

    @Override
    public void withdraw(double amount) {
        // Savings accounts do not allow withdrawals - only transfers are permitted
        throw new IllegalStateException("Withdrawals are not permitted from savings accounts. Use transfers instead.");
    }

    // Getters and Setters
    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        if (interestRate < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative");
        }
        this.interestRate = interestRate;
    }

    public LocalDate getLastInterestApplied() {
        return lastInterestApplied;
    }

    public double getMinimumBalance() {
        return minimumBalance;
    }

    public void setMinimumBalance(double minimumBalance) {
        if (minimumBalance < 0) {
            throw new IllegalArgumentException("Minimum balance cannot be negative");
        }
        this.minimumBalance = minimumBalance;
    }

    @Override
    public String toString() {
        return "SavingsAccount{" +
                "interestRate=" + interestRate +
                ", minimumBalance=" + minimumBalance +
                ", lastInterestApplied=" + lastInterestApplied +
                "} " + super.toString();
    }
}