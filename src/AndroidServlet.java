import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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

@WebServlet(name = "AndroidServlet", urlPatterns = "/api/android")
public class AndroidServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init() {
        try {
            // Initialize the DataSource using JNDI lookup
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:comp/env");
            dataSource = (DataSource) envContext.lookup("jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String page = request.getParameter("page");
        System.out.println("Received search request with page: " + page);

        handleSearchRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String query = request.getParameter("title");
        System.out.println("Received search request with title: " + query);

        // Store the title in the session
        request.getSession().setAttribute("searchTitle", query);

        handleSearchRequest(request, response);
    }

    protected void handleSearchRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        int resultsPerPage = 10;
        try (Connection conn = dataSource.getConnection()) {
            // Get the query string from the parameter
            String query = (String) request.getSession().getAttribute("searchTitle");

            // Split the query into keywords
            String[] keywords = query.split("\\s+");

            // Determine the current page
            int currentPage = Integer.parseInt(request.getParameter("page"));
            System.out.println("currentPage: " + currentPage);

            // Calculate the offset based on the current page
            int offset = (currentPage - 1) * resultsPerPage;

            // Build the MATCH condition for each keyword
            StringBuilder matchConditionBuilder = new StringBuilder();
            for (int i = 0; i < keywords.length; i++) {
                if (i > 0) {
                    matchConditionBuilder.append(" AND ");
                }
                matchConditionBuilder.append("MATCH(title) AGAINST(? IN BOOLEAN MODE)");
            }

            // SQL query with dynamic MATCH conditions
            String sql = "SELECT id, title FROM movies WHERE " +
                    matchConditionBuilder.toString() +
                    " LIMIT ? OFFSET ?";

            // Declare jsonArray here
            JsonArray jsonArray = new JsonArray();
            JsonObject responseJson = new JsonObject();

            // Use a PreparedStatement to set parameters
            try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                // Set parameters for each keyword
                for (int i = 0; i < keywords.length; i++) {
                    preparedStatement.setString(i + 1, keywords[i] + "*");
                }

                // Set LIMIT parameter
                preparedStatement.setInt(keywords.length + 1, resultsPerPage);

                // Set OFFSET parameter
                preparedStatement.setInt(keywords.length + 2, offset);

                // Execute the query
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    // Fetch results for the current page
                    while (rs.next()) {
                        String movieId = rs.getString("id");
                        String movieTitle = rs.getString("title");
                        System.out.println("Received search request with movieTitle: " + movieTitle);

                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("movie_id", movieId);
                        jsonObject.addProperty("movie_title", movieTitle);

                        jsonArray.add(jsonObject);
                    }
                }

                // Add pagination information to the response
                JsonObject paginationInfo = new JsonObject();
                paginationInfo.addProperty("results_per_page", resultsPerPage);
                paginationInfo.addProperty("current_page", currentPage);


                // Add results and pagination information to the response
                responseJson.add("movies", jsonArray);
                responseJson.add("pagination", paginationInfo);

                out.write(responseJson.toString());
                response.setStatus(200);
            }
        } catch (Exception e) {
            // Handle exceptions
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
