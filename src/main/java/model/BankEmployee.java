package model;
import java.time.LocalDate;

public class BankEmployee extends User implements TransactionProcessing {
    private String employeeId;
    private String department;
    private static int employeeCount = 0;

    public BankEmployee(String userId, String username, String password,
                        String employeeId, String department) {
        super(userId, username, password, UserRole.BANK_EMPLOYEE);
        setEmployeeId(employeeId);
        setDepartment(department);
        employeeCount++;
    }

    @Override
    public boolean authenticate() {
        // Additional authentication logic for employees
        return super.authenticate() && department != null && !department.trim().isEmpty();
    }

    public Account openAccount(Customer customer, AccountType accountType, double initialDeposit) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        if (initialDeposit < 0) {
            throw new IllegalArgumentException("Initial deposit cannot be negative");
        }

        String accountNumber = generateAccountNumber();
        Account account;
        
        switch (accountType) {
            case SAVINGS:
                account = new SavingsAccount(accountNumber, initialDeposit, 
                    LocalDate.now(), LocalDate.now(), customer, AccountStatus.ACTIVE);
                break;
            case CHEQUE:
                account = new ChequeAccount(accountNumber, initialDeposit,
                    LocalDate.now(), LocalDate.now(), customer, AccountStatus.ACTIVE,
                    "", "", true);
                break;
            case INVESTMENT:
                InvestmentAccount investmentAccount = new InvestmentAccount(accountNumber, initialDeposit,
                    LocalDate.now(), LocalDate.now(), customer, AccountStatus.ACTIVE);
                if (!investmentAccount.validateInitialDeposit(initialDeposit)) {
                    throw new IllegalArgumentException(
                        "Investment account requires minimum deposit of " + 
                        InvestmentAccount.getMinimumInitialDeposit());
                }
                account = investmentAccount;
                break;
            default:
                throw new IllegalArgumentException("Unsupported account type: " + accountType);
        }

        customer.addAccount(account);
        customer.recordAudit(Action.ACCOUNT_CREATED, 
            String.format("Account %s opened by employee %s", accountNumber, employeeId));
        
        return account;
    }

    @Override
    public void processDeposit(Account account, double amount) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        account.deposit(amount);
    }

    @Override
    public void processWithdrawal(Account account, double amount) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        account.withdraw(amount);
    }

    public void manageCustomer(Customer customer) {
        // Implementation for managing customer
        if (customer != null) {
            customer.recordAudit(Action.ACCOUNT_UPDATED, 
                "Customer managed by employee: " + employeeId);
        }
    }

    public Customer createCustomerProfile(String userId, String username, String password,
                                        String customerId, String firstName, String surname, 
                                        String address, String phoneNumber, String email, 
                                        CustomerType customerType) {
        Customer customer = new Customer(userId, username, password, customerId,
                firstName, surname, address, phoneNumber, email, customerType);
        
        customer.recordAudit(Action.ACCOUNT_CREATED, 
            "Customer profile created by employee: " + employeeId);
        
        return customer;
    }

    public void updateCustomerInfo(Customer customer, String firstName, String surname, 
                                  String address, String phoneNumber, String email) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        customer.updateProfile(firstName, surname, address, phoneNumber, email);
    }

    public void deleteCustomerProfile(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        
        // Close all accounts first
        customer.getAccounts().forEach(account -> 
            account.updateStatus(AccountStatus.CLOSED));
        
        customer.recordAudit(Action.ACCOUNT_CLOSED, 
            "Customer profile deleted by employee: " + employeeId);
    }

    @Override
    public void transferFunds(Account fromAccount, Account toAccount, double amount) {
        if (fromAccount == null || toAccount == null) {
            throw new IllegalArgumentException("Both accounts must be specified");
        }
        fromAccount.transferTo(toAccount, amount);
    }

    private String generateAccountNumber() {
        return "ACC" + System.currentTimeMillis() + "_" + (employeeCount++);
    }

    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be null or empty");
        }
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department cannot be null or empty");
        }
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
