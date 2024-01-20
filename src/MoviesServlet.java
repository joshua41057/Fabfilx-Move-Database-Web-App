import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

// Declaring a WebServlet called MoviesServlet, which maps to url "/api/movies"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet{
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        //TS TJ time test
        //------------------------------------------------------//
        long startTj;
        long endTj;
        long TJ;
        long startTS = System.nanoTime();
        ArrayList<Long> TJs = new ArrayList<>();
        //------------------------------------------------------//


        response.setContentType("application/json"); // Response mime type
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection dbcon = MyUtils.getReadConnection();) {

            String query = "SELECT " +
                    "m.id AS movie_id, " +
                    "m.title AS movie_title, " +
                    "m.year AS movie_year, " +
                    "m.director AS movie_director, " +
                    "GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC) AS movie_genres, " +
                    "GROUP_CONCAT(DISTINCT CONCAT(s.id, ':', s.name) ORDER BY s.name ASC) AS movie_stars, " +
                    "r.rating AS movie_rating " +
                    "FROM movies m " +
                    "JOIN genres_in_movies gm ON m.id = gm.movieId " +
                    "JOIN genres g ON gm.genreId = g.id " +
                    "JOIN stars_in_movies sm ON m.id = sm.movieId " +
                    "JOIN stars s ON sm.starId = s.id " +
                    "JOIN ratings r ON m.id = r.movieId " +
                    "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
                    "ORDER BY r.rating DESC " + // Added a space before LIMIT
                    "LIMIT 20";


            //-----------------------------------------------------------//
            startTj = System.nanoTime();
            //-----------------------------------------------------------//

            PreparedStatement statement = dbcon.prepareStatement(query);
            ResultSet rs = statement.executeQuery();


            //-----------------------------------------------------------//
            endTj = System.nanoTime();
            TJ = endTj - startTj;
            TJs.add(TJ);
            //-----------------------------------------------------------//


            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("movie_id");
                String movie_title = rs.getString("movie_title");
                int movie_year = rs.getInt("movie_year");
                String movie_director = rs.getString("movie_director");
                String movie_genres = rs.getString("movie_genres");
                String movie_stars = rs.getString("movie_stars");
                float movie_rating = rs.getFloat("movie_rating");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);

                // Split the movie_genres and movie_stars into arrays and store the first 3 values
                String[] genresArray = movie_genres.split(",");
                JsonArray genresJsonArray = new JsonArray();
                for (int i = 0; i < Math.min(3, genresArray.length); i++) {
                    genresJsonArray.add(genresArray[i]);
                }
                jsonObject.add("movie_genres", genresJsonArray);

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
                jsonObject.add("movie_stars", starsJsonArray);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);



            //-----------------------------------------------------------//
            long endTS = System.nanoTime();
            long ts = (endTS - startTS);
            long tjTotal = 0;
            for(long d : TJs)
                tjTotal += d;
            String result_str = String.valueOf(ts) + "," + String.valueOf(tjTotal)+"\n";
            Path path = Paths.get(request.getServletContext().getRealPath("/"),"/TSTJlog.csv");
            try (
                    OutputStream logOut = new BufferedOutputStream(
                            Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND))){
                logOut.write(result_str.getBytes());
            } catch (IOException e) {
                request.getServletContext().log(e.getMessage());
            }

            //-----------------------------------------------------------//


        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

    }




}
