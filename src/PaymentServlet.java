import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "PaymentServlet", urlPatterns = {"/checkout"})
public class PaymentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init() throws ServletException {
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:comp/env");
            dataSource = (DataSource) envContext.lookup("jdbc/moviedb");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String firstName = request.getParameter("fname");
        String lastName = request.getParameter("lname");
        String creditCardInfo = request.getParameter("credit_info");
        String expirationDate = request.getParameter("exp_date");

        // Process payment information and validate credit card
        boolean validCheckout = validatePaymentInformation(firstName, lastName, creditCardInfo, expirationDate);

        // Create a JSON object to represent the response
        JsonObject jsonResponse = new JsonObject();

        if (validCheckout) {
            // If payment is valid, set "valid_checkout" to true and add any additional data
            jsonResponse.addProperty("valid_checkout", true);
            jsonResponse.addProperty("message", "Payment Successful"); // You can customize this message
        } else {
            // If payment is not valid, set "valid_checkout" to false and provide a failure message
            jsonResponse.addProperty("valid_checkout", false);
            jsonResponse.addProperty("message", "Payment validation failed");
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.write(jsonResponse.toString()); // Send the JSON response
        out.close();
    }

    private boolean validatePaymentInformation(String firstName, String lastName, String creditCardInfo, String expirationDate) {
        // Implement your payment validation logic here (e.g., validate credit card information)
        try (Connection conn = MyUtils.getReadConnection()) {
            String query = "SELECT COUNT(*) AS count " +
                    "FROM creditcards " +
                    "WHERE firstName = ? " +
                    "AND lastName = ? " +
                    "AND id = ? " +
                    "AND expiration = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, creditCardInfo);
            statement.setString(4, expirationDate);

            // Print or log the values
            System.out.println("firstName: " + firstName);
            System.out.println("lastName: " + lastName);
            System.out.println("creditCardInfo: " + creditCardInfo);
            System.out.println("expirationDate: " + expirationDate);

            ResultSet rs = statement.executeQuery();
            if (rs.next() && rs.getInt("count") > 0) {
                return true;  // Payment is valid
            }
        } catch (SQLException e) {
            // Handle database error
            e.printStackTrace();
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }


        return false;  // Payment is not valid
    }
}
