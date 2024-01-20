import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet(name = "DashboardServlet", urlPatterns = "/_dashboard")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init() throws ServletException {
        try {
            // Initialize the DataSource using JNDI lookup
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:comp/env");
            dataSource = (DataSource) envContext.lookup("jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO: Implement login functionality and session management
        // For simplicity, let's assume the user is already logged in.

        // Implementing the metadata retrieval
        try (Connection connection = dataSource.getConnection()) {
            PrintWriter out = response.getWriter();
            out.println("<h2>Database Metadata</h2>");
            out.println("<table border='1'>");
            out.println("<tr><th>Table</th><th>Attribute</th><th>Type</th></tr>");

            // Fetch metadata
            String[] tables = {"movies", "stars", "stars_in_movies", "genres", "genres_in_movies", "creditcards", "customers", "sales", "ratings", "employees"};
            for (String table : tables) {
                try (ResultSet resultSet = connection.getMetaData().getColumns(null, null, table, null)) {
                    while (resultSet.next()) {
                        out.println("<tr><td>" + table + "</td><td>" + resultSet.getString("COLUMN_NAME") + "</td><td>"
                                + resultSet.getString("TYPE_NAME") + "</td></tr>");
                    }
                }
            }
            out.println("</table>");
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching metadata");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        System.out.println("action: " + action);

        if ("addStar".equals(action)) {
            // Handle adding a new star
            handleAddStar(request, response);
        } else if ("addMovie".equals(action)) {
            // Handle adding a new movie
            handleAddMovie(request, response);
        } else {
            // Handle other actions or provide an error response
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
        }
    }

    private void handleAddStar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String starName = request.getParameter("starName");
        String birthYear = request.getParameter("birthYear");

        // Log the data using System.out.println
        System.out.println("Star Name: " + starName);
        System.out.println("Birth Year: " + birthYear);

        try (Connection connection = dataSource.getConnection()) {
            String addStarProcedure = "{CALL add_star(?, ?)}";
            try (CallableStatement callableStatement = connection.prepareCall(addStarProcedure)) {
                callableStatement.setString(1, starName);
                if (birthYear != null && !birthYear.isEmpty() && birthYear.matches("\\d+")) {
                    callableStatement.setInt(2, Integer.parseInt(birthYear));
                } else {
                    callableStatement.setNull(2, java.sql.Types.INTEGER);
                }

                // Execute the stored procedure
                boolean hasResults = callableStatement.execute();

                // Check if there are results
                if (hasResults) {
                    // Retrieve the result message
                    try (ResultSet resultSet = callableStatement.getResultSet()) {
                        if (resultSet.next()) {
                            String message = resultSet.getString("message");
                            response.getWriter().println(message);
                        } else {
                            response.getWriter().println("Error adding star. No result message.");
                        }
                    }
                } else {
                    response.getWriter().println("Error adding star. No results returned.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Log the exception for debugging
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error adding star: " + e.getMessage());
        }
    }



    private void handleAddMovie(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String movieTitle = request.getParameter("movieTitle");
        String movieYear = request.getParameter("movieYear");
        String movieDirector = request.getParameter("movieDirector");
        String starName = request.getParameter("starName");
        String genreName = request.getParameter("genreName");

        try (Connection connection = dataSource.getConnection()) {
            String addMovieProcedure = "{CALL add_movie(?, ?, ?, ?, ?)}";
            try (CallableStatement callableStatement = connection.prepareCall(addMovieProcedure)) {
                callableStatement.setString(1, movieTitle);
                callableStatement.setInt(2, Integer.parseInt(movieYear));
                callableStatement.setString(3, movieDirector);
                callableStatement.setString(4, starName);
                callableStatement.setString(5, genreName);

                // Execute the stored procedure
                boolean hasResults = callableStatement.execute();

                // Check if there are results
                if (hasResults) {
                    // Retrieve the result message
                    try (ResultSet resultSet = callableStatement.getResultSet()) {
                        if (resultSet.next()) {
                            String message = resultSet.getString("message");
                            response.getWriter().println(message);
                        } else {
                            response.getWriter().println("Error adding movie. No result message.");
                        }
                    }
                } else {
                    response.getWriter().println("Error adding star. No results returned.");
                }

            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error adding movie");
        }
    }
}
