// BankingApplication.java
package view;

import database.DatabaseConnection;
import javafx.application.Application;
import javafx.stage.Stage;
import service.InterestService;

public class BankingApplication extends Application {
    
    private InterestService interestService;

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Starting Banking Application...");
            
            //Initialize services
            initializeServices();
            
            // Show login screen
            LoginView loginView = new LoginView();
            loginView.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to start application: " + e.getMessage());
        }
    }

    private void initializeServices() {
        // Start interest service
        interestService = new InterestService();
        interestService.startInterestService();
        
    }
    
    @Override
    public void stop() {
        // Cleanup services when application closes
        if (interestService != null) {
            interestService.stopInterestService();
        }
        DatabaseConnection.closeConnection();
        System.out.println("Banking application stopped");
    }

    public static void main(String[] args) {
        System.out.println("Launching JavaFX Application...");
        launch(args);
    }

}