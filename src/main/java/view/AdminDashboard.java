// AdminDashboard.java
package view;

import database.AuditDAO;
import database.UserDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import model.AuditEntry;
import model.User;
import model.UserRole;
import service.IDGeneratorService;

import java.util.List;
import java.io.File;

public class AdminDashboard {
    private Stage stage;
    private User currentUser;
    private AuditDAO auditDAO;
    private UserDAO userDAO;
    private TableView<User> userTableView;

    public AdminDashboard(User user) {
        this.stage = new Stage();
        this.currentUser = user;
        this.auditDAO = new AuditDAO();
        this.userDAO = new UserDAO();
        initializeUI();
    }

    private void initializeUI() {
        stage.setTitle("Administrator Dashboard - Banking System");

        // Create tab pane for different functionalities
        TabPane tabPane = new TabPane();

        // User Management Tab
        Tab userTab = new Tab("Manage Users");
        userTab.setContent(createUserManagementTab());
        userTab.setClosable(false);

        // Audit Trail Tab
        Tab auditTab = new Tab("Audit Trail");
        auditTab.setContent(createAuditTrailTab());
        auditTab.setClosable(false);

        // System Tab
        Tab systemTab = new Tab("System Info");
        systemTab.setContent(createSystemInfoTab());
        systemTab.setClosable(false);

        tabPane.getTabs().addAll(userTab, auditTab, systemTab);

        // Main layout
        VBox mainLayout = new VBox(10);
        mainLayout.getStyleClass().add("root");
        mainLayout.getStyleClass().add("main-container");

        // Header
        Label headerLabel = new Label("Administrator Dashboard");
        headerLabel.getStyleClass().add("header-label");

        // User info
        Label userInfoLabel = new Label("Logged in as: " + currentUser.getUsername() + " (Administrator)");
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
        scrollPane.setFitToHeight(false); // Allow height to fit if possible, but scroll if needed
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;"); // Ensure it blends in

        Scene scene = new Scene(scrollPane, 1000, 700);

        // Load CSS
        try {
            String cssPath = getClass().getResource("/styles/styles.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Error loading CSS: " + e.getMessage());
        }

        stage.setScene(scene);
    }

    private VBox createUserManagementTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(20));

        Label titleLabel = new Label("User Management");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Create User Section
        VBox createUserSection = new VBox(10);
        createUserSection.getStyleClass().add("card");

        Label createUserLabel = new Label("Create New User");
        createUserLabel.getStyleClass().add("sub-header-label");

        // Create User Form
        GridPane createUserForm = new GridPane();
        createUserForm.setHgap(10);
        createUserForm.setVgap(10);
        createUserForm.setPadding(new Insets(10));

        TextField newUsernameField = new TextField();
        newUsernameField.setPromptText("Enter Username");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter Password");

        ComboBox<UserRole> newRoleCombo = new ComboBox<>();
        newRoleCombo.getItems().addAll(UserRole.values());
        newRoleCombo.setValue(UserRole.CUSTOMER);

        Button createUserButton = new Button("Create User");
        createUserButton.getStyleClass().addAll("button", "button-success");

        // Add form fields
        int row = 0;
        createUserForm.add(new Label("Username:"), 0, row);
        createUserForm.add(newUsernameField, 1, row++);
        createUserForm.add(new Label("Password:"), 0, row);
        createUserForm.add(newPasswordField, 1, row++);
        createUserForm.add(new Label("Role:"), 0, row);
        createUserForm.add(newRoleCombo, 1, row++);
        createUserForm.add(createUserButton, 1, row++);

        Label createUserStatusLabel = new Label();

        // Create User Button Action
        createUserButton.setOnAction(e -> {
            String username = newUsernameField.getText().trim();
            String password = newPasswordField.getText();
            UserRole role = newRoleCombo.getValue();

            if (username.isEmpty() || password.isEmpty()) {
                createUserStatusLabel.setText("Please fill in username and password");
                createUserStatusLabel.getStyleClass().removeAll("status-label-success");
                createUserStatusLabel.getStyleClass().add("status-label-error");
                return;
            }

            try {
                // Auto-generate user ID based on role
                IDGeneratorService idGenerator = new IDGeneratorService();
                String userId = idGenerator.generateUserId(role);

                // Create new user with auto-generated ID
                User newUser = new User(userId, username, password, role);

                // Save to database
                if (userDAO.save(newUser)) {
                    createUserStatusLabel.setText(String.format(
                            "User created successfully! ID: %s, Username: %s, Role: %s",
                            userId, username, role));
                    createUserStatusLabel.getStyleClass().removeAll("status-label-error");
                    createUserStatusLabel.getStyleClass().add("status-label-success");

                    // Record audit entry
                    auditDAO.recordAudit(currentUser.getUserId(), "USER_CREATED",
                            String.format("Created user %s (%s) with role %s", username, userId, role));

                    // Clear form
                    newUsernameField.clear();
                    newPasswordField.clear();
                    newRoleCombo.setValue(UserRole.CUSTOMER);

                    // Refresh user table
                    refreshUserTable();
                } else {
                    createUserStatusLabel.setText("Failed to create user");
                    createUserStatusLabel.getStyleClass().removeAll("status-label-success");
                    createUserStatusLabel.getStyleClass().add("status-label-error");
                }
            } catch (Exception ex) {
                createUserStatusLabel.setText("Error creating user: " + ex.getMessage());
                createUserStatusLabel.getStyleClass().removeAll("status-label-success");
                createUserStatusLabel.getStyleClass().add("status-label-error");
            }
        });

        createUserSection.getChildren().addAll(createUserLabel, createUserForm, createUserStatusLabel);

        // User Table Section
        VBox userTableSection = new VBox(10);
        Label userTableLabel = new Label("Existing Users");
        userTableLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // User table
        userTableView = new TableView<>();

        TableColumn<User, String> userIdCol = new TableColumn<>("User ID");
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        userTableView.getColumns().addAll(userIdCol, usernameCol, roleCol);

        // User Management Buttons
        HBox userManagementButtons = new HBox(10);
        Button refreshButton = new Button("Refresh Users");
        Button changeRoleButton = new Button("Change Role");
        Button resetPasswordButton = new Button("Reset Password");
        Button deleteUserButton = new Button("Delete User");

        refreshButton.getStyleClass().addAll("button", "button-secondary");
        changeRoleButton.getStyleClass().addAll("button", "button-warning");
        resetPasswordButton.getStyleClass().addAll("button", "button-success");
        deleteUserButton.getStyleClass().addAll("button", "button-danger");

        userManagementButtons.getChildren().addAll(refreshButton, changeRoleButton, resetPasswordButton,
                deleteUserButton);

        // Status label for user management
        Label userManagementStatusLabel = new Label();

        // Load users
        refreshButton.setOnAction(e -> refreshUserTable());

        // Change role functionality
        changeRoleButton.setOnAction(e -> {
            User selectedUser = userTableView.getSelectionModel().getSelectedItem();
            if (selectedUser == null) {
                userManagementStatusLabel.setText("Please select a user first");
                userManagementStatusLabel.setStyle("-fx-text-fill: #dc3545;");
                return;
            }

            // Prevent admin from changing their own role
            if (selectedUser.getUserId().equals(currentUser.getUserId())) {
                userManagementStatusLabel.setText("Cannot change your own role");
                userManagementStatusLabel.getStyleClass().removeAll("status-label-success");
                userManagementStatusLabel.getStyleClass().add("status-label-error");
                return;
            }

            ChoiceDialog<UserRole> dialog = new ChoiceDialog<>(selectedUser.getRole(),
                    UserRole.CUSTOMER, UserRole.BANK_EMPLOYEE, UserRole.ADMINISTRATOR);
            dialog.setTitle("Change User Role");
            dialog.setHeaderText("Change role for: " + selectedUser.getUsername());
            dialog.setContentText("Select new role:");

            dialog.showAndWait().ifPresent(newRole -> {
                try {
                    selectedUser.setRole(newRole);
                    if (userDAO.update(selectedUser)) {
                        userManagementStatusLabel
                                .setText("Role changed successfully for " + selectedUser.getUsername());
                        userManagementStatusLabel.getStyleClass().removeAll("status-label-error");
                        userManagementStatusLabel.getStyleClass().add("status-label-success");

                        // Record audit entry
                        auditDAO.recordAudit(currentUser.getUserId(), "USER_ROLE_CHANGED",
                                String.format("Changed role for user %s to %s", selectedUser.getUsername(), newRole));

                        refreshUserTable();
                    }
                } catch (Exception ex) {
                    userManagementStatusLabel.setText("Error changing role: " + ex.getMessage());
                    userManagementStatusLabel.getStyleClass().removeAll("status-label-success");
                    userManagementStatusLabel.getStyleClass().add("status-label-error");
                }
            });
        });

        // Reset password functionality
        resetPasswordButton.setOnAction(e -> {
            User selectedUser = userTableView.getSelectionModel().getSelectedItem();
            if (selectedUser == null) {
                userManagementStatusLabel.setText("Please select a user first");
                userManagementStatusLabel.setStyle("-fx-text-fill: #dc3545;");
                return;
            }

            TextInputDialog dialog = new TextInputDialog("TempPassword123!");
            dialog.setTitle("Reset Password");
            dialog.setHeaderText("Reset password for: " + selectedUser.getUsername());
            dialog.setContentText("Enter new password:");

            dialog.showAndWait().ifPresent(newPassword -> {
                try {
                    selectedUser.setPassword(newPassword);
                    if (userDAO.update(selectedUser)) {
                        userManagementStatusLabel
                                .setText("Password reset successfully for " + selectedUser.getUsername());
                        userManagementStatusLabel.getStyleClass().removeAll("status-label-error");
                        userManagementStatusLabel.getStyleClass().add("status-label-success");

                        // Record audit entry
                        auditDAO.recordAudit(currentUser.getUserId(), "PASSWORD_RESET",
                                String.format("Reset password for user %s", selectedUser.getUsername()));
                    }
                } catch (Exception ex) {
                    userManagementStatusLabel.setText("Error resetting password: " + ex.getMessage());
                    userManagementStatusLabel.getStyleClass().removeAll("status-label-success");
                    userManagementStatusLabel.getStyleClass().add("status-label-error");
                }
            });
        });

        // Delete user functionality
        deleteUserButton.setOnAction(e -> {
            User selectedUser = userTableView.getSelectionModel().getSelectedItem();
            if (selectedUser == null) {
                userManagementStatusLabel.setText("Please select a user first");
                userManagementStatusLabel.setStyle("-fx-text-fill: #dc3545;");
                return;
            }

            // Prevent admin from deleting themselves
            if (selectedUser.getUserId().equals(currentUser.getUserId())) {
                userManagementStatusLabel.setText("Cannot delete your own account");
                userManagementStatusLabel.getStyleClass().removeAll("status-label-success");
                userManagementStatusLabel.getStyleClass().add("status-label-error");
                return;
            }

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Delete User");
            confirmation.setHeaderText("Delete User: " + selectedUser.getUsername());
            confirmation.setContentText("Are you sure you want to delete this user? This action cannot be undone.");

            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        if (userDAO.delete(selectedUser.getUserId())) {
                            userManagementStatusLabel
                                    .setText("User deleted successfully: " + selectedUser.getUsername());
                            userManagementStatusLabel.getStyleClass().removeAll("status-label-error");
                            userManagementStatusLabel.getStyleClass().add("status-label-success");

                            // Record audit entry
                            auditDAO.recordAudit(currentUser.getUserId(), "USER_DELETED",
                                    String.format("Deleted user %s", selectedUser.getUsername()));

                            refreshUserTable();
                        } else {
                            userManagementStatusLabel.setText("Failed to delete user");
                            userManagementStatusLabel.getStyleClass().removeAll("status-label-success");
                            userManagementStatusLabel.getStyleClass().add("status-label-error");
                        }
                    } catch (Exception ex) {
                        userManagementStatusLabel.setText("Error deleting user: " + ex.getMessage());
                        userManagementStatusLabel.getStyleClass().removeAll("status-label-success");
                        userManagementStatusLabel.getStyleClass().add("status-label-error");
                    }
                }
            });
        });

        userTableSection.getChildren().addAll(userTableLabel, userTableView, userManagementButtons,
                userManagementStatusLabel);

        // Add sections to tab content
        tabContent.getChildren().addAll(titleLabel, createUserSection, userTableSection);

        // Load initial data
        refreshUserTable();

        return tabContent;
    }

    private void refreshUserTable() {
        try {
            List<User> users = userDAO.findAll();
            ObservableList<User> observableList = FXCollections.observableArrayList(users);

            if (userTableView != null) {
                userTableView.setItems(observableList);
                System.out.println("User table refreshed with " + users.size() + " users");
            } else {
                System.err.println("User table view is null - cannot refresh");
            }
        } catch (Exception ex) {
            System.err.println("Error refreshing user table: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private VBox createAuditTrailTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(20));

        Label titleLabel = new Label("Audit Trail");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Refresh button
        Button refreshButton = new Button("Refresh Audit Trail");
        refreshButton.getStyleClass().addAll("button", "button-secondary");

        // Create table for audit entries
        TableView<AuditEntry> auditTable = new TableView<>();

        // Define columns
        TableColumn<AuditEntry, String> auditIdCol = new TableColumn<>("Audit ID");
        auditIdCol.setCellValueFactory(new PropertyValueFactory<>("auditId"));

        TableColumn<AuditEntry, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(new PropertyValueFactory<>("action"));

        TableColumn<AuditEntry, String> timestampCol = new TableColumn<>("Timestamp");
        timestampCol.setCellValueFactory(new PropertyValueFactory<>("timeStamp"));

        TableColumn<AuditEntry, String> userIdCol = new TableColumn<>("User ID");
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));

        TableColumn<AuditEntry, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));

        auditTable.getColumns().addAll(auditIdCol, actionCol, timestampCol, userIdCol, detailsCol);

        // Set column widths
        auditIdCol.setPrefWidth(150);
        actionCol.setPrefWidth(120);
        timestampCol.setPrefWidth(150);
        userIdCol.setPrefWidth(100);
        detailsCol.setPrefWidth(400);

        // Status label
        Label statusLabel = new Label();

        // Load audit data
        refreshButton.setOnAction(e -> {
            try {
                List<AuditEntry> auditEntries = auditDAO.findAll();
                ObservableList<AuditEntry> observableList = FXCollections.observableArrayList(auditEntries);
                auditTable.setItems(observableList);

                statusLabel.setText("Loaded " + auditEntries.size() + " audit entries");
                statusLabel.getStyleClass().removeAll("status-label-error");
                statusLabel.getStyleClass().add("status-label-success");
            } catch (Exception ex) {
                statusLabel.setText("Error loading audit trail: " + ex.getMessage());
                statusLabel.getStyleClass().removeAll("status-label-success");
                statusLabel.getStyleClass().add("status-label-error");
            }
        });

        // Load initial data
        refreshButton.fire();

        tabContent.getChildren().addAll(titleLabel, refreshButton, auditTable, statusLabel);
        return tabContent;
    }

    private VBox createSystemInfoTab() {
        VBox tabContent = new VBox(15);
        tabContent.setPadding(new Insets(20));

        Label titleLabel = new Label("System Information");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // System info
        VBox infoBox = new VBox(10);
        infoBox.getStyleClass().add("card");

        Label javaVersion = new Label("Java Version: " + System.getProperty("java.version"));
        Label javaFxVersion = new Label("JavaFX Version: 17.0.2");
        Label databaseInfo = new Label("Database: SQLite");
        Label userCount = new Label("Total Users: " + userDAO.findAll().size());
        Label auditCount = new Label("Audit Entries: " + auditDAO.findAll().size());

        infoBox.getChildren().addAll(javaVersion, javaFxVersion, databaseInfo, userCount, auditCount);

        // System actions
        VBox actionsBox = new VBox(10);
        Label actionsLabel = new Label("System Actions");
        actionsLabel.setStyle("-fx-font-weight: bold;");

        Button clearAuditButton = new Button("Clear Old Audit Entries");
        Button backupButton = new Button("Backup Database");

        clearAuditButton.getStyleClass().addAll("button", "button-secondary");
        backupButton.getStyleClass().addAll("button", "button-primary");

        actionsBox.getChildren().addAll(actionsLabel, clearAuditButton, backupButton);

        // Status label
        Label statusLabel = new Label();

        clearAuditButton.setOnAction(e -> {
            try {
                // Show confirmation dialog
                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Clear Old Audit Entries");
                confirmation.setHeaderText("Clear Old Audit Entries");
                confirmation.setContentText(
                        "This will delete audit entries older than 30 days. This action cannot be undone. Continue?");

                confirmation.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            // Delete audit entries older than 30 days
                            int deletedCount = auditDAO.deleteOldEntries(30);

                            statusLabel.setText("Successfully deleted " + deletedCount + " old audit entries");
                            statusLabel.getStyleClass().removeAll("status-label-error");
                            statusLabel.getStyleClass().add("status-label-success");

                            // Record audit entry for this action
                            auditDAO.recordAudit(currentUser.getUserId(), "AUDIT_CLEANUP",
                                    "Cleared " + deletedCount + " old audit entries");

                        } catch (Exception ex) {
                            statusLabel.setText("Error clearing audit entries: " + ex.getMessage());
                            statusLabel.getStyleClass().removeAll("status-label-success");
                            statusLabel.getStyleClass().add("status-label-error");
                        }
                    }
                });
            } catch (Exception ex) {
                statusLabel.setText("Error: " + ex.getMessage());
                statusLabel.getStyleClass().removeAll("status-label-success");
                statusLabel.getStyleClass().add("status-label-error");
            }
        });

        // Backup button functionality
        backupButton.setOnAction(e -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Database Backup");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("SQLite Database Files", "*.db", "*.sqlite"));

                String timestamp = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                fileChooser.setInitialFileName("banking_system_backup_" + timestamp + ".db");

                File backupFile = fileChooser.showSaveDialog(stage);

                if (backupFile != null) {
                    boolean success = performDatabaseBackup(backupFile);
                    if (success) {
                        statusLabel.setText("Backup created successfully: " + backupFile.getAbsolutePath());
                        statusLabel.setStyle("-fx-text-fill: #28a745;");
                        auditDAO.recordAudit(currentUser.getUserId(), "DATABASE_BACKUP",
                                "Created database backup: " + backupFile.getName());
                    } else {
                        statusLabel.setText("Backup failed");
                        statusLabel.setStyle("-fx-text-fill: #dc3545;");
                    }
                }
            } catch (Exception ex) {
                statusLabel.setText("Backup error: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: #dc3545;");
            }
        });

        tabContent.getChildren().addAll(titleLabel, infoBox, actionsBox, statusLabel);
        return tabContent;
    }

    /**
     * Perform the actual database backup
     */
    private boolean performDatabaseBackup(File backupFile) {
        try {
            // Get the current database connection to ensure it's initialized
            database.DatabaseConnection.getConnection();

            // Close the connection temporarily to allow backup
            database.DatabaseConnection.closeConnection();

            // Get the source database file path from DatabaseConnection
            String sourceDbPath = database.DatabaseConnection.getDbFilePath();
            File sourceDbFile = new File(sourceDbPath);

            if (!sourceDbFile.exists()) {
                throw new RuntimeException("Source database file not found: " + sourceDbPath);
            }

            // Copy the database file
            try (InputStream in = new FileInputStream(sourceDbFile);
                    OutputStream out = new FileOutputStream(backupFile)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            // Reinitialize the database connection
            database.DatabaseConnection.initializeDatabase();

            return true;
        } catch (Exception ex) {
            // Ensure connection is reinitialized even if backup fails
            try {
                database.DatabaseConnection.initializeDatabase();
            } catch (Exception e) {
                System.err.println("Failed to reinitialize database connection: " + e.getMessage());
            }
            throw new RuntimeException("Backup failed: " + ex.getMessage(), ex);
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