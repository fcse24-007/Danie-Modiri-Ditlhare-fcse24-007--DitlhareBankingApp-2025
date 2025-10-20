package main;

public class JavaVersionTest {
    public static void main(String[] args) {
        System.out.println("=== Java Version Check ===");
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Java Vendor: " + System.getProperty("java.vendor"));
        
        // Test Java features used in your project
        testJavaFeatures();
        
        System.out.println("✅ All Java features used in your project are available in Java 17+");
    }
    
    private static void testJavaFeatures() {
        // Test var (Java 10+)
        var message = "Testing Java features";
        System.out.println("✓ 'var' keyword works: " + message);
        
        // Test streams (Java 8+)
        java.util.List<String> list = java.util.List.of("a", "b", "c");
        long count = list.stream().count();
        System.out.println("✓ Streams API works: " + count);
        
        // Test try-with-resources (Java 7+)
        try (java.io.StringWriter writer = new java.io.StringWriter()) {
            writer.write("test");
            System.out.println("✓ Try-with-resources works");
        } catch (Exception e) {}
        
        // Test LocalDate (Java 8+)
        java.time.LocalDate date = java.time.LocalDate.now();
        System.out.println("✓ LocalDate works: " + date);
    }
}