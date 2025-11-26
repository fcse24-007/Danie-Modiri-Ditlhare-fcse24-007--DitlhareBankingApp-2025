// InterestService.java
package service;

import database.AccountDAO;
import model.Account;
import model.InterestBearing;
import model.SavingsAccount;
import model.InvestmentAccount;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InterestService {
    private AccountDAO accountDAO;
    private ScheduledExecutorService scheduler;

    public InterestService() {
        this.accountDAO = new AccountDAO();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void startInterestService() {
        // Run interest calculation every day at 2 AM
        scheduler.scheduleAtFixedRate(this::applyInterestToAllAccounts, 
            0, 1, TimeUnit.DAYS);
        
        System.out.println("Interest service started - will run daily");
    }

    public void stopInterestService() {
        scheduler.shutdown();
        System.out.println("Interest service stopped");
    }

    private void applyInterestToAllAccounts() {
        try {
            System.out.println("Applying interest to all accounts...");
            List<Account> allAccounts = accountDAO.findAll();
            
            int interestAppliedCount = 0;
            for (Account account : allAccounts) {
                if (account instanceof InterestBearing) {
                    InterestBearing interestAccount = (InterestBearing) account;
                    interestAccount.applyInterest();
                    
                    // Update account in database
                    accountDAO.update(account);
                    interestAppliedCount++;
                }
            }
            
            System.out.println("Interest applied to " + interestAppliedCount + " accounts");
            
        } catch (Exception e) {
            System.err.println("Error applying interest: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Manual interest application for testing
    public void applyInterestNow() {
        applyInterestToAllAccounts();
    }
}