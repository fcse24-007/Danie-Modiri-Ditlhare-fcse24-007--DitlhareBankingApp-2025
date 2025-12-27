package util;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

public class Passwords {

    private static final int ITERATIONS = 3;      
    private static final int MEMORY = 65536;       
    private static final int PARALLELISM = 1;     

    public static String hashPassword(String password) {
        Argon2 argon2 = Argon2Factory.create();

        try {
            return argon2.hash(ITERATIONS, MEMORY, PARALLELISM, password.toCharArray());
        } finally {
            argon2.wipeArray(password.toCharArray()); // Clears password from memory
        }
    }

    public static boolean verifyPassword(String password, String hash) {
        Argon2 argon2 = Argon2Factory.create();
        try {
            return argon2.verify(hash, password.toCharArray());
        } finally {
            argon2.wipeArray(password.toCharArray());
        }
    }

    // Keep your strong password validation as is
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    public static String generateTemporaryPassword() {
        long timestamp = System.currentTimeMillis() % 1000000L;
        // Example: Temp123456Aa1!
        return "Temp" + timestamp + "Aa1!";
    }
}
