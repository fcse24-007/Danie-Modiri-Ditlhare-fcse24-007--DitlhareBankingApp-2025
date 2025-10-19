package model;
import java.time.LocalDate;

public class SavingsAccount extends Account implements InterestBearing {
    private double interestRate = 0.0005; // Default 0.05% interest rate

    public SavingsAccount(String accountNumber, double balance, LocalDate dateCreated,
                          LocalDate dateOpened, Customer customer, AccountStatus status) {
        super(accountNumber, balance, dateCreated, dateOpened, customer, status);
    }

    @Override
    public double calculateInterest() {
        // Implementation for calculating interest
        return getBalance() * interestRate;
    }

    @Override
    public void applyInterest() {
        // Implementation for applying interest
        double interest = calculateInterest();
        deposit(interest);
    }

    // Getters and Setters
    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    @Override
    public String toString() {
        return "SavingsAccount{" +
                "interestRate=" + interestRate +
                "} " + super.toString();
    }
}
