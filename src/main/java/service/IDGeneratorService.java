// IDGeneratorService.java
package service;

import database.CustomerDAO;
import database.UserDAO;
import model.UserRole;

import java.util.concurrent.atomic.AtomicInteger;

public class IDGeneratorService {
    private static final AtomicInteger customerCounter = new AtomicInteger(0);
    private static final AtomicInteger employeeCounter = new AtomicInteger(0);
    private static final AtomicInteger adminCounter = new AtomicInteger(0);
    
    private UserDAO userDAO;
    private CustomerDAO customerDAO;
    
    public IDGeneratorService() {
        this.userDAO = new UserDAO();
        this.customerDAO = new CustomerDAO();
        initializeCounters();
    }
    
    private void initializeCounters() {
        initializeCustomerCounter();
        initializeEmployeeCounter();
        initializeAdminCounter();
    }
    
    private void initializeCustomerCounter() {
        try {
            var customers = customerDAO.findAll();
            int maxId = customers.stream()
                .map(customer -> customer.getCustomerId())
                .filter(id -> id.startsWith("CUST-"))
                .map(id -> {
                    try {
                        return Integer.parseInt(id.replace("CUST-", ""));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
            customerCounter.set(maxId);
        } catch (Exception e) {
            System.err.println("Error initializing customer counter: " + e.getMessage());
            customerCounter.set(0);
        }
    }
    
    private void initializeEmployeeCounter() {
        try {
            var users = userDAO.findAll();
            int maxId = users.stream()
                .filter(user -> user.getRole() == UserRole.BANK_EMPLOYEE)
                .map(user -> user.getUserId())
                .filter(id -> id.startsWith("BE-"))
                .map(id -> {
                    try {
                        return Integer.parseInt(id.replace("BE-", ""));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
            employeeCounter.set(maxId);
        } catch (Exception e) {
            System.err.println("Error initializing employee counter: " + e.getMessage());
            employeeCounter.set(0);
        }
    }
    
    private void initializeAdminCounter() {
        try {
            var users = userDAO.findAll();
            int maxId = users.stream()
                .filter(user -> user.getRole() == UserRole.ADMINISTRATOR)
                .map(user -> user.getUserId())
                .filter(id -> id.startsWith("ADM-"))
                .map(id -> {
                    try {
                        return Integer.parseInt(id.replace("ADM-", ""));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
            adminCounter.set(maxId);
        } catch (Exception e) {
            System.err.println("Error initializing admin counter: " + e.getMessage());
            adminCounter.set(0);
        }
    }
    
    public String generateCustomerId() {
        int nextId = customerCounter.incrementAndGet();
        return String.format("CUST-%03d", nextId);
    }
    
    public String generateCustomerUserId() {
        int nextId = customerCounter.incrementAndGet();
        return String.format("CUST-%03d", nextId);
    }
    
    public String generateBankEmployeeId() {
        int nextId = employeeCounter.incrementAndGet();
        return String.format("BE-%03d", nextId);
    }
    
    public String generateAdminId() {
        int nextId = adminCounter.incrementAndGet();
        return String.format("ADM-%03d", nextId);
    }
    
    public String generateUserId(UserRole role) {
        switch (role) {
            case CUSTOMER:
                return generateCustomerUserId();
            case BANK_EMPLOYEE:
                return generateBankEmployeeId();
            case ADMINISTRATOR:
                return generateAdminId();
            default:
                throw new IllegalArgumentException("Unknown user role: " + role);
        }
    }
}