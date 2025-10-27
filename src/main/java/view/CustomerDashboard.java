// CustomerDashboard.java
package view;

import controller.TransactionController;
import database.AccountDAO;
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
    private TransactionController transactionController;

    public CustomerDashboard(User user) {
        this.stage = new Stage();
        this.currentUser = user;
        this.customerDAO = new CustomerDAO();
        this.accountDAO = new AccountDAO();
        this.transactionDAO = new TransactionDAO();
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

        tabPane.getTabs().addAll(accountsTab, transactionsTab, transferTab);

        // Main layout
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(15));
        
        // Header
        Label headerLabel = new Label("Customer Dashboard");
        headerLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // User info
        Label userInfoLabel = new Label("Welcome, " + currentUser.getUsername());
        
        // Logout button
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
        logoutButton.setOnAction(e -> logout());

        HBox headerBox = new HBox(20);
        headerBox.getChildren().addAll(headerLabel, userInfoLabel, logoutButton);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        mainLayout.getChildren().addAll(headerBox, tabPane);

        Scene scene = new Scene(mainLayout, 900, 600);
        stage.setScene(scene);
    }

    private VBox createAccountsTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(20));

        Label titleLabel = new Label("My Accounts");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Refresh button
        Button refreshButton = new Button("Refresh Accounts");
        refreshButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");

        // Withdrawal button
        Button withdrawalButton = new Button("Withdraw Funds");
        withdrawalButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;");

        // Accounts table
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
                
                statusLabel.setText("Loaded " + accounts.size() + " accounts for " + currentCustomer.getFirstName() + " " + currentCustomer.getSurname());
                statusLabel.setStyle("-fx-text-fill: #28a745;");
            } catch (Exception ex) {
                statusLabel.setText("Error loading accounts: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: #dc3545;");
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

            // Check if it's a savings account - withdrawals not allowed
            if (selectedAccount instanceof model.SavingsAccount) {
                statusLabel.setText("Withdrawals not allowed from Savings accounts. Please use Transfer Funds instead.");
                statusLabel.setStyle("-fx-text-fill: #dc3545;");
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

        // Load initial data
        refreshButton.fire();

        tabContent.getChildren().addAll(titleLabel, refreshButton, withdrawalButton, accountsTable, statusLabel);
        return tabContent;
    }

    private VBox createTransactionsTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(20));

        Label titleLabel = new Label("Transaction History");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Refresh button
        Button refreshButton = new Button("Refresh Transactions");
        refreshButton.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");

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
                List<Transaction> transactions = transactionDAO.findAll();
                ObservableList<Transaction> observableList = FXCollections.observableArrayList(transactions);
                transactionsTable.setItems(observableList);
                
                statusLabel.setText("Loaded " + transactions.size() + " transactions");
                statusLabel.setStyle("-fx-text-fill: #28a745;");
            } catch (Exception ex) {
                statusLabel.setText("Error loading transactions: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: #dc3545;");
            }
        });

        // Load initial data
        refreshButton.fire();

        tabContent.getChildren().addAll(titleLabel, refreshButton, transactionsTable, statusLabel);
        return tabContent;
    }

    private VBox createTransferTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(20));

        Label titleLabel = new Label("Transfer Funds");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Transfer form
        VBox formBox = new VBox(10);
        formBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20; -fx-border-color: #dee2e6; -fx-border-radius: 5px;");

        TextField fromAccountField = new TextField();
        fromAccountField.setPromptText("From Account Number");
        
        TextField toAccountField = new TextField();
        toAccountField.setPromptText("To Account Number");
        
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        Button transferButton = new Button("Transfer Funds");
        transferButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-pref-height: 40px;");

        Label statusLabel = new Label();

        transferButton.setOnAction(e -> {
            String fromAccountNumber = fromAccountField.getText().trim();
            String toAccountNumber = toAccountField.getText().trim();
            String amountText = amountField.getText().trim();

            if (fromAccountNumber.isEmpty() || toAccountNumber.isEmpty() || amountText.isEmpty()) {
                statusLabel.setText("Please fill in all fields");
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

                // Validate that the from account belongs to the current customer
                List<Account> customerAccounts = accountDAO.findByCustomerId(currentCustomer.getCustomerId());
                Account fromAccount = null;
                for (Account account : customerAccounts) {
                    if (account.getAccountNumber().equals(fromAccountNumber)) {
                        fromAccount = account;
                        break;
                    }
                }

                if (fromAccount == null) {
                    statusLabel.setText("From account does not belong to you or does not exist");
                    statusLabel.setStyle("-fx-text-fill: #dc3545;");
                    return;
                }

                // Check if from account has sufficient balance
                if (fromAccount.getBalance() < amount) {
                    statusLabel.setText("Insufficient funds in source account");
                    statusLabel.setStyle("-fx-text-fill: #dc3545;");
                    return;
                }

                // Find the destination account
                Optional<Account> toAccountOpt = accountDAO.findById(toAccountNumber);
                if (toAccountOpt.isEmpty()) {
                    statusLabel.setText("Destination account does not exist");
                    statusLabel.setStyle("-fx-text-fill: #dc3545;");
                    return;
                }

                Account toAccount = toAccountOpt.get();

                // Perform the transfer
                transactionController.transferFunds(fromAccount, toAccount, amount);

                statusLabel.setText(String.format("Transfer of $%.2f from %s to %s completed successfully", 
                    amount, fromAccountNumber, toAccountNumber));
                statusLabel.setStyle("-fx-text-fill: #28a745;");

                // Clear form
                fromAccountField.clear();
                toAccountField.clear();
                amountField.clear();

            } catch (NumberFormatException ex) {
                statusLabel.setText("Please enter a valid amount");
                statusLabel.setStyle("-fx-text-fill: #dc3545;");
            } catch (Exception ex) {
                statusLabel.setText("Transfer failed: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: #dc3545;");
            }
        });

        formBox.getChildren().addAll(
            new Label("From Account:"), fromAccountField,
            new Label("To Account:"), toAccountField,
            new Label("Amount:"), amountField,
            transferButton, statusLabel
        );

        tabContent.getChildren().addAll(titleLabel, formBox);
        return tabContent;
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