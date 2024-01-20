import com.google.gson.JsonArray;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.google.gson.JsonObject;



@WebServlet(name = "IndexServlet", urlPatterns = {"/api/index"})
public class IndexServlet extends HttpServlet {
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection conn = MyUtils.getReadConnection();){

            // Assuming you have a method to retrieve genres from the database
            JsonArray genresJsonArray = getGenresJsonArray(conn);

            // Send genres as a JSON array
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.write(genresJsonArray.toString());
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle SQL exception
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving genres from the database.");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonArray getGenresJsonArray(Connection connection) throws SQLException {
        String query = "SELECT id, name FROM genres";
        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            JsonArray jsonArray = new JsonArray();

            while (resultSet.next()) {
                int genreId = resultSet.getInt("id");
                String genreName = resultSet.getString("name");

                JsonObject genreJsonObject = new JsonObject();
                genreJsonObject.addProperty("id", genreId);
                genreJsonObject.addProperty("name", genreName);

                jsonArray.add(genreJsonObject);
            }

            return jsonArray;
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Handle POST requests for searching movies
        String title = request.getParameter("title");
        String year = request.getParameter("year");
        String director = request.getParameter("director");
        String star = request.getParameter("star");

        // Get the existing session or create a new one
        HttpSession session = request.getSession(true);

        // Clear existing session attributes before setting new values
        session.removeAttribute("title");
        session.removeAttribute("year");
        session.removeAttribute("director");
        session.removeAttribute("star");
        session.removeAttribute("genre");
        session.removeAttribute("prefix");
        session.removeAttribute("sort");
        session.removeAttribute("page");
        session.removeAttribute("n");


        // Store search parameters in the session
        session.setAttribute("title", title);
        session.setAttribute("year", year);
        session.setAttribute("director", director);
        session.setAttribute("star", star);

        // Redirect to the results page
        response.sendRedirect(request.getContextPath() + "/api/results");
    }
}
