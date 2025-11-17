// BankEmployeeDashboard.java
package view;

import java.util.List;

import controller.BankEmployeeController;
import controller.TransactionController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.beans.value.ObservableValue;
import model.Account;
import model.User;
import service.IDGeneratorService;
import database.AccountDAO;

public class BankEmployeeDashboard {
    private Stage stage;
    private User currentUser;
    private BankEmployeeController employeeController;
    private TransactionController transactionController;

    public BankEmployeeDashboard(User user) {
        this.stage = new Stage();
        this.currentUser = user;
        this.employeeController = new BankEmployeeController();
        this.transactionController = new TransactionController();
        initializeUI();
    }

    private void initializeUI() {
        stage.setTitle("Bank Employee Dashboard - Banking System");
        
        // Create tab pane for different functionalities
        TabPane tabPane = new TabPane();

        // Customer Management Tab
        Tab customerTab = new Tab("Customer Management");
        customerTab.setContent(createCustomerManagementTab());
        customerTab.setClosable(false);

        // Account Management Tab
        Tab accountTab = new Tab("Account Management");
        accountTab.setContent(createAccountManagementTab());
        accountTab.setClosable(false);

        // Deposit Processing Tab
        Tab depositTab = new Tab("Process Deposits");
        depositTab.setContent(createDepositProcessingTab());
        depositTab.setClosable(false);

        tabPane.getTabs().addAll(customerTab, accountTab, depositTab);

        // Main layout
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(15));
        
        // Header
        Label headerLabel = new Label("Bank Employee Dashboard");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // User info and logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        logoutButton.setOnAction(e -> logout());
        
        mainLayout.getChildren().addAll(headerLabel, logoutButton,tabPane);

        Scene scene = new Scene(mainLayout, 900, 700);
        stage.setScene(scene);
    }

    private VBox createCustomerManagementTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(20));

        Label titleLabel = new Label("Create New Customer");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Form fields
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10));

        // Customer details
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Enter first name");
        TextField surnameField = new TextField();
        surnameField.setPromptText("Enter surname");
        TextField addressField = new TextField();
        addressField.setPromptText("Enter address");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Enter phone number");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter email");
        ComboBox<String> customerTypeCombo = new ComboBox<>();
        customerTypeCombo.getItems().addAll("INDIVIDUAL", "JOINT", "BUSINESS");
        customerTypeCombo.setValue("INDIVIDUAL");

        // Add form fields
        int row = 0;
        formGrid.add(new Label("Username:"), 0, row);
        formGrid.add(usernameField, 1, row++);
        formGrid.add(new Label("Password:"), 0, row);
        formGrid.add(passwordField, 1, row++);
        formGrid.add(new Label("First Name:"), 0, row);
        formGrid.add(firstNameField, 1, row++);
        formGrid.add(new Label("Surname:"), 0, row);
        formGrid.add(surnameField, 1, row++);
        formGrid.add(new Label("Address:"), 0, row);
        formGrid.add(addressField, 1, row++);
        formGrid.add(new Label("Phone:"), 0, row);
        formGrid.add(phoneField, 1, row++);
        formGrid.add(new Label("Email:"), 0, row);
        formGrid.add(emailField, 1, row++);
        formGrid.add(new Label("Customer Type:"), 0, row);
        formGrid.add(customerTypeCombo, 1, row++);

        Button createCustomerButton = new Button("Create Customer");
        createCustomerButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");

        Label statusLabel = new Label();

        createCustomerButton.setOnAction(e -> {
            try {
                IDGeneratorService idGenerator = new IDGeneratorService();
                
                // Auto-generate customer ID and user ID
                String customerId = idGenerator.generateCustomerId();
                String userId = idGenerator.generateCustomerUserId();
                
                // Create customer using controller with auto-generated IDs
                var customer = employeeController.createCustomer(
                    userId, 
                    usernameField.getText(), 
                    passwordField.getText(),
                    customerId, 
                    firstNameField.getText(), 
                    surnameField.getText(),
                    addressField.getText(), 
                    phoneField.getText(), 
                    emailField.getText(),
                    model.CustomerType.valueOf(customerTypeCombo.getValue()),
                    currentUser.getUserId()
                );

                statusLabel.setText(String.format(
                    "Customer created successfully! Customer ID: %s, User ID: %s", 
                    customerId, userId
                ));
                statusLabel.setStyle("-fx-text-fill: #28a745;");

                // Clear form
                clearFormFields(usernameField, passwordField, firstNameField, 
                              surnameField, addressField, phoneField, emailField);

            } catch (Exception ex) {
                statusLabel.setText("Error: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: #dc3545;");
            }
        });

        tabContent.getChildren().addAll(titleLabel, formGrid, createCustomerButton, statusLabel);
        return tabContent;
    }

    private VBox createAccountManagementTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(20));

        Label titleLabel = new Label("Account Management");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Form fields for opening account
        VBox formBox = new VBox(10);
        TextField customerIdField = new TextField();
        customerIdField.setPromptText("Enter Customer ID");
        
        ComboBox<String> accountTypeCombo = new ComboBox<>();
        accountTypeCombo.getItems().addAll("SAVINGS", "CHEQUE", "INVESTMENT");
        accountTypeCombo.setValue("SAVINGS");
        
        TextField initialDepositField = new TextField();
        initialDepositField.setPromptText("Enter initial deposit amount");

        Button openAccountButton = new Button("Open Account");
        openAccountButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");

        // Accounts table for viewing and closing accounts
        Label accountsLabel = new Label("Existing Accounts");
        accountsLabel.setStyle("-fx-font-weight: bold;");
        
        TableView<Account> accountsTable = new TableView<>();
        
        TableColumn<Account, String> accNumberCol = new TableColumn<>("Account Number");
        accNumberCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));
        
        TableColumn<Account, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Account, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Account, String> param) {
                Account account = param.getValue();
                String accountType = account.getClass().getSimpleName();
                switch (accountType) {
                    case "SavingsAccount":
                        return new javafx.beans.property.SimpleStringProperty("SAVINGS ACCOUNT");
                    case "InvestmentAccount":
                        return new javafx.beans.property.SimpleStringProperty("INVESTMENT ACCOUNT");
                    case "ChequeAccount":
                        return new javafx.beans.property.SimpleStringProperty("CHEQUE ACCOUNT");
                    default:
                        return new javafx.beans.property.SimpleStringProperty(accountType);
                }
            }
        });
        
        TableColumn<Account, Double> balanceCol = new TableColumn<>("Balance");
        balanceCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
        
        TableColumn<Account, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        TableColumn<Account, String> customerCol = new TableColumn<>("Customer ID");
        customerCol.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Account, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Account, String> param) {
                Account account = param.getValue();
                return new javafx.beans.property.SimpleStringProperty(account.getCustomer().getCustomerId());
            }
        });
        
        accountsTable.getColumns().addAll(accNumberCol, typeCol, balanceCol, statusCol, customerCol);

        // Refresh button for accounts table
        Button refreshButton = new Button("Refresh Accounts");
        refreshButton.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");

        // Close account button
        Button closeAccountButton = new Button("Close Selected Account");
        closeAccountButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

        // Withdrawal button
        Button withdrawalButton = new Button("Process Withdrawal");
        withdrawalButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;");

        Label statusLabel = new Label();

        // Open account action
        openAccountButton.setOnAction(e -> {
            String customerId = customerIdField.getText().trim();
            String amountText = initialDepositField.getText().trim();

            if (customerId.isEmpty() || amountText.isEmpty()) {
                statusLabel.setText("Please enter both customer ID and initial deposit amount");
                statusLabel.setStyle("-fx-text-fill: #dc3545;");
                return;
            }

            try {
                double initialDeposit = Double.parseDouble(amountText);
                if (initialDeposit < 0) {
                    statusLabel.setText("Initial deposit cannot be negative");
                    statusLabel.setStyle("-fx-text-fill: #dc3545;");
                    return;
                }

                // Look up the actual customer from the database
                database.CustomerDAO customerDAO = new database.CustomerDAO();
                var customerOpt = customerDAO.findById(customerId);
                
                if (customerOpt.isEmpty()) {
                    statusLabel.setText("Customer not found with ID: " + customerId);
                    statusLabel.setStyle("-fx-text-fill: #dc3545;");
                    return;
                }

                var customer = customerOpt.get();
                var account = employeeController.openAccount(
                    customer,
                    model.AccountType.valueOf(accountTypeCombo.getValue()),
                    initialDeposit,
                    currentUser.getUserId()
                );

                statusLabel.setText("Account opened successfully: " + account.getAccountNumber());
                statusLabel.setStyle("-fx-text-fill: #28a745;");

                // Clear form and refresh table
                customerIdField.clear();
                initialDepositField.clear();
                refreshButton.fire(); // Refresh the accounts table

            } catch (NumberFormatException ex) {
                statusLabel.setText("Please enter a valid amount for initial deposit");
                statusLabel.setStyle("-fx-text-fill: #dc3545;");
            } catch (Exception ex) {
                statusLabel.setText("Error: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: #dc3545;");
                ex.printStackTrace();
            }
        });

        // Close account action
        closeAccountButton.setOnAction(e -> {
            Account selectedAccount = accountsTable.getSelectionModel().getSelectedItem();
            if (selectedAccount == null) {
                statusLabel.setText("Please select an account first");
                statusLabel.setStyle("-fx-text-fill: #dc3545;");
                return;
            }

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Close Account");
            confirmation.setHeaderText("Close Account: " + selectedAccount.getAccountNumber());
            confirmation.setContentText("Are you sure you want to close this account?");
            
            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        boolean success = employeeController.closeAccount(
                            selectedAccount.getAccountNumber(), currentUser.getUserId());
                        
                        if (success) {
                            statusLabel.setText("Account closed successfully");
                            statusLabel.setStyle("-fx-text-fill: #28a745;");
                            refreshButton.fire(); // Refresh the table
                        } else {
                            statusLabel.setText("Failed to close account");
                            statusLabel.setStyle("-fx-text-fill: #dc3545;");
                        }
                    } catch (Exception ex) {
                        statusLabel.setText("Error: " + ex.getMessage());
                        statusLabel.setStyle("-fx-text-fill: #dc3545;");
                    }
                }
            });
        });

        // Refresh accounts action
        refreshButton.setOnAction(e -> {
            try {
                // Use AccountDAO to load all accounts from database
                database.AccountDAO accountDAO = new database.AccountDAO();
                List<Account> accounts = accountDAO.findAll();
                
                ObservableList<Account> observableList = FXCollections.observableArrayList(accounts);
                accountsTable.setItems(observableList);

                statusLabel.setText("Loaded " + accounts.size() + " accounts");
                statusLabel.setStyle("-fx-text-fill: #28a745;");
                
                // Enhanced debug output
                System.out.println("=== ACCOUNTS DEBUG INFO ===");
                System.out.println("Refreshed accounts table with " + accounts.size() + " accounts:");
                for (Account account : accounts) {
                    System.out.println(" - " + account.getAccountNumber() + 
                        ": Balance $" + account.getBalance() + 
                        " (" + account.getStatus() + ")" +
                        " Customer: " + account.getCustomer().getCustomerId() +
                        " Type: " + account.getClass().getSimpleName());
                }
                System.out.println("=== END DEBUG INFO ===");
            } catch (Exception ex) {
                statusLabel.setText("Error loading accounts: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: #dc3545;");
                ex.printStackTrace();
            }
        });

        // Withdrawal action
        withdrawalButton.setOnAction(e -> {
            Account selectedAccount = accountsTable.getSelectionModel().getSelectedItem();
            if (selectedAccount == null) {
                statusLabel.setText("Please select an account first");
                statusLabel.setStyle("-fx-text-fill: #dc3545;");
                return;
            }

            // Check if selected account is a SavingsAccount - withdrawals not allowed
            if (selectedAccount instanceof model.SavingsAccount) {
                statusLabel.setText("Withdrawals are not permitted from savings accounts. Use transfers instead.");
                statusLabel.setStyle("-fx-text-fill: #dc3545;");
                return;
            }

            // Create withdrawal dialog
            TextInputDialog dialog = new TextInputDialog("0.00");
            dialog.setTitle("Process Withdrawal");
            dialog.setHeaderText("Withdrawal from Account: " + selectedAccount.getAccountNumber());
            dialog.setContentText("Enter withdrawal amount:");

            dialog.showAndWait().ifPresent(amountText -> {
                try {
                    double amount = Double.parseDouble(amountText);
                    if (amount <= 0) {
                        statusLabel.setText("Withdrawal amount must be positive");
                        statusLabel.setStyle("-fx-text-fill: #dc3545;");
                        return;
                    }

                    if (amount > selectedAccount.getBalance()) {
                        statusLabel.setText("Insufficient funds for withdrawal");
                        statusLabel.setStyle("-fx-text-fill: #dc3545;");
                        return;
                    }

                    // Process withdrawal using TransactionController
                    transactionController.processWithdrawal(selectedAccount, amount);

                    statusLabel.setText(String.format("Withdrawal of $%.2f processed successfully", amount));
                    statusLabel.setStyle("-fx-text-fill: #28a745;");

                    // Refresh the table to show updated balance
                    refreshButton.fire();

                } catch (NumberFormatException ex) {
                    statusLabel.setText("Please enter a valid amount");
                    statusLabel.setStyle("-fx-text-fill: #dc3545;");
                } catch (Exception ex) {
                    statusLabel.setText("Withdrawal failed: " + ex.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #dc3545;");
                }
            });
        });

        // Button container
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(openAccountButton, closeAccountButton, withdrawalButton, refreshButton);

        // Add all components to form
        formBox.getChildren().addAll(
            new Label("Customer ID:"), customerIdField,
            new Label("Account Type:"), accountTypeCombo,
            new Label("Initial Deposit:"), initialDepositField,
            buttonBox,
            accountsLabel,
            accountsTable,
            statusLabel
        );

        // Load initial data
        refreshButton.fire();

        tabContent.getChildren().addAll(titleLabel, formBox);
        return tabContent;
    }

    private VBox createDepositProcessingTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(20));

        Label titleLabel = new Label("Process Deposit");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Form fields
        VBox formBox = new VBox(10);
        TextField accountNumberField = new TextField();
        accountNumberField.setPromptText("Enter Account Number");
        
        TextField amountField = new TextField();
        amountField.setPromptText("Enter deposit amount");

        Button processDepositButton = new Button("Process Deposit");
        processDepositButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");

        Label statusLabel = new Label();

        processDepositButton.setOnAction(e -> {
            String accountNumber = accountNumberField.getText().trim();
            String amountText = amountField.getText().trim();

            // Step 2: Validate input
            if (accountNumber.isEmpty() || amountText.isEmpty()) {
                statusLabel.setText("Please enter both account number and amount");
                statusLabel.setStyle("-fx-text-fill: #dc3545;");
                return;
            }

            try {
                double amount = Double.parseDouble(amountText);
                
                if (amount <= 0) {
                    statusLabel.setText("Amount must be positive");
                    statusLabel.setStyle("-fx-text-fill: #dc3545;");
                    return;
                }

                // Step 3: Process deposit
                var result = employeeController.processDepositForCustomer(
                    accountNumber, amount, currentUser.getUserId()
                );

                if (result.isSuccess()) {
                    statusLabel.setText(String.format(
                        "Deposit successful! New balance: $%.2f, Transaction: %s",
                        result.getNewBalance(), result.getTransactionId()
                    ));
                    statusLabel.setStyle("-fx-text-fill: #28a745;");
                    
                    // Clear form
                    accountNumberField.clear();
                    amountField.clear();
                } else {
                    statusLabel.setText("Deposit failed: " + result.getMessage());
                    statusLabel.setStyle("-fx-text-fill: #dc3545;");
                }

            } catch (NumberFormatException ex) {
                statusLabel.setText("Please enter a valid amount");
                statusLabel.setStyle("-fx-text-fill: #dc3545;");
            } catch (Exception ex) {
                statusLabel.setText("Error: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: #dc3545;");
            }
        });

        formBox.getChildren().addAll(
            new Label("Account Number:"), accountNumberField,
            new Label("Amount:"), amountField,
            processDepositButton, statusLabel
        );

        tabContent.getChildren().addAll(titleLabel, formBox);
        return tabContent;
    }

    private void clearFormFields(TextField... fields) {
        for (TextField field : fields) {
            field.clear();
        }
    }

    private void logout() {
        stage.close();
        LoginView loginView = new LoginView();
        loginView.show();
    }

    public void show() {
        stage.show();
    }
}
