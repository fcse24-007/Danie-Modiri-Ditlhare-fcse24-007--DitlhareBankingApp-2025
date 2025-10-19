package model;
import java.time.LocalDate;

public class InvestmentAccount extends Account implements InterestBearing {
    private double interestRate = 0.05; // Default 5% interest rate
    private static final double MINIMUM_INITIAL_DEPOSIT = 500.00;

    public InvestmentAccount(String accountNumber, double balance, LocalDate dateCreated,
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

    public void withdrawAmount(double amount) {
        // Implementation for withdrawal
        if (getBalance() >= amount) {
            setBalance(getBalance() - amount);
        }
    }

    public boolean validateInitialDeposit(double amount) {
        // Implementation for validating initial deposit
        return amount >= MINIMUM_INITIAL_DEPOSIT;
    }

    public double getInvestmentAccountInitialDeposit() {
        return MINIMUM_INITIAL_DEPOSIT;
    }

    // Getters and Setters
    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public static double getMinimumInitialDeposit() {
        return MINIMUM_INITIAL_DEPOSIT;
    }

    @Override
    public String toString() {
        return "InvestmentAccount{" +
                "interestRate=" + interestRate +
                "} " + super.toString();
    }
}