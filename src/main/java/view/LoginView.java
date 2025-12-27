// LoginView.java
package view;

import model.User;
import model.UserRole;

import controller.AuthenticationController;
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
    private Button loginButton;
    private Label statusLabel;

    public LoginView() {
        this.stage = new Stage();
        this.authController = new AuthenticationController();
        initializeUI();
    }

    private void initializeUI() {
        stage.setTitle("Banking System - Login");

        // Main container
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.getStyleClass().add("root");
        mainContainer.setPadding(new Insets(40));

        // Header
        Label headerLabel = new Label("Banking System");
        headerLabel.getStyleClass().add("header-label");

        // Login form container
        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(30));
        formContainer.setAlignment(Pos.CENTER);
        formContainer.getStyleClass().add("card");
        formContainer.setMaxWidth(400);

        // Username field
        VBox usernameBox = new VBox(5);
        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle("-fx-font-weight: bold;");
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.getStyleClass().add("text-field");
        usernameBox.getChildren().addAll(usernameLabel, usernameField);

        // Password field
        VBox passwordBox = new VBox(5);
        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-font-weight: bold;");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.getStyleClass().add("password-field");
        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        // Login button
        loginButton = new Button("Login");
        loginButton.getStyleClass().add("button");
        loginButton.setPrefWidth(120);
        loginButton.setPrefHeight(40);

        // Status label
        statusLabel = new Label();
        statusLabel.setWrapText(true);

        // Add components to form
        formContainer.getChildren().addAll(
                usernameBox, passwordBox, loginButton, statusLabel);

        // Add to main container
        mainContainer.getChildren().addAll(headerLabel, formContainer);

        // Set up event handlers
        setupEventHandlers();

        // Create scene and show stage
        Scene scene = new Scene(mainContainer, 500, 500);

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
        FadeTransition fadeOut = new FadeTransition(Duration.millis(800), mainContainer);
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
        String password = passwordField.getText();

        // Clear previous status
        statusLabel.setText("");

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both username and password");
            return;
        }

        // Show loading state
        loginButton.setDisable(true);
        loginButton.setText("Logging in...");

        // Perform login (this would typically be in a background thread)
        new Thread(() -> {
            try {
                // Step 2-3: Authenticate user
                var result = authController.login(username, password);

                // Update UI on JavaFX Application Thread
                javafx.application.Platform.runLater(() -> {
                    loginButton.setDisable(false);
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
                    loginButton.setDisable(false);
                    loginButton.setText("Login");
                    statusLabel.setText("Login failed: " + e.getMessage());
                    statusLabel.getStyleClass().removeAll("status-label-success");
                    statusLabel.getStyleClass().add("status-label-error");
                });
            }
        }).start();
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