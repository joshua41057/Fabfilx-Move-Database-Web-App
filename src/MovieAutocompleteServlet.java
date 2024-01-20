import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/movie-autocomplete")
public class MovieAutocompleteServlet extends HttpServlet {
    private DataSource dataSource;

    public void init() {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            JsonArray jsonArray = new JsonArray();

            // Get the query string from the parameter
            String query = request.getParameter("query");

            // Perform full-text search on the movie title in the database
            try (Connection connection = MyUtils.getReadConnection()) {
                // Split the query into keywords
                String[] keywords = query.split("\\s+");

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
                        " LIMIT 10";

                System.out.println(sql);

                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    // Set parameters for each keyword
                    for (int i = 0; i < keywords.length; i++) {
                        statement.setString(i + 1, keywords[i] + "*");
                    }

                    try (ResultSet resultSet = statement.executeQuery()) {
                        while (resultSet.next()) {
                            String movieId = resultSet.getString("id");
                            String movieTitle = resultSet.getString("title");
                            jsonArray.add(generateJsonObject(movieId, movieTitle));
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Internal Server Error: Database connection error.");
            }

            // Set the response content type
            response.setContentType("application/json");
            // Write the JSON array to the response
            response.getWriter().write(jsonArray.toString());

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private static JsonObject generateJsonObject(String movieId, String movieTitle) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", movieTitle);
        jsonObject.addProperty("id", movieId);
        return jsonObject;
    }
}
