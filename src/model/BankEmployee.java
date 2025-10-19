package model;

public class BankEmployee extends User implements TransactionProcessing {
    private String employeeId;
    private String department;

    public BankEmployee(String userId, String username, String password,
                        String employeeId, String department) {
        super(userId, username, password, UserRole.BANK_EMPLOYEE);
        this.employeeId = employeeId;
        this.department = department;
    }

    public boolean authenticate() {
        // Implementation for bank employee authentication
        return super.authenticate();
    }

    public Account openAccount(Account account, AccountType accountType) {
        // Implementation for opening an account
        return null;
    }

    @Override
    public void processDeposit(Account account, double amount) {
        // Implementation for processing deposit
    }

    @Override
    public void processWithdrawal(Account account, double amount) {
        // Implementation for processing withdrawal
    }

    public void manageCustomer(Customer customer) {
        // Implementation for managing customer
    }

    public void createCustomerProfile(Customer customer) {
        // Implementation for creating customer profile
    }

    public void updateCustomerInfo(Customer customer) {
        // Implementation for updating customer info
    }

    public void deleteCustomerProfile(String customerId) {
        // Implementation for deleting customer profile
    }

    @Override
    public void transferFunds(Account fromAccount, Account toAccount, double amount) {
        // Implementation for transferring funds
    }

    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return "BankEmployee{" +
                "department='" + department + '\'' +
                ", employeeId='" + employeeId + '\'' +
                "} " + super.toString();
    }
}