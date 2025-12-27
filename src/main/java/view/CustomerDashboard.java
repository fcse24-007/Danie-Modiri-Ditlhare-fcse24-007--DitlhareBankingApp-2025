package view;

import controller.TransactionController;
import database.AccountDAO;
import database.AuditDAO;
import database.CustomerDAO;
import database.TransactionDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.beans.value.ObservableValue;
import model.Account;
import model.Customer;
import model.Transaction;
import model.User;
import model.UserRole;

import java.util.List;
import java.util.Optional;

public class CustomerDashboard {
    private Stage stage;
    private User currentUser;
    private Customer currentCustomer;
    private AccountDAO accountDAO;
    private CustomerDAO customerDAO;
    private TransactionDAO transactionDAO;
    private AuditDAO auditDAO;
    private TransactionController transactionController;

    public CustomerDashboard(User user) {
        this.stage = new Stage();
        this.currentUser = user;
        this.customerDAO = new CustomerDAO();
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();
        this.auditDAO = new AuditDAO();
        this.transactionController = new TransactionController();

        // Find the customer record for this user
        Optional<Customer> customerOpt = customerDAO.findByUserId(user.getUserId());
        if (customerOpt.isPresent()) {
            this.currentCustomer = customerOpt.get();
        } else {
            throw new RuntimeException("Customer record not found for user: " + user.getUsername());
        }
        initializeUI();
    }

    private void initializeUI() {
        stage.setTitle("Customer Dashboard - Banking System");

        // Create tab pane for different functionalities
        TabPane tabPane = new TabPane();

        // Accounts Tab
        Tab accountsTab = new Tab("My Accounts");
        accountsTab.setContent(createAccountsTab());
        accountsTab.setClosable(false);

        // Transactions Tab
        Tab transactionsTab = new Tab("Transaction History");
        transactionsTab.setContent(createTransactionsTab());
        transactionsTab.setClosable(false);

        // Transfer Tab
        Tab transferTab = new Tab("Transfer Funds");
        transferTab.setContent(createTransferTab());
        transferTab.setClosable(false);

        // New Profile Tab
        Tab profileTab = new Tab("Profile");
        profileTab.setContent(createProfileTab());
        profileTab.setClosable(false);
        // Add Delete Profile Tab
        Tab deleteProfileTab = new Tab("Delete Profile");
        deleteProfileTab.setContent(createDeleteProfileTab());
        deleteProfileTab.setClosable(false);

        tabPane.getTabs().addAll(accountsTab, transactionsTab, transferTab, profileTab, deleteProfileTab);

        // Main layout
        VBox mainLayout = new VBox(10);
        mainLayout.getStyleClass().add("root");
        mainLayout.getStyleClass().add("main-container");

        // Header
        Label headerLabel = new Label("Customer Dashboard");
        headerLabel.getStyleClass().add("header-label");

        // User info
        Label userInfoLabel = new Label("Welcome, " + currentUser.getUsername());
        userInfoLabel.getStyleClass().add("sub-header-label");

        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().addAll("button", "button-danger");
        logoutButton.setOnAction(e -> logout());

        HBox headerBox = new HBox(20);
        headerBox.getChildren().addAll(headerLabel, userInfoLabel, logoutButton);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        mainLayout.getChildren().addAll(headerBox, tabPane);

        // Wrap main layout in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Scene scene = new Scene(scrollPane, 900, 600);

        // Load CSS
        try {
            String cssPath = getClass().getResource("/styles/styles.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Error loading CSS: " + e.getMessage());
        }

        stage.setScene(scene);
    }

    private VBox createAccountsTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(20));

        Label titleLabel = new Label("My Accounts");
        titleLabel.getStyleClass().add("sub-header-label");

        // Refresh button
        Button refreshButton = new Button("Refresh Accounts");
        refreshButton.getStyleClass().addAll("button", "button-secondary");

        // Withdrawal button
        Button withdrawalButton = new Button("Withdraw Funds");
        withdrawalButton.getStyleClass().addAll("button", "button-warning");

        // Accounts table
        TableView<Account> accountsTable = new TableView<>();

        TableColumn<Account, String> accNumberCol = new TableColumn<>("Account Number");
        accNumberCol.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));

        TableColumn<Account, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Account, String>, ObservableValue<String>>() {
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

        TableColumn<Account, String> balanceCol = new TableColumn<>("Balance");
        balanceCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<Account, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(TableColumn.CellDataFeatures<Account, String> param) {
                        Account account = param.getValue();
                        return new javafx.beans.property.SimpleStringProperty(
                                String.format("P%.2f", account.getBalance()));
                    }
                });

        TableColumn<Account, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        accountsTable.getColumns().addAll(accNumberCol, typeCol, balanceCol, statusCol);

        // Status label
        Label statusLabel = new Label();

        // Load accounts
        refreshButton.setOnAction(e -> {
            try {
                // Load only the current customer's accounts
                List<Account> accounts = accountDAO.findByCustomerId(currentCustomer.getCustomerId());
                ObservableList<Account> observableList = FXCollections.observableArrayList(accounts);
                accountsTable.setItems(observableList);

                statusLabel.setText("Loaded " + accounts.size() + " accounts for " + currentCustomer.getFirstName()
                        + " " + currentCustomer.getSurname());
                statusLabel.getStyleClass().removeAll("status-label-error");
                statusLabel.getStyleClass().add("status-label-success");
            } catch (Exception ex) {
                statusLabel.setText("Error loading accounts: " + ex.getMessage());
                statusLabel.getStyleClass().removeAll("status-label-success");
                statusLabel.getStyleClass().add("status-label-error");
            }
        });

        // Withdrawal action
        withdrawalButton.setOnAction(e -> {
            Account selectedAccount = accountsTable.getSelectionModel().getSelectedItem();
            if (selectedAccount == null) {
                statusLabel.setText("Please select an account first");
                statusLabel.getStyleClass().removeAll("status-label-success");
                statusLabel.getStyleClass().add("status-label-error");
                return;
            }

            // Check if it's a savings account - withdrawals not allowed
            if (selectedAccount instanceof model.SavingsAccount) {
                statusLabel
                        .setText("Withdrawals not allowed from Savings accounts. Please use Transfer Funds instead.");
                statusLabel.getStyleClass().removeAll("status-label-success");
                statusLabel.getStyleClass().add("status-label-error");
                return;
            }

            // Create withdrawal dialog
            TextInputDialog dialog = new TextInputDialog("0.00");
            dialog.setTitle("Withdraw Funds");
            dialog.setHeaderText("Withdrawal from Account: " + selectedAccount.getAccountNumber());
            dialog.setContentText("Enter withdrawal amount:");

            dialog.showAndWait().ifPresent(amountText -> {
                try {
                    double amount = Double.parseDouble(amountText);
                    if (amount <= 0) {
                        statusLabel.setText("Withdrawal amount must be positive");
                        statusLabel.getStyleClass().removeAll("status-label-success");
                        statusLabel.getStyleClass().add("status-label-error");
                        return;
                    }

                    if (amount > selectedAccount.getBalance()) {
                        statusLabel.setText("Insufficient funds for withdrawal");
                        statusLabel.getStyleClass().removeAll("status-label-success");
                        statusLabel.getStyleClass().add("status-label-error");
                        return;
                    }

                    // Process withdrawal using TransactionController
                    transactionController.processWithdrawal(selectedAccount, amount);

                    statusLabel.setText(String.format("Withdrawal of P%.2f processed successfully", amount));
                    statusLabel.getStyleClass().removeAll("status-label-error");
                    statusLabel.getStyleClass().add("status-label-success");

                    // Refresh the table to show updated balance
                    refreshButton.fire();

                } catch (NumberFormatException ex) {
                    statusLabel.setText("Please enter a valid amount");
                    statusLabel.getStyleClass().removeAll("status-label-success");
                    statusLabel.getStyleClass().add("status-label-error");
                } catch (Exception ex) {
                    statusLabel.setText("Withdrawal failed: " + ex.getMessage());
                    statusLabel.getStyleClass().removeAll("status-label-success");
                    statusLabel.getStyleClass().add("status-label-error");
                }
            });
        });

        // Load initial data
        refreshButton.fire();

        tabContent.getChildren().addAll(titleLabel, refreshButton, withdrawalButton, accountsTable, statusLabel);
        return tabContent;
    }

    private VBox createTransactionsTab() {
        // Section: Transaction History
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(20));

        Label titleLabel = new Label("Transaction History");
        titleLabel.getStyleClass().add("sub-header-label");

        // Refresh button
        Button refreshButton = new Button("Refresh Transactions");
        refreshButton.getStyleClass().addAll("button", "button-secondary");

        // Transactions table
        TableView<Transaction> transactionsTable = new TableView<>();

        TableColumn<Transaction, String> transIdCol = new TableColumn<>("Transaction ID");
        transIdCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("transactionType"));

        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("timeStamp"));

        TableColumn<Transaction, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        transactionsTable.getColumns().addAll(transIdCol, typeCol, amountCol, dateCol, descCol);

        // Status label
        Label statusLabel = new Label();

        // Load transactions
        refreshButton.setOnAction(e -> {
            try {
                // Load only transactions for current customer's accounts
                List<Account> customerAccounts = accountDAO.findByCustomerId(currentCustomer.getCustomerId());
                List<String> accountNumbers = customerAccounts.stream()
                        .map(Account::getAccountNumber)
                        .collect(java.util.stream.Collectors.toList());

                List<Transaction> allTransactions = transactionDAO.findAll();
                List<Transaction> transactions = allTransactions.stream()
                        .filter(t -> accountNumbers.contains(t.getAccount().getAccountNumber()))
                        .collect(java.util.stream.Collectors.toList());
                ObservableList<Transaction> observableList = FXCollections.observableArrayList(transactions);
                transactionsTable.setItems(observableList);

                statusLabel.setText("Loaded " + transactions.size() + " transactions");
                statusLabel.getStyleClass().removeAll("status-label-error");
                statusLabel.getStyleClass().add("status-label-success");
            } catch (Exception ex) {
                statusLabel.setText("Error loading transactions: " + ex.getMessage());
                statusLabel.getStyleClass().removeAll("status-label-success");
                statusLabel.getStyleClass().add("status-label-error");
            }
        });

        // Load initial data
        refreshButton.fire();

        tabContent.getChildren().addAll(titleLabel, refreshButton, transactionsTable, statusLabel);
        return tabContent;
    }

    private VBox createProfileTab() {
        VBox profileContent = new VBox(15);
        profileContent.setPadding(new Insets(20));
        profileContent.getStyleClass().add("card");

        Label titleLabel = new Label("My Profile");
        titleLabel.getStyleClass().add("sub-header-label");

        // Form fields for profile
        GridPane profileGrid = new GridPane();
        profileGrid.setHgap(15);
        profileGrid.setVgap(10);
        profileGrid.setPadding(new Insets(10));

        // Editable fields except password
        // Password field is intentionally omitted here to prevent customers from
        // editing their password.
        // Password changes are restricted and should be handled through other secure
        // mechanisms if needed.
        TextField firstNameField = new TextField(currentCustomer.getFirstName());
        TextField surnameField = new TextField(currentCustomer.getSurname());
        TextField addressField = new TextField(currentCustomer.getAddress());
        TextField phoneField = new TextField(currentCustomer.getPhoneNumber());
        TextField emailField = new TextField(currentCustomer.getEmail());

        // Disable password editing: no password field in profile UI

        // Add form fields labels and inputs
        int row = 0;
        profileGrid.add(new Label("First Name:"), 0, row);
        profileGrid.add(firstNameField, 1, row++);
        profileGrid.add(new Label("Surname:"), 0, row);
        profileGrid.add(surnameField, 1, row++);
        profileGrid.add(new Label("Address:"), 0, row);
        profileGrid.add(addressField, 1, row++);
        profileGrid.add(new Label("Phone Number:"), 0, row);
        profileGrid.add(phoneField, 1, row++);
        profileGrid.add(new Label("Email:"), 0, row);
        profileGrid.add(emailField, 1, row++);

        // Status label for feedback
        Label statusLabel = new Label();

        // Update button
        Button updateButton = new Button("Update Profile");
        updateButton.getStyleClass().addAll("button", "button-primary");

        updateButton.setOnAction(e -> {
            try {
                // Validate inputs (simple validation here)
                String newFirstName = firstNameField.getText().trim();
                String newSurname = surnameField.getText().trim();
                String newAddress = addressField.getText().trim();
                String newPhone = phoneField.getText().trim();
                String newEmail = emailField.getText().trim();

                if (newFirstName.isEmpty() || newSurname.isEmpty() || newAddress.isEmpty() || newPhone.isEmpty()
                        || !newEmail.contains("@")) {
                    statusLabel.setText("Please fill all fields correctly with valid email.");
                    statusLabel.getStyleClass().removeAll("status-label-success");
                    statusLabel.getStyleClass().add("status-label-error");
                    return;
                }

                // Update currentCustomer model
                currentCustomer.updateProfile(newFirstName, newSurname, newAddress, newPhone, newEmail);

                // Persist update
                boolean success = customerDAO.update(currentCustomer);
                if (success) {
                    statusLabel.setText("Profile updated successfully.");
                    statusLabel.getStyleClass().removeAll("status-label-error");
                    statusLabel.getStyleClass().add("status-label-success");
                } else {
                    statusLabel.setText("Failed to update profile.");
                    statusLabel.getStyleClass().removeAll("status-label-success");
                    statusLabel.getStyleClass().add("status-label-error");
                }
            } catch (IllegalArgumentException ex) {
                statusLabel.setText("Validation error: " + ex.getMessage());
                statusLabel.getStyleClass().removeAll("status-label-success");
                statusLabel.getStyleClass().add("status-label-error");
            } catch (Exception ex) {
                statusLabel.setText("An error occurred: " + ex.getMessage());
                statusLabel.getStyleClass().removeAll("status-label-success");
                statusLabel.getStyleClass().add("status-label-error");
            }
        });

        profileContent.getChildren().addAll(titleLabel, profileGrid, updateButton, statusLabel);
        return profileContent;
    }

    private VBox createDeleteProfileTab() {
        VBox deleteContent = new VBox(15);
        deleteContent.setPadding(new Insets(20));
        deleteContent.getStyleClass().add("card");

        Label titleLabel = new Label("Delete My Profile");
        titleLabel.getStyleClass().add("sub-header-label");

        Label instructions = new Label("You can only delete your profile if all your bank accounts are closed.");

        Label statusLabel = new Label();

        Button deleteButton = new Button("Delete My Profile");
        deleteButton.getStyleClass().addAll("button", "button-danger");

        deleteButton.setOnAction(e -> {
            try {
                // Check accounts - must all be closed (balance 0, status CLOSED)
                List<Account> accounts = accountDAO.findByCustomerId(currentCustomer.getCustomerId());
                boolean allClosed = accounts.stream()
                        .allMatch(a -> a.getBalance() == 0 && "CLOSED".equalsIgnoreCase(a.getStatus().toString()));

                if (!allClosed) {
                    statusLabel.setText(
                            "Cannot delete profile. All bank accounts must be closed before deleting profile.");
                    statusLabel.getStyleClass().removeAll("status-label-success");
                    statusLabel.getStyleClass().add("status-label-error");
                    return;
                }

                // Confirm deletion
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirm Profile Deletion");
                confirmAlert.setHeaderText("Are you sure you want to delete your profile?");
                confirmAlert.setContentText("This action is irreversible.");

                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        boolean success = customerDAO.delete(currentCustomer.getCustomerId());
                        if (success) {
                            // Record audit for profile deletion
                            auditDAO.recordAudit(currentCustomer.getUserId(), "CUSTOMER_DELETED",
                                    "Customer deleted their own profile: " + currentCustomer.getCustomerId());
                            statusLabel.setText("Profile deleted successfully. Logging out...");
                            statusLabel.getStyleClass().removeAll("status-label-error");
                            statusLabel.getStyleClass().add("status-label-success");
                            logout();
                        } else {
                            statusLabel.setText("Failed to delete profile.");
                            statusLabel.getStyleClass().removeAll("status-label-success");
                            statusLabel.getStyleClass().add("status-label-error");
                        }
                    }
                });
            } catch (Exception ex) {
                statusLabel.setText("An error occurred: " + ex.getMessage());
                statusLabel.getStyleClass().removeAll("status-label-success");
                statusLabel.getStyleClass().add("status-label-error");
            }
        });

        deleteContent.getChildren().addAll(titleLabel, instructions, deleteButton, statusLabel);
        return deleteContent;
    }

    private void logout() {
        stage.close();
        LoginView loginView = new LoginView();
        loginView.show();
    }

    public void show() {
        stage.show();
    }

    private VBox createTransferTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(20));

        Label titleLabel = new Label("Transfer Funds");
        titleLabel.getStyleClass().add("sub-header-label");

        // Source account selection
        Label sourceLabel = new Label("Select Source Account:");
        ComboBox<Account> sourceAccountCombo = new ComboBox<>();
        sourceAccountCombo.setMinWidth(300);

        // Add StringConverter and cell factory to restrict item display to account type
        // and number
        javafx.util.StringConverter<Account> accountStringConverter = new javafx.util.StringConverter<Account>() {
            @Override
            public String toString(Account account) {
                if (account == null) {
                    return "";
                }
                String accountType = "";
                String className = account.getClass().getSimpleName();
                switch (className) {
                    case "SavingsAccount":
                        accountType = "SAVINGS ACCOUNT";
                        break;
                    case "InvestmentAccount":
                        accountType = "INVESTMENT ACCOUNT";
                        break;
                    case "ChequeAccount":
                        accountType = "CHEQUE ACCOUNT";
                        break;
                    default:
                        accountType = className.toUpperCase();
                }
                return accountType + " - " + account.getAccountNumber();
            }

            @Override
            public Account fromString(String string) {
                // Not needed for ComboBox usage here
                return null;
            }
        };

        sourceAccountCombo.setConverter(accountStringConverter);
        sourceAccountCombo.setCellFactory(lv -> new ListCell<Account>() {
            @Override
            protected void updateItem(Account account, boolean empty) {
                super.updateItem(account, empty);
                if (empty || account == null) {
                    setText(null);
                } else {
                    String accountType = "";
                    String className = account.getClass().getSimpleName();
                    switch (className) {
                        case "SavingsAccount":
                            accountType = "SAVINGS ACCOUNT";
                            break;
                        case "InvestmentAccount":
                            accountType = "INVESTMENT ACCOUNT";
                            break;
                        case "ChequeAccount":
                            accountType = "CHEQUE ACCOUNT";
                            break;
                        default:
                            accountType = className.toUpperCase();
                    }
                    setText(accountType + " - " + account.getAccountNumber());
                }
            }
        });

        // Load current customer's accounts for source selection
        try {
            List<Account> accounts = accountDAO.findByCustomerId(currentCustomer.getCustomerId());
            ObservableList<Account> accountOptions = FXCollections.observableArrayList(accounts);
            sourceAccountCombo.setItems(accountOptions);
        } catch (Exception e) {
            // If error occurs, disable combo box and show message
            sourceAccountCombo.setDisable(true);
            tabContent.getChildren().add(new Label("Error loading accounts for transfer: " + e.getMessage()));
        }

        // Target account number entry
        Label targetLabel = new Label("Enter Target Account Number:");
        TextField targetAccountField = new TextField();
        targetAccountField.setPromptText("Account Number");

        // Transfer amount entry
        Label amountLabel = new Label("Enter Amount to Transfer:");
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        // Status label
        Label statusLabel = new Label();

        // Transfer button
        Button transferButton = new Button("Transfer");
        transferButton.getStyleClass().addAll("button", "button-primary");

        transferButton.setOnAction(e -> {
            Account sourceAccount = sourceAccountCombo.getValue();
            String targetAccountNumber = targetAccountField.getText().trim();
            String amountText = amountField.getText().trim();

            if (sourceAccount == null) {
                statusLabel.setText("Please select a source account.");
                statusLabel.getStyleClass().removeAll("status-label-success");
                statusLabel.getStyleClass().add("status-label-error");
                return;
            }

            if (targetAccountNumber.isEmpty()) {
                statusLabel.setText("Please enter target account number.");
                statusLabel.getStyleClass().removeAll("status-label-success");
                statusLabel.getStyleClass().add("status-label-error");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    statusLabel.setText("Please enter a positive amount.");
                    statusLabel.getStyleClass().removeAll("status-label-success");
                    statusLabel.getStyleClass().add("status-label-error");
                    return;
                }
            } catch (NumberFormatException ex) {
                statusLabel.setText("Invalid amount format.");
                statusLabel.getStyleClass().removeAll("status-label-success");
                statusLabel.getStyleClass().add("status-label-error");
                return;
            }

            if (amount > sourceAccount.getBalance()) {
                statusLabel.setText("Insufficient funds in source account.");
                statusLabel.getStyleClass().removeAll("status-label-success");
                statusLabel.getStyleClass().add("status-label-error");
                return;
            }

            try {
                // Find target account by account number
                Optional<Account> targetAccountOpt = accountDAO.findById(targetAccountNumber);
                if (!targetAccountOpt.isPresent()) {
                    statusLabel.setText("Target account not found.");
                    statusLabel.getStyleClass().removeAll("status-label-success");
                    statusLabel.getStyleClass().add("status-label-error");
                    return;
                }
                Account targetAccount = targetAccountOpt.get();
                // Use transactionController to process transfer
                transactionController.transferFunds(sourceAccount, targetAccount, amount);
                statusLabel.setText(
                        String.format("Transfer of P%.2f to account %s successful.", amount, targetAccountNumber));
                statusLabel.getStyleClass().removeAll("status-label-error");
                statusLabel.getStyleClass().add("status-label-success");
                // Optionally refresh accounts table if needed
            } catch (Exception ex) {
                statusLabel.setText("Error processing transfer: " + ex.getMessage());
                statusLabel.getStyleClass().removeAll("status-label-success");
                statusLabel.getStyleClass().add("status-label-error");
            }
        });

        tabContent.getChildren().addAll(
                titleLabel,
                sourceLabel,
                sourceAccountCombo,
                targetLabel,
                targetAccountField,
                amountLabel,
                amountField,
                transferButton,
                statusLabel);

        return tabContent;
    }
}