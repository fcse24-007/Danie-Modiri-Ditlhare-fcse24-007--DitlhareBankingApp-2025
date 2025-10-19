package model;
import java.util.List;

public class Customer extends User implements Auditable {
    private String customerId;
    private String firstName;
    private String address;
    private String phoneNumber;
    private String email;
    private CustomerType customerType;

    public Customer(String userId, String username, String password,
                    String customerId, String firstName, String address,
                    String phoneNumber, String email, CustomerType customerType) {
        super(userId, username, password, UserRole.CUSTOMER);
        this.customerId = customerId;
        this.firstName = firstName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.customerType = customerType;
    }

    public void updateProfile() {
        // Implementation for updating customer profile
    }

    public List<Account> viewAccounts() {
        // Implementation for viewing accounts
        return null;
    }

    public void addAccount(Account account) {
        // Implementation for adding account
    }

    public List<Account> getAccounts() {
        // Implementation for getting accounts
        return null;
    }

    @Override
    public void recordAudit(Action action, String details) {
        // Implementation for recording audit
    }

    // Getters and Setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public void setCustomerType(CustomerType customerType) {
        this.customerType = customerType;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "address='" + address + '\'' +
                ", customerId='" + customerId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", customerType=" + customerType +
                "} " + super.toString();
    }

    public String getName() {
        return null;
    }
}