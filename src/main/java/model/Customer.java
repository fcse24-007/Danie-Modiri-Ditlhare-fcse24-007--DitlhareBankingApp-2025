package model;

import java.util.ArrayList;
import java.util.List;

public class Customer extends User implements Auditable {
    private String customerId;
    private String firstName;
    private String surname;
    private String address;
    private String phoneNumber;
    private String email;
    private CustomerType customerType;
    private List<Account> accounts;
    private List<AuditEntry> auditTrail;

    public Customer(String userId, String username, String password,
            String customerId, String firstName, String surname, String address,
            String phoneNumber, String email, CustomerType customerType) {
        this(userId, username, password, customerId, firstName, surname, address, phoneNumber, email, customerType,
                false);
    }

    public Customer(String userId, String username, String password,
            String customerId, String firstName, String surname, String address,
            String phoneNumber, String email, CustomerType customerType, boolean isHashed) {
        super(userId, username, password, UserRole.CUSTOMER, isHashed);
        setCustomerId(customerId);
        setFirstName(firstName);
        setSurname(surname);
        setAddress(address);
        setPhoneNumber(phoneNumber);
        setEmail(email);
        setCustomerType(customerType);
        this.accounts = new ArrayList<>();
        this.auditTrail = new ArrayList<>();
    }

    public void updateProfile(String firstName, String surname, String address,
            String phoneNumber, String email) {
        setFirstName(firstName);
        setSurname(surname);
        setAddress(address);
        setPhoneNumber(phoneNumber);
        setEmail(email);

        recordAudit(Action.PROFILE_UPDATED, "Customer profile updated");
    }

    public List<Account> viewAccounts() {
        return new ArrayList<>(accounts); // Return copy to preserve encapsulation
    }

    public void addAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        accounts.add(account);
        recordAudit(Action.ACCOUNT_CREATED,
                String.format("Account %s added to customer", account.getAccountNumber()));
    }

    public List<Account> getAccounts() {
        return new ArrayList<>(accounts);
    }

    @Override
    public void recordAudit(Action action, String details) {
        AuditEntry auditEntry = new AuditEntry(
                "AUDIT_" + System.currentTimeMillis() + "_" + auditTrail.size(),
                action.toString(),
                java.time.LocalDateTime.now(),
                getUserId(),
                details);
        auditTrail.add(auditEntry);
    }

    public List<AuditEntry> getAuditTrail() {
        return new ArrayList<>(auditTrail);
    }

    public Account findAccount(String accountNumber) {
        return accounts.stream()
                .filter(acc -> acc.getAccountNumber().equals(accountNumber))
                .findFirst()
                .orElse(null);
    }

    public double getTotalBalance() {
        return accounts.stream()
                .mapToDouble(Account::getBalance)
                .sum();
    }

    // Getters and Setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }
        this.customerId = customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        if (surname == null || surname.trim().isEmpty()) {
            throw new IllegalArgumentException("Surname cannot be null or empty");
        }
        this.surname = surname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be null or empty");
        }
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }
        this.email = email;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public void setCustomerType(CustomerType customerType) {
        if (customerType == null) {
            throw new IllegalArgumentException("Customer type cannot be null");
        }
        this.customerType = customerType;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "address='" + address + '\'' +
                ", customerId='" + customerId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", surname='" + surname + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", customerType=" + customerType +
                ", accountsCount=" + accounts.size() +
                "} " + super.toString();
    }
}