import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
import java.util.ArrayList;
import java.util.List;



@WebServlet(name = "CartServlet", urlPatterns = {"/cart"})
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init() throws ServletException {
        try {
            // Initialize the DataSource using JNDI lookup
            // (Assuming you have set up the JNDI configuration)
            // You may need to adjust the JNDI name to match your configuration.
            // Example:
            // "jdbc/moviedb" should be the name of the JNDI data source in your server configuration.
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:comp/env");
            dataSource = (DataSource) envContext.lookup("jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        // Get cart items from the session
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");

        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }

        // Send the cart items as JSON response
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.write(convertCartItemsToJson(cartItems));
        out.close();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        // Get cart items from the session
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");

        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }

        // Get parameters for the cart action
        String movieIdParam = request.getParameter("currentMovieId");
        String typeOfChange = request.getParameter("typeOfChange");

        if (movieIdParam != null && typeOfChange != null) {
            int movieId = Integer.parseInt(movieIdParam);

            if (typeOfChange.equals("increment")) {
                // Increment the quantity of the selected movie in the cart
                for (CartItem cartItem : cartItems) {
                    if (cartItem.getMovieId() == movieId) {
                        cartItem.incrementQuantity();
                        break;
                    }
                }
            } else if (typeOfChange.equals("decrement")) {
                // Decrement the quantity of the selected movie in the cart
                for (CartItem cartItem : cartItems) {
                    if (cartItem.getMovieId() == movieId) {
                        cartItem.decrementQuantity();
                        if (cartItem.getQuantity() == 0) {
                            // Remove the item from the cart if the quantity becomes zero
                            cartItems.remove(cartItem);
                        }
                        break;
                    }
                }
            } else if (typeOfChange.equals("delete")) {
                // Delete the movie from the cart
                cartItems.removeIf(cartItem -> cartItem.getMovieId() == movieId);
            }
        }

        // Store the updated cart items in the session
        session.setAttribute("cartItems", cartItems);
    }

    private String convertCartItemsToJson(List<CartItem> cartItems) {
        // Convert cart items to a JSON array
        Gson gson = new Gson();
        return gson.toJson(cartItems);
    }
}