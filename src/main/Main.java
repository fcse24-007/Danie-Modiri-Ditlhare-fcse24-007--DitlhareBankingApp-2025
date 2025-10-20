package main;

import dao.*;
import model.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static CustomerDAO customerDAO = new CustomerDAO();
    private static AccountDAO accountDAO = new AccountDAO();
    private static TransactionDAO transactionDAO = new TransactionDAO();
    private static UserDAO userDAO = new UserDAO();
    private static AuditEntryDAO auditDAO = new AuditEntryDAO();

    public static void main(String[] args) {
        System.out.println(" === WELCOME TO DITLHARE BANKING APP === \n");

     /* // Test database connection first
        if (!testDatabaseConnection()) {
            System.out.println("Cannot start application - Database connection failed");
            return;
        }*/

        showMainMenu();
    }

    /*private static boolean testDatabaseConnection() {
        System.out.println("🔌 Testing database connection...");
        try {
            if (DBConnection.getInstance().isConnected()) {
                System.out.println("Database connected successfully!\n");
                return true;
            } else {
                System.out.println("Database connection failed");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error testing database: " + e.getMessage());
            return false;
        }
    }*/

    private static void showMainMenu() {
        while (true) {
            System.out.println(" === MAIN MENU ===");
            System.out.println("1. Customer Management");
            System.out.println("2. Account Operations");
            System.out.println("3. Transaction Processing");
            System.out.println("4. System Reports");
            System.out.println("5. Exit");
            System.out.print("Select an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    showCustomerMenu();
                    break;
                case 2:
                    showAccountMenu();
                    break;
                case 3:
                    showTransactionMenu();
                    break;
                case 4:
                    showReportsMenu();
                    break;
                case 5:
                    System.out.println("Thank you for using Banking System!");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void showCustomerMenu() {
        while (true) {
            System.out.println("\n === CUSTOMER MANAGEMENT ===");
            System.out.println("1. Register New Customer");
            System.out.println("2. View Customer Details");
            System.out.println("3. List All Customers");
            System.out.println("4. Back to Main Menu");
            System.out.print("Select an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    registerCustomer();
                    break;
                case 2:
                    viewCustomerDetails();
                    break;
                case 3:
                    listAllCustomers();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void showAccountMenu() {
        while (true) {
            System.out.println("\n === ACCOUNT OPERATIONS ===");
            System.out.println("1. Open New Account");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. View Account Details");
            System.out.println("5. List All Accounts");
            System.out.println("6. Back to Main Menu");
            System.out.print("Select an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    openNewAccount();
                    break;
                case 2:
                    depositMoney();
                    break;
                case 3:
                    withdrawMoney();
                    break;
                case 4:
                    viewAccountDetails();
                    break;
                case 5:
                    listAllAccounts();
                    break;
                case 6:
                    return;
                default:
                    System.out.println(" Invalid option. Please try again.");
            }
        }
    }

    private static void showTransactionMenu() {
        while (true) {
            System.out.println("\n === TRANSACTION PROCESSING ===");
            System.out.println("1. View Transaction History");
            System.out.println("2. Find Transactions by Account");
            System.out.println("3. View All Transactions");
            System.out.println("4. Back to Main Menu");
            System.out.print("Select an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    viewTransactionHistory();
                    break;
                case 2:
                    findTransactionsByAccount();
                    break;
                case 3:
                    viewAllTransactions();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void showReportsMenu() {
        while (true) {
            System.out.println("\n=== SYSTEM REPORTS ===");
            System.out.println("1. System Statistics");
            System.out.println("2. Audit Trail");
            System.out.println("3. Customer Report");
            System.out.println("4. Accounts Summary");
            System.out.println("5. Back to Main Menu");
            System.out.print("Select an option: ");

            int choice = getIntInput();

            switch (choice) {
                case 1:
                    showSystemStatistics();
                    break;
                case 2:
                    showAuditTrail();
                    break;
                case 3:
                    showCustomerReport();
                    break;
                case 4:
                    showAccountsSummary();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // ========== CUSTOMER OPERATIONS ==========

    private static void registerCustomer() {
        System.out.println("\n=== REGISTER NEW CUSTOMER ===");

        System.out.print("Enter User ID: ");
        String userId = scanner.nextLine();
        System.out.print("Enter Username: ");
        String username = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        System.out.print("Enter Customer ID: ");
        String customerId = scanner.nextLine();
        System.out.print("Enter First Name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter Address: ");
        String address = scanner.nextLine();
        System.out.print("Enter Phone Number: ");
        String phoneNumber = scanner.nextLine();
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();

        System.out.println("Select Customer Type:");
        System.out.println("1. Individual");
        System.out.println("2. Joint");
        System.out.println("3. Business");
        System.out.print("Choice: ");
        int typeChoice = getIntInput();

        CustomerType customerType = CustomerType.INDIVIDUAL;
        switch (typeChoice) {
            case 1: customerType = CustomerType.INDIVIDUAL; break;
            case 2: customerType = CustomerType.JOINT; break;
            case 3: customerType = CustomerType.BUSINESS; break;
            default:
                System.out.println("Invalid choice, using Individual");
        }

        Customer customer = new Customer(userId, username, password, customerId,
                firstName, address, phoneNumber, email, customerType);

        Customer saved = customerDAO.save(customer);
        if (saved != null) {
            System.out.println("Customer registered successfully!");

            // Record audit entry
            auditDAO.save(new AuditEntry(
                    "AUD-" + System.currentTimeMillis(),
                    "CUSTOMER_REGISTERED",
                    LocalDateTime.now(),
                    userId,
                    "New customer " + firstName + " registered"
            ));
        } else {
            System.out.println("Failed to register customer");
        }
    }

    private static void viewCustomerDetails() {
        System.out.print("\nEnter Customer ID: ");
        String customerId = scanner.nextLine();

        Customer customer = customerDAO.findById(customerId);
        if (customer != null) {
            System.out.println("\nCustomer Found:");
            System.out.println("Name: " + customer.getFirstName());
            System.out.println("Email: " + customer.getEmail());
            System.out.println("Phone: " + customer.getPhoneNumber());
            System.out.println("Address: " + customer.getAddress());
            System.out.println("Type: " + customer.getCustomerType());

            // Show customer's accounts
            List<Account> accounts = accountDAO.findAccountsByCustomerId(customerId);
            System.out.println("Accounts: " + accounts.size());
        } else {
            System.out.println("Customer not found");
        }
    }

    private static void listAllCustomers() {
        List<Customer> customers = customerDAO.findAll();
        System.out.println("\n=== ALL CUSTOMERS ===");
        if (customers.isEmpty()) {
            System.out.println("No customers found");
        } else {
            for (Customer customer : customers) {
                System.out.printf("ID: %s | Name: %s | Email: %s | Type: %s%n",
                        customer.getCustomerId(), customer.getFirstName(),
                        customer.getEmail(), customer.getCustomerType());
            }
            System.out.println("Total: " + customers.size() + " customers");
        }
    }

    // ========== ACCOUNT OPERATIONS ==========

    private static void openNewAccount() {
        System.out.println("\n=== OPEN NEW ACCOUNT ===");

        System.out.print("Enter Customer ID: ");
        String customerId = scanner.nextLine();

        Customer customer = customerDAO.findById(customerId);
        if (customer == null) {
            System.out.println("Customer not found");
            return;
        }

        System.out.print("Enter Account Number: ");
        String accountNumber = scanner.nextLine();
        System.out.print("Enter Initial Balance: ");
        double balance = getDoubleInput();

        System.out.println("Select Account Type:");
        System.out.println("1. Savings Account");
        System.out.println("2. Investment Account");
        System.out.println("3. Cheque Account");
        System.out.print("Choice: ");
        int typeChoice = getIntInput();

        Account account = null;
        switch (typeChoice) {
            case 1:
                account = new SavingsAccount(accountNumber, balance, LocalDate.now(),
                        LocalDate.now(), customer, AccountStatus.ACTIVE);
                break;
            case 2:
                account = new InvestmentAccount(accountNumber, balance, LocalDate.now(),
                        LocalDate.now(), customer, AccountStatus.ACTIVE);
                break;
            case 3:
                account = new ChequeAccount(accountNumber, balance, LocalDate.now(),
                        LocalDate.now(), customer, AccountStatus.ACTIVE,
                        "N/A", "N/A", true);
                break;
            default:
                System.out.println("Invalid choice");
                return;
        }

        Account saved = accountDAO.save(account);
        if (saved != null) {
            System.out.println("Account opened successfully!");

            // Record transaction for initial deposit
            if (balance > 0) {
                Transaction transaction = new Transaction(
                        "TXN-" + System.currentTimeMillis(),
                        TransactionType.DEPOSIT,
                        balance,
                        LocalDateTime.now(),
                        "Initial deposit",
                        saved
                );
                transactionDAO.save(transaction);
            }
        } else {
            System.out.println("Failed to open account");
        }
    }

    private static void depositMoney() {
        System.out.println("\n=== DEPOSIT MONEY ===");

        System.out.print("Enter Account Number: ");
        String accountNumber = scanner.nextLine();

        Account account = accountDAO.findById(accountNumber);
        if (account == null) {
            System.out.println("Account not found");
            return;
        }

        System.out.print("Enter Deposit Amount: ");
        double amount = getDoubleInput();

        if (amount <= 0) {
            System.out.println("Invalid amount");
            return;
        }

        double oldBalance = account.getBalance();
        account.deposit(amount);

        if (accountDAO.updateBalance(accountNumber, account.getBalance())) {
            System.out.println("Deposit successful!");
            System.out.printf("Old Balance: P%.2f | New Balance: P%.2f%n", oldBalance, account.getBalance());

            // Record transaction
            Transaction transaction = new Transaction(
                    "TXN-" + System.currentTimeMillis(),
                    TransactionType.DEPOSIT,
                    amount,
                    LocalDateTime.now(),
                    "Cash deposit",
                    account
            );
            transactionDAO.save(transaction);
        } else {
            System.out.println("Deposit failed");
        }
    }

    private static void withdrawMoney() {
        System.out.println("\n=== WITHDRAW MONEY ===");

        System.out.print("Enter Account Number: ");
        String accountNumber = scanner.nextLine();

        Account account = accountDAO.findById(accountNumber);
        if (account == null) {
            System.out.println("Account not found");
            return;
        }

        System.out.print("Enter Withdrawal Amount: ");
        double amount = getDoubleInput();

        if (amount <= 0) {
            System.out.println("Invalid amount");
            return;
        }

        if (amount > account.getBalance()) {
            System.out.println("Insufficient funds");
            return;
        }

        double oldBalance = account.getBalance();
        account.setBalance(account.getBalance() - amount);

        if (accountDAO.updateBalance(accountNumber, account.getBalance())) {
            System.out.println("Withdrawal successful!");
            System.out.printf("Old Balance: P%.2f | New Balance: P%.2f%n", oldBalance, account.getBalance());

            // Record transaction
            Transaction transaction = new Transaction(
                    "TXN-" + System.currentTimeMillis(),
                    TransactionType.WITHDRAWAL,
                    amount,
                    LocalDateTime.now(),
                    "Cash withdrawal",
                    account
            );
            transactionDAO.save(transaction);
        } else {
            System.out.println("Withdrawal failed");
        }
    }

    // ========== TRANSACTION OPERATIONS ==========

    private static void viewTransactionHistory() {
        System.out.print("\nEnter Account Number: ");
        String accountNumber = scanner.nextLine();

        List<Transaction> transactions = transactionDAO.findTransactionsByAccount(accountNumber);
        System.out.println("\n === TRANSACTION HISTORY ===");
        if (transactions.isEmpty()) {
            System.out.println("No transactions found for account: " + accountNumber);
        } else {
            System.out.printf("%-12s %-15s %-10s %-20s %s%n",
                    "ID", "Type", "Amount", "Date", "Description");
            System.out.println("------------------------------------------------------------------------");
            for (Transaction txn : transactions) {
                System.out.printf("%-12s %-15s P%-9.2f %-20s %s%n",
                        txn.getTransactionId().substring(0, Math.min(8, txn.getTransactionId().length())),
                        txn.getTransactionType(),
                        txn.getAmount(),
                        txn.getTimeStamp().toLocalDate(),
                        txn.getDescription());
            }
            System.out.println("Total transactions: " + transactions.size());
        }
    }

    // ========== REPORT OPERATIONS ==========

    private static void showSystemStatistics() {
        System.out.println("\n === SYSTEM STATISTICS ===");

        int userCount = userDAO.findAll().size();
        int customerCount = customerDAO.findAll().size();
        int accountCount = accountDAO.findAll().size();
        int transactionCount = transactionDAO.findAll().size();
        int auditCount = auditDAO.findAll().size();

        double totalAssets = accountDAO.findAll().stream()
                .mapToDouble(Account::getBalance)
                .sum();

        System.out.println("Users: " + userCount);
        System.out.println("Customers: " + customerCount);
        System.out.println("Accounts: " + accountCount);
        System.out.println("Transactions: " + transactionCount);
        System.out.println("Audit Entries: " + auditCount);
        System.out.printf("Total Assets: P%.2f%n", totalAssets);
    }

    // ========== HELPER METHODS ==========

    private static int getIntInput() {
        while (true) {
            try {
                int input = Integer.parseInt(scanner.nextLine());
                return input;
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }

    private static double getDoubleInput() {
        while (true) {
            try {
                double input = Double.parseDouble(scanner.nextLine());
                return input;
            } catch (NumberFormatException e) {
                System.out.print(" Invalid input. Please enter a number: ");
            }
        }
    }

    // Add these missing methods for completeness:

    private static void viewAccountDetails() {
        System.out.print("\nEnter Account Number: ");
        String accountNumber = scanner.nextLine();

        Account account = accountDAO.findById(accountNumber);
        if (account != null) {
            System.out.println("\n Account Details:");
            System.out.println("Account Number: " + account.getAccountNumber());
            System.out.printf("Balance: P%.2f%n", account.getBalance());
            System.out.println("Status: " + account.getStatus());
            System.out.println("Customer: " + account.getCustomer().getFirstName());
            System.out.println("Date Opened: " + account.getDateOpened());

            if (account instanceof SavingsAccount) {
                System.out.println("Type: Savings Account");
            } else if (account instanceof InvestmentAccount) {
                System.out.println("Type: Investment Account");
            } else if (account instanceof ChequeAccount) {
                System.out.println("Type: Cheque Account");
            }
        } else {
            System.out.println(" Account not found");
        }
    }

    private static void listAllAccounts() {
        List<Account> accounts = accountDAO.findAll();
        System.out.println("\n === ALL ACCOUNTS ===");
        if (accounts.isEmpty()) {
            System.out.println("No accounts found");
        } else {
            for (Account account : accounts) {
                String type = account instanceof SavingsAccount ? "Savings" :
                        account instanceof InvestmentAccount ? "Investment" : "Cheque";
                System.out.printf("Account: %s | Balance: P%.2f | Type: %s | Customer: %s%n",
                        account.getAccountNumber(), account.getBalance(),
                        type, account.getCustomer().getFirstName());
            }
            System.out.println("Total: " + accounts.size() + " accounts");
        }
    }

    private static void findTransactionsByAccount() {
        viewTransactionHistory(); // Reuse the same functionality
    }

    private static void viewAllTransactions() {
        List<Transaction> transactions = transactionDAO.findAll();
        System.out.println("\n=== ALL TRANSACTIONS ===");
        if (transactions.isEmpty()) {
            System.out.println("No transactions found");
        } else {
            for (Transaction txn : transactions) {
                System.out.printf("%s | %s | P%.2f | %s | %s%n",
                        txn.getTransactionId().substring(0, 8),
                        txn.getTransactionType(),
                        txn.getAmount(),
                        txn.getTimeStamp().toLocalDate(),
                        txn.getDescription());
            }
            System.out.println("Total: " + transactions.size() + " transactions");
        }
    }

    private static void showAuditTrail() {
        List<AuditEntry> audits = auditDAO.findAll();
        System.out.println("\n=== AUDIT TRAIL ===");
        if (audits.isEmpty()) {
            System.out.println("No audit entries found");
        } else {
            for (AuditEntry audit : audits) {
                System.out.printf("%s | %s | %s | %s%n",
                        audit.getTimeStamp().toLocalDate(),
                        audit.getAction(),
                        audit.getUserId(),
                        audit.getDetails());
            }
            System.out.println("Total: " + audits.size() + " audit entries");
        }
    }

    private static void showCustomerReport() {
        listAllCustomers(); // Reuse existing functionality
    }

    private static void showAccountsSummary() {
        listAllAccounts(); // Reuse existing functionality
    }
}