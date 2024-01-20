import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.jasypt.util.password.StrongPasswordEncryptor;



@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        // String recaptchaResponse = request.getParameter("g-recaptcha-response");

        PrintWriter out = response.getWriter();

        // Continue with your existing login logic
        try (Connection conn = MyUtils.getReadConnection()) {
            // Check if the user is a customer
            String customerQuery = "SELECT * FROM customers WHERE email = ?";
            PreparedStatement customerStatement = conn.prepareStatement(customerQuery);
            customerStatement.setString(1, username);

            ResultSet customerRs = customerStatement.executeQuery();

            // Check if the user is an employee
            String employeeQuery = "SELECT * FROM employees WHERE email = ?";
            PreparedStatement employeeStatement = conn.prepareStatement(employeeQuery);
            employeeStatement.setString(1, username);

            ResultSet employeeRs = employeeStatement.executeQuery();

            JsonObject responseJsonObject = new JsonObject();

            if (customerRs.next()) {
                // Retrieve the encrypted password from the database
                String encryptedPassword = customerRs.getString("password");

                // Use the StrongPasswordEncryptor to compare the user input password with the encrypted password
                boolean success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);

                if (success) {
                    // Correct password: Login success
                    User user = new User(username);
                    request.getSession().setAttribute("user", user);
                    // No role attribute for customers
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "Login successful");
                } else {
                    // Incorrect password
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Incorrect password");
                }
            } else if (employeeRs.next()) {
                // Retrieve the encrypted password from the database for employees
                String encryptedPassword = employeeRs.getString("password");

                // Use the StrongPasswordEncryptor to compare the user input password with the encrypted password
                boolean success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);

                if (success) {
                    // Correct password: Login success
                    User user = new User(username);
                    request.getSession().setAttribute("user", user);
                    request.getSession().setAttribute("role", "employee");

                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "Login successful");
                    responseJsonObject.addProperty("role", "employee");
                } else {
                    // Incorrect password
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Incorrect password");
                }
            } else {
                // User with the provided email doesn't exist
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "User does not exist");
            }

            /*try {
                // Verify reCAPTCHA
                RecaptchaVerifyUtils.verify(recaptchaResponse);
            } catch (Exception e) {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "reCAPTCHA verification failed");
            }*/

            customerRs.close();
            employeeRs.close();
            customerStatement.close();
            employeeStatement.close();
            out.write(responseJsonObject.toString());

        } catch (Exception e) {
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("status", "error");
            responseJsonObject.addProperty("message", e.getMessage());

            out.write(responseJsonObject.toString());
            response.setStatus(404); // Not Found
        } finally {
            out.close();
        }
    }
}
