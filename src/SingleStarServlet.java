import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String id = request.getParameter("id");
        request.getServletContext().log("getting id: " + id);
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();
        String title = (String) session.getAttribute("title");
        String year = (String) session.getAttribute("year");
        String director = (String) session.getAttribute("director");
        String star = (String) session.getAttribute("star");

        try (Connection conn = MyUtils.getReadConnection()) {
            // Construct a query with parameter represented by "?"
            String query = "SELECT s.id AS starId, s.name AS starName, s.birthYear AS starDob, " +
                    "m.id AS movieId, m.title AS movieTitle, m.year AS movieYear, m.director AS movieDirector " +
                    "FROM stars s " +
                    "JOIN stars_in_movies sim ON s.id = sim.starId " +
                    "JOIN movies m ON sim.movieId = m.id " +
                    "WHERE s.id = ? " +
                    "ORDER BY m.year DESC, m.title ASC"; // Sorting by year and title

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, id);
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                String starId = rs.getString("starId");
                String starName = rs.getString("starName");
                String starDob = rs.getString("starDob");
                String movieId = rs.getString("movieId");
                String movieTitle = rs.getString("movieTitle");
                String movieYear = rs.getString("movieYear");
                String movieDirector = rs.getString("movieDirector");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("star_id", starId);
                jsonObject.addProperty("star_name", starName);
                jsonObject.addProperty("star_dob", starDob);
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            out.write(jsonArray.toString());
            response.setStatus(200);
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
