import dao.*;
import model.*;
import java.sql.Date;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Database Test ===");

        // Test DB connection
        try {
            if (DBConnection.getInstance() != null) {
                System.out.println("✓ Database connected!\n");
            }
        } catch (Exception e) {
            System.out.println("✗ DB connection failed: " + e.getMessage());
        }

        // Test saving and retrieving a customer
        try {
            CustomerDAO customerDAO = new CustomerDAO();
            // Correct number of parameters: userId, name, address, email
            Customer customer = new Customer("U1001", "John Doe", "123 Main St", "daniel@me.com", "Daniel", "Gaborone", "72427748", "daniel@me.com", CustomerType.INDIVIDUAL);
            customerDAO.save(customer);

            Customer retrieved = customerDAO.findById("U1001");
            if (retrieved != null) {
                System.out.println("✓ Customer saved and retrieved: " + retrieved.getName());
            }
        } catch (Exception e) {
            System.out.println("✗ Customer test failed: " + e.getMessage());
        }
        System.out.println("\n=== Test Completed ===");
    }
}
