import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

public class UpdateSecurePassword {

    /*
     *
     * This program updates your existing moviedb customers table to change the
     * plain text passwords to encrypted passwords.
     *
     * You should only run this program **once**, because this program uses the
     * existing passwords as real passwords, then replace them. If you run it more
     * than once, it will treat the encrypted passwords as real passwords and
     * generate wrong values.
     *
     */
    public static void main(String[] args) {
        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        try {
            // Load the JDBC driver
            Class.forName("com.mysql.jdbc.Driver").newInstance();

            // Establish a connection
            try (Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd)) {

                // Create a statement
                try (Statement statement = connection.createStatement()) {

                    // change the customers table password column from VARCHAR(20) to VARCHAR(128)
                    String alterQuery = "ALTER TABLE employees MODIFY COLUMN password VARCHAR(128)";
                    int alterResult = statement.executeUpdate(alterQuery);
                    System.out.println("altering customers table schema completed, " + alterResult + " rows affected");

                    // get the ID and password for each customer
                    String query = "SELECT email, password FROM employees";

                    try (ResultSet rs = statement.executeQuery(query)) {

                        // we use the StrongPasswordEncryptor from jasypt library (Java Simplified Encryption)
                        // it internally uses SHA-256 algorithm and 10,000 iterations to calculate the encrypted password
                        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

                        System.out.println("encrypting password (this might take a while)");
                        while (rs.next()) {
                            // get the email and plain text password from the current table
                            String email = rs.getString("email");
                            String plainPassword = rs.getString("password");

                            // encrypt the password using StrongPasswordEncryptor
                            String encryptedPassword = passwordEncryptor.encryptPassword(plainPassword);

                            // generate the update query
                            String updateQuery = String.format("UPDATE employees SET password='%s' WHERE email='%s';",
                                    encryptedPassword, email);
                            // Execute the update query
                            statement.executeUpdate(updateQuery);
                        }
                    }
                    System.out.println("Password encryption and update completed successfully!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
