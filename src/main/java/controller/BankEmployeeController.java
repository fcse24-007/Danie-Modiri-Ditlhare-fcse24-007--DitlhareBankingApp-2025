// BankEmployeeController.java
package controller;

import database.*;
import model.*;
import view.BankEmployeeDashboard;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BankEmployeeController {
    private CustomerDAO customerDAO;
    private AccountDAO accountDAO;
    private AuditDAO auditDAO;
    private TransactionController transactionController;

    public BankEmployeeController() {
        this.customerDAO = new CustomerDAO();
        this.accountDAO = new AccountDAO();
        this.auditDAO = new AuditDAO();
        this.transactionController = new TransactionController();
    }

    public Customer findCustomerByIdOrUsername(String search) {
        try {
            // Try finding by customer ID first
            Optional<Customer> customerOpt = customerDAO.findById(search);
            if (customerOpt.isPresent()) {
                return customerOpt.get();
            }
            // Then try by username via userDAO
            Optional<model.User> userOpt = customerDAO.getUserDAO().findByUsername(search);
            if (userOpt.isPresent()) {
                model.User user = userOpt.get();
                Optional<Customer> customerByUserOpt = customerDAO.findByUserId(user.getUserId());
                if (customerByUserOpt.isPresent()) {
                    return customerByUserOpt.get();
                }
            }
        } catch (Exception e) {
            System.err.println("BankEmployeeController: Error finding customer by ID or username: " + e.getMessage());
        }
        return null;
    }

    public boolean updateCustomer(String userId, String username, String password,
                                   String firstName, String surname, String address,
                                   String phoneNumber, String email, model.CustomerType customerType,
                                   String employeeId) {
        try {
            // Fetch existing customer by user ID
            Optional<Customer> customerOpt = customerDAO.findByUserId(userId);
            if (customerOpt.isEmpty()) {
                System.err.println("BankEmployeeController: Customer not found with userId: " + userId);
                return false;
            }
            Customer customer = customerOpt.get();
            // Update fields
            customer.setUsername(username);
            if (password != null && !password.isBlank()) {
                customer.setPassword(password);
            }
            customer.setFirstName(firstName);
            customer.setSurname(surname);
            customer.setAddress(address);
            customer.setPhoneNumber(phoneNumber);
            customer.setEmail(email);
            customer.setCustomerType(customerType);

            boolean updateResult = customerDAO.update(customer);
            if (updateResult) {
                auditDAO.recordAudit(employeeId, "CUSTOMER_UPDATED", "Updated customer profile: " + customer.getCustomerId());
                System.out.println("BankEmployeeController: Customer updated successfully - " + customer.getCustomerId());
                return true;
            }
        } catch (Exception e) {
            System.err.println("BankEmployeeController: Error updating customer: " + e.getMessage());
        }
        return false;
    }

    public Customer createCustomer(String userId, String username, String password,
                                     String customerId, String firstName, String surname,
                                     String address, String phoneNumber, String email,
                                     CustomerType customerType, String employeeId) {
        System.out.println("BankEmployeeController: Creating customer " + firstName + " " + surname);
        
        Customer customer = new Customer(userId, username, password, customerId,
                firstName, surname, address, phoneNumber, email, customerType);
        
        if (customerDAO.save(customer)) {
            auditDAO.recordAudit(employeeId, "CUSTOMER_CREATED", 
                "Created customer profile: " + customerId);
            System.out.println("BankEmployeeController: Customer created successfully - " + customerId);
            return customer;
        } else {
            throw new RuntimeException("Failed to create customer profile");
        }
    }
    
    public boolean deleteCustomerById(String customerId) {
        try {
            // First get the user_id for the audit trail
            String userId = null;
            var customerOpt = customerDAO.findById(customerId);
            if (customerOpt.isPresent()) {
                userId = customerOpt.get().getUserId();
            }
            
            boolean result = customerDAO.delete(customerId);
            if (result) {
                // Record audit with actual customer user ID if available
                if (userId != null) {
                    auditDAO.recordAudit(userId, "CUSTOMER_DELETED", "Deleted customer profile: " + customerId);
                }
                System.out.println("BankEmployeeController: Customer deleted successfully - " + customerId);
                return true;
            }
        } catch (Exception e) {
            System.err.println("BankEmployeeController: Error deleting customer: " + e.getMessage());
        }
        return false;
    }

    public Account openAccount(Customer customer, AccountType accountType, 
                             double initialDeposit, String employeeId) {
        System.out.println("BankEmployeeController: Opening " + accountType + 
                          " account for customer " + customer.getCustomerId());
        
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

        if (accountDAO.save(account)) {
            customer.addAccount(account);
            auditDAO.recordAudit(employeeId, "ACCOUNT_OPENED", 
                String.format("Opened %s account %s for customer %s", 
                    accountType, accountNumber, customer.getCustomerId()));
            
            System.out.println("BankEmployeeController: Account opened successfully - " + accountNumber);
            return account;
        } else {
            throw new RuntimeException("Failed to open account");
        }
    }

    public Account openChequeAccount(Customer customer, double initialDeposit,
                                    String employerName, String employerAddress,
                                    boolean employmentStatus, String employeeId) {
        System.out.println("BankEmployeeController: Opening CHEQUE account with employment details for customer " + customer.getCustomerId());
        
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        if (initialDeposit < 0) {
            throw new IllegalArgumentException("Initial deposit cannot be negative");
        }
        if (employerName == null || employerName.isEmpty()) {
            throw new IllegalArgumentException("Employer name is required");
        }
        if (employerAddress == null || employerAddress.isEmpty()) {
            throw new IllegalArgumentException("Employer address is required");
        }

        String accountNumber = generateAccountNumber();
        ChequeAccount account = new ChequeAccount(
            accountNumber, initialDeposit,
            LocalDate.now(), LocalDate.now(), customer, AccountStatus.ACTIVE,
            employerName, employerAddress, employmentStatus
        );

        if (accountDAO.save(account)) {
            customer.addAccount(account);
            auditDAO.recordAudit(employeeId, "ACCOUNT_OPENED", 
                String.format("Opened CHEQUE account %s for customer %s with employer: %s", 
                    accountNumber, customer.getCustomerId(), employerName));
            
            System.out.println("BankEmployeeController: Cheque account opened successfully - " + accountNumber);
            return account;
        } else {
            throw new RuntimeException("Failed to open cheque account");
        }
    }

    public TransactionController.DepositResult processDepositForCustomer(
            String accountNumber, double amount, String employeeId) {
        return transactionController.processDeposit(accountNumber, amount, employeeId);
    }

    private String generateAccountNumber() {
        return "ACC" + System.currentTimeMillis();
    }

    // In BankEmployeeController.java - Add account closure (AM-003)
    public boolean closeAccount(String accountNumber, String employeeId) {
        try {
            var accountOpt = accountDAO.findById(accountNumber);
            if (accountOpt.isEmpty()) {
                throw new IllegalArgumentException("Account not found: " + accountNumber);
            }
            
            Account account = accountOpt.get();
            
            // Check if account has zero balance
            if (account.getBalance() > 0) {
                throw new IllegalStateException(
                    "Cannot close account with positive balance. Balance must be zero.");
            }
            
            // Update account status to CLOSED
            account.updateStatus(AccountStatus.CLOSED);
            accountDAO.update(account);
            
            // Record audit
            auditDAO.recordAudit(employeeId, "ACCOUNT_CLOSED", 
                "Account closed: " + accountNumber);
            
            System.out.println("BankEmployeeController: Account closed successfully - " + accountNumber);
            return true;
            
        } catch (Exception e) {
            System.err.println("BankEmployeeController: Error closing account - " + e.getMessage());
            auditDAO.recordAudit(employeeId, "ACCOUNT_CLOSURE_FAILED", 
                "Failed to close account: " + accountNumber + " - " + e.getMessage());
            return false;
        }
    }

}