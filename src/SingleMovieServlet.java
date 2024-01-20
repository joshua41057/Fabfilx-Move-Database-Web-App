import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession();
        String title = (String) session.getAttribute("title");
        String year = (String) session.getAttribute("year");
        String director = (String) session.getAttribute("director");
        String star = (String) session.getAttribute("star");

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = MyUtils.getReadConnection()) {
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query = "SELECT m.title AS movie_title, m.year AS movie_year, m.director AS movie_director, " +
                    "GROUP_CONCAT(DISTINCT CONCAT(g.id, ':', g.name) ORDER BY g.name ASC) AS movie_genres, " +
                    "GROUP_CONCAT(DISTINCT CONCAT(s.id, ':', s.name) ORDER BY stars_movies_count DESC, s.name ASC) AS movie_stars, " +
                    "(SELECT rating FROM ratings WHERE movieId = m.id) AS movie_rating " +
                    "FROM movies m " +
                    "JOIN genres_in_movies gm ON m.id = gm.movieId " +
                    "JOIN genres g ON gm.genreId = g.id " +
                    "JOIN stars_in_movies sm ON m.id = sm.movieId " +
                    "JOIN stars s ON sm.starId = s.id " +
                    "LEFT JOIN (SELECT starId, COUNT(movieId) AS stars_movies_count FROM stars_in_movies GROUP BY starId) smc ON sm.starId = smc.starId " +
                    "WHERE m.id = ?;";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            ResultSet rs = statement.executeQuery();

            JsonObject movieObject = new JsonObject();

            if (rs.next()) {


                String movie_title = rs.getString("movie_title");
                int movie_year = rs.getInt("movie_year");
                String movie_director = rs.getString("movie_director");
                String movie_genres = rs.getString("movie_genres");
                String movie_stars = rs.getString("movie_stars");
                float movie_rating = rs.getFloat("movie_rating");

                movieObject.addProperty("movie_title", movie_title);
                movieObject.addProperty("movie_year", movie_year);
                movieObject.addProperty("movie_director", movie_director);


                movieObject.addProperty("movie_rating", movie_rating);



                String[] genresArray = movie_genres.split(",");
                JsonArray genresJsonArray = new JsonArray();
                for (String GenreInfo : genresArray) {
                    String[] GenreInfoParts = GenreInfo.split(":");
                    if (GenreInfoParts.length == 2) {
                        JsonObject GenreObject = new JsonObject();
                        GenreObject.addProperty("genre_id", GenreInfoParts[0]);
                        GenreObject.addProperty("genre_name", GenreInfoParts[1]);
                        genresJsonArray.add(GenreObject);
                    }
                }

                for (int i = 0; i < genresArray.length; i++) {
                    genresJsonArray.add(genresArray[i]);
                }
                movieObject.add("movie_genres", genresJsonArray);



                String[] starsArray = movie_stars.split(",");
                JsonArray starsJsonArray = new JsonArray();
                for (String starInfo : starsArray) {
                    String[] starInfoParts = starInfo.split(":");
                    if (starInfoParts.length == 2) {
                        JsonObject starObject = new JsonObject();
                        starObject.addProperty("star_id", starInfoParts[0]);
                        starObject.addProperty("star_name", starInfoParts[1]);
                        starsJsonArray.add(starObject);
                    }
                }
                movieObject.add("movie_stars", starsJsonArray);

                movieObject.addProperty("movie_rating", movie_rating);

                // Write the JSON response
                out.write(movieObject.toString());
            } else {
                JsonObject errorObject = new JsonObject();
                errorObject.addProperty("errorMessage", "Movie not found.");
                out.write(errorObject.toString());
                response.setStatus(404); // Not Found
            }

            rs.close();
            statement.close();

            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

    }

}