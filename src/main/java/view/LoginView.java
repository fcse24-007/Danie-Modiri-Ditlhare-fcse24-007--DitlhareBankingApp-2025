// LoginView.java
package view;

import model.User;
import model.UserRole;

import controller.AuthenticationController;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class LoginView {
    private Stage stage;
    private AuthenticationController authController;
    private TextField usernameField;
    private PasswordField passwordField;
    private TextField visiblePasswordField;
    private CheckBox showPasswordCheck;
    private Button loginButton;
    private Label statusLabel;
    private ProgressIndicator loadingIndicator;
    private Label helperLabel;

    public LoginView() {
        this.stage = new Stage();
        this.authController = new AuthenticationController();
        initializeUI();
    }

    private void initializeUI() {
        stage.setTitle("Banking System - Login");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        HBox wrapper = new HBox(32);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(40));
        wrapper.getStyleClass().add("login-wrapper");

        // Informational panel
        VBox infoPanel = new VBox(10);
        infoPanel.getStyleClass().add("info-panel");
        Label appLabel = new Label("Ditlhare Banking");
        appLabel.getStyleClass().add("header-label");
        Label taglineLabel = new Label("Secure, modern banking for customers, employees, and administrators.");
        taglineLabel.getStyleClass().add("muted-text");
        Label bullet1 = new Label("• Argon2 password security & audit trails");
        bullet1.getStyleClass().add("bullet-text");
        Label bullet2 = new Label("• Role-based dashboards & quick navigation");
        bullet2.getStyleClass().add("bullet-text");
        Label bullet3 = new Label("• Activity monitored with smart lockout protection");
        bullet3.getStyleClass().add("bullet-text");
        infoPanel.getChildren().addAll(appLabel, taglineLabel, bullet1, bullet2, bullet3);

        // Login form container
        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(30));
        formContainer.setAlignment(Pos.CENTER);
        formContainer.getStyleClass().addAll("card", "login-card");
        formContainer.setMaxWidth(420);

        Label welcomeLabel = new Label("Welcome back");
        welcomeLabel.getStyleClass().add("sub-header-label");

        // Username field
        VBox usernameBox = new VBox(5);
        Label usernameLabel = new Label("Username");
        usernameLabel.getStyleClass().add("input-label");
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.getStyleClass().add("text-field");
        usernameBox.getChildren().addAll(usernameLabel, usernameField);

        // Password field with toggle
        VBox passwordBox = new VBox(5);
        Label passwordLabel = new Label("Password");
        passwordLabel.getStyleClass().add("input-label");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.getStyleClass().add("password-field");

        visiblePasswordField = new TextField();
        visiblePasswordField.setPromptText("Enter your password");
        visiblePasswordField.getStyleClass().add("password-field");
        visiblePasswordField.setManaged(false);
        visiblePasswordField.setVisible(false);
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        showPasswordCheck = new CheckBox("Show password");
        showPasswordCheck.getStyleClass().add("muted-text");
        showPasswordCheck.setOnAction(e -> {
            boolean show = showPasswordCheck.isSelected();
            passwordField.setManaged(!show);
            passwordField.setVisible(!show);
            visiblePasswordField.setManaged(show);
            visiblePasswordField.setVisible(show);
        });

        VBox passwordFieldHolder = new VBox(6, passwordField, visiblePasswordField, showPasswordCheck);
        passwordBox.getChildren().addAll(passwordLabel, passwordFieldHolder);

        // Action row
        loginButton = new Button("Login");
        loginButton.getStyleClass().add("button");
        loginButton.setPrefWidth(150);
        loginButton.setPrefHeight(42);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(28, 28);

        HBox actionRow = new HBox(10, loginButton, loadingIndicator);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        // Helper + status
        helperLabel = new Label("Tip: Use your assigned username. Accounts lock after repeated failures.");
        helperLabel.getStyleClass().add("muted-text");
        statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.getStyleClass().add("status-label");

        Hyperlink forgotPasswordLink = new Hyperlink("Forgot password?");
        forgotPasswordLink.setOnAction(e -> showForgotPasswordHelp());

        // Disable login until fields are populated or while loading
        loginButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> usernameField.getText().trim().isEmpty()
                                || passwordField.getText().trim().isEmpty()
                                || loadingIndicator.isVisible(),
                        usernameField.textProperty(),
                        passwordField.textProperty(),
                        loadingIndicator.visibleProperty()));

        formContainer.getChildren().addAll(
                welcomeLabel,
                usernameBox,
                passwordBox,
                actionRow,
                forgotPasswordLink,
                helperLabel,
                statusLabel);

        wrapper.getChildren().addAll(infoPanel, formContainer);
        root.setCenter(wrapper);

        // Set up event handlers
        setupEventHandlers();

        // Create scene and show stage
        Scene scene = new Scene(root, 900, 520);

        // Load CSS
        try {
            String cssPath = getClass().getResource("/styles/styles.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Error loading CSS: " + e.getMessage());
        }

        stage.setScene(scene);
        stage.setResizable(false);

        // Add Fade In Animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(800), root);
        fadeOut.setFromValue(0);
        fadeOut.setToValue(1);
        fadeOut.play();
    }

    private void setupEventHandlers() {
        // Login button action
        loginButton.setOnAction(e -> login());

        // Enter key support
        usernameField.setOnAction(e -> login());
        passwordField.setOnAction(e -> login());
        visiblePasswordField.setOnAction(e -> login());
    }

    /**
     * Matches Login Sequence Diagram exactly:
     * 1. enterCredentials(username, password)
     * 2. login(username, password)
     * 3. authenticate(username, password) : boolean
     * 4. return authResult : boolean
     */
    private void login() {
        String username = usernameField.getText().trim();
        String password = getPasswordInput();

        // Clear previous status
        statusLabel.setText("");
        statusLabel.getStyleClass().removeAll("status-label-success", "status-label-error");

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password");
            return;
        }

        // Show loading state
        loadingIndicator.setVisible(true);
        loginButton.setText("Logging in...");

        // Perform login (this would typically be in a background thread)
        new Thread(() -> {
            try {
                // Step 2-3: Authenticate user
                var result = authController.login(username, password);

                // Update UI on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    loginButton.setText("Login");

                    if (result.isSuccess()) {
                        // Step 7-8: Login successful - show dashboard
                        statusLabel.setText("Login successful!");
                        statusLabel.getStyleClass().removeAll("status-label-error");
                        statusLabel.getStyleClass().add("status-label-success");

                        // Show appropriate dashboard based on role
                        showDashboard(result.getRole(), result.getUser());
                    } else {
                        // Step 10-11: Login failed
                        statusLabel.setText(result.getMessage());
                        statusLabel.getStyleClass().removeAll("status-label-success");
                        statusLabel.getStyleClass().add("status-label-error");
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    loginButton.setText("Login");
                    statusLabel.setText("Login failed: " + e.getMessage());
                    statusLabel.getStyleClass().removeAll("status-label-success");
                    statusLabel.getStyleClass().add("status-label-error");
                });
            }
        }).start();
    }

    private String getPasswordInput() {
        return showPasswordCheck.isSelected() ? visiblePasswordField.getText() : passwordField.getText();
    }

    private void showForgotPasswordHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password assistance");
        alert.setHeaderText("Can't sign in?");
        alert.setContentText(
                "For security, password resets are handled by administrators.\n"
                        + "If you are locked out, wait for the cooldown or contact an admin for a temporary password reset.");
        alert.showAndWait();
    }

    private void showDashboard(UserRole role, User user) {
        stage.close();

        switch (role) {
            case BANK_EMPLOYEE:
                BankEmployeeDashboard employeeDashboard = new BankEmployeeDashboard(user);
                employeeDashboard.show();
                break;
            case CUSTOMER:
                CustomerDashboard customerDashboard = new CustomerDashboard(user); // Use the fixed one
                customerDashboard.show();
                break;
            case ADMINISTRATOR:
                AdminDashboard adminDashboard = new AdminDashboard(user); // Use the fixed one
                adminDashboard.show();
                break;
            default:
                // Show error and return to login
                statusLabel.setText("Unknown user role: " + role);
                statusLabel.getStyleClass().removeAll("status-label-success");
                statusLabel.getStyleClass().add("status-label-error");
                stage.show(); // Show login again
                break;
        }
    }

    public void show() {
        stage.show();
    }
}
