import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@WebServlet(name = "ResultsServlet", urlPatterns = "/api/results")
public class ResultsServlet extends HttpServlet {
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handleSearchRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handleSearchRequest(request, response);
    }

    protected void handleSearchRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try (Connection conn = MyUtils.getReadConnection()) {
            Statement statement = conn.createStatement();

            String title = request.getParameter("title");
            String year = request.getParameter("year");
            String director = request.getParameter("director");
            String star = request.getParameter("star");
            String genre = request.getParameter("genre");
            String prefix = request.getParameter("prefix");
            String sort = request.getParameter("sort");
            String pageParam =  request.getParameter("page");
            String numResultsPerPageParam = request.getParameter("n");



            // Parse page and results per page parameters
            int page = 1;
            int numResultsPerPage = 10;


            if (pageParam != null && !pageParam.isEmpty()) {
                page = Integer.parseInt(pageParam);
            }
            if (numResultsPerPageParam != null && !numResultsPerPageParam.isEmpty()) {
                numResultsPerPage = Integer.parseInt(numResultsPerPageParam);
            }

            // Calculate offset for pagination
            int offset = (page - 1) * numResultsPerPage;

            // Construct the SQL query based on the parameters
            // Construct the SQL query based on the parameters
            String query = "SELECT " +
                    "m.id AS movie_id, " +
                    "m.title AS movie_title, " +
                    "m.year AS movie_year, " +
                    "m.director AS movie_director, " +
                    "GROUP_CONCAT(DISTINCT g.id, ':', g.name ORDER BY g.name ASC) AS movie_genres, " +
                    "GROUP_CONCAT(DISTINCT CONCAT(s.id, ':', s.name) ORDER BY s.name ASC) AS movie_stars, " +
                    "IFNULL(MAX(r.rating), 'N/A') AS movie_rating " +
                    "FROM movies m " +
                    "JOIN genres_in_movies gm ON m.id = gm.movieId " +
                    "JOIN genres g ON gm.genreId = g.id " +
                    "JOIN stars_in_movies sm ON m.id = sm.movieId " +
                    "JOIN stars s ON sm.starId = s.id " +
                    "LEFT JOIN ratings r ON m.id = r.movieId " +
                    "WHERE 1=1 ";

            // Append conditionals based on parameters using PreparedStatement
            if (title != null && !title.isEmpty()) {
                query += " AND m.title LIKE ?";
            }
            if (year != null && !year.isEmpty()) {
                query += " AND m.year = ?";
            }
            if (director != null && !director.isEmpty()) {
                query += " AND m.director LIKE ?";
            }
            if (star != null && !star.isEmpty()) {
                query += " AND s.name LIKE ?";
            }
            if (genre != null && !genre.isEmpty()) {
                query += " AND g.id = ?";
            }
            if (prefix != null && prefix.equals("*")) {
                query += " AND NOT (m.title REGEXP '^[0-9a-zA-Z].*$') ";
            } else if (prefix != null && !prefix.isEmpty()) {
                query += " AND m.title LIKE ?";
            }

            query += " GROUP BY movie_id, movie_title, movie_year, movie_director, r.rating ";


            // Sorting
            if (sort != null && !sort.isEmpty()) {
                switch (sort) {
                    case "0":
                        query += " ORDER BY movie_title ASC, movie_rating ASC";
                        break;
                    case "1":
                        query += " ORDER BY movie_title ASC, movie_rating DESC";
                        break;
                    case "2":
                        query += " ORDER BY movie_title DESC, movie_rating ASC";
                        break;
                    case "3":
                        query += " ORDER BY movie_title DESC, movie_rating DESC";
                        break;
                    case "4":
                        query += " ORDER BY movie_rating ASC, movie_title ASC";
                        break;
                    case "5":
                        query += " ORDER BY movie_rating ASC, movie_title DESC";
                        break;
                    case "6":
                        query += " ORDER BY movie_rating DESC, movie_title ASC";
                        break;
                    case "7":
                        query += " ORDER BY movie_rating DESC, movie_title DESC";
                        break;
                }
            }
            query += " LIMIT ? OFFSET ?";
            // Execute the query
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            int parameterIndex = 1;
            // Set parameters for PreparedStatement
            if (title != null && !title.isEmpty()) {
                preparedStatement.setString(parameterIndex++, "%" + title + "%");
            }
            if (year != null && !year.isEmpty()) {
                preparedStatement.setString(parameterIndex++, year);
            }
            if (director != null && !director.isEmpty()) {
                preparedStatement.setString(parameterIndex++, "%" + director + "%");
            }
            if (star != null && !star.isEmpty()) {
                preparedStatement.setString(parameterIndex++, "%" + star + "%");
            }
            if (genre != null && !genre.isEmpty()) {
                preparedStatement.setString(parameterIndex++, genre);
            }
            if (prefix != null && !prefix.isEmpty() && !prefix.equals("*")) {
                preparedStatement.setString(parameterIndex++, prefix + "%");
            }

            // Set additional parameters for sorting, limit, and offset
            preparedStatement.setInt(parameterIndex++, numResultsPerPage);
            preparedStatement.setInt(parameterIndex++, offset);

            // Execute the query
            ResultSet rs = preparedStatement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                String movie_id = rs.getString("movie_id");
                String movie_title = rs.getString("movie_title");
                int movie_year = rs.getInt("movie_year");
                String movie_director = rs.getString("movie_director");
                String movie_genres = rs.getString("movie_genres");
                String movie_stars = rs.getString("movie_stars");
                String movie_rating = rs.getString("movie_rating");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);

                String[] genresArray = movie_genres.split(",");
                JsonArray genresJsonArray = new JsonArray();
                for (String genreInfo : genresArray) {
                    String[] genreInfoParts = genreInfo.split(":");
                    if (genreInfoParts.length == 2) {
                        JsonObject genreObject = new JsonObject();
                        genreObject.addProperty("genre_id", genreInfoParts[0]);
                        genreObject.addProperty("genre_name", genreInfoParts[1]);
                        genresJsonArray.add(genreObject);
                    }
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

            out.write(jsonArray.toString());
            response.setStatus(200);
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
