package model;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class InvestmentAccount extends Account implements InterestBearing {
    private double interestRate = 0.065; 
    private static final double MINIMUM_INITIAL_DEPOSIT = 500.00;
    private static final double MINIMUM_BALANCE = 500.00;
    private LocalDate lastInterestApplied;

    public InvestmentAccount(String accountNumber, double balance, LocalDate dateCreated,
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
        
        if (daysSinceLastInterest < 90) { // Apply interest quarterly
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
                    String.format("Investment interest applied: P%.2f to account %s", 
                        interest, getAccountNumber()));
            }
        }
    }

    public void withdrawAmount(double amount) {
        // Investment account withdrawal restrictions
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        
        if (getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot withdraw from inactive investment account");
        }
        
        if (getBalance() - amount < MINIMUM_BALANCE) {
            throw new IllegalArgumentException(
                String.format("Withdrawal would bring balance below minimum requirement of P%.2f", 
                    MINIMUM_BALANCE));
        }
        
        if (ChronoUnit.DAYS.between(getDateOpened(), LocalDate.now()) < 30) {
            throw new IllegalArgumentException(
                "Investment accounts require 30-day notice for withdrawals");
        }
        
        // Process withdrawal
        setBalance(getBalance() - amount);
        
        // Record investment account withdrawal
        if (getCustomer() != null) {
            getCustomer().recordAudit(Action.WITHDRAWAL, 
                String.format("Investment account withdrawal: P%.2f from account %s", 
                    amount, getAccountNumber()));
        }
    }

    public boolean validateInitialDeposit(double amount) {
        return amount >= MINIMUM_INITIAL_DEPOSIT;
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

    public static double getMinimumInitialDeposit() {
        return MINIMUM_INITIAL_DEPOSIT;
    }

    public static double getMinimumBalance() {
        return MINIMUM_BALANCE;
    }

    public LocalDate getLastInterestApplied() {
        return lastInterestApplied;
    }

    @Override
    public String toString() {
        return "InvestmentAccount{" +
                "interestRate=" + interestRate +
                ", lastInterestApplied=" + lastInterestApplied +
                "} " + super.toString();
    }
}
