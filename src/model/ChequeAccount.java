package model;
import java.time.LocalDate;

public class ChequeAccount extends Account {
    private String employerName;
    private String employerAddress;
    private boolean employmentStatus;
    private double withdrawalAmount;
    private double depositAmount;

    public ChequeAccount(String accountNumber, double balance, LocalDate dateCreated,
                         LocalDate dateOpened, Customer customer, AccountStatus status,
                         String employerName, String employerAddress, boolean employmentStatus) {
        super(accountNumber, balance, dateCreated, dateOpened, customer, status);
        this.employerName = employerName;
        this.employerAddress = employerAddress;
        this.employmentStatus = employmentStatus;
    }

    // Getters and Setters
    public String getEmployerName() {
        return employerName;
    }

    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }

    public String getEmployerAddress() {
        return employerAddress;
    }

    public void setEmployerAddress(String employerAddress) {
        this.employerAddress = employerAddress;
    }

    public boolean isEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(boolean employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public double getWithdrawalAmount() {
        return withdrawalAmount;
    }

    public void setWithdrawalAmount(double withdrawalAmount) {
        this.withdrawalAmount = withdrawalAmount;
    }

    public double getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(double depositAmount) {
        this.depositAmount = depositAmount;
    }

    @Override
    public String toString() {
        return "ChequeAccount{" +
                "depositAmount=" + depositAmount +
                ", employerName='" + employerName + '\'' +
                ", employerAddress='" + employerAddress + '\'' +
                ", employmentStatus=" + employmentStatus +
                ", withdrawalAmount=" + withdrawalAmount +
                "} " + super.toString();
    }
}
