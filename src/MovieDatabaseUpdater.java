import java.sql.*;




import java.sql.Connection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class MovieDatabaseUpdater {
    private DataSource dataSource;

    String currentMovieId;
    String currentStarId;
    int currentGenreId;
    int currentMovieIdNum;
    int currentStarIdNum;
    Connection connection;
    public void init() throws Exception{
        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = MyUtils.getReadConnection();
    }
    public void setupData(){
        currentMovieId = generateMovieId();
        currentStarId = generateStarId();
        currentGenreId = generateGenreId();
        currentMovieIdNum = Integer.parseInt(currentMovieId.substring(2, 9));
        currentStarIdNum = Integer.parseInt(currentStarId.substring(2, 9));

        currentGenreId = currentGenreId + 1;
        currentMovieId = "tt" + Integer.toString(currentMovieIdNum + 1);
        currentStarId = "nm" + Integer.toString(currentStarIdNum + 1);

    }

    public void updateDatabase() {
        try (Connection conn = MyUtils.getReadConnection()) {
            parseAndInsertMovies("mains243.xml");
            parseAndInsertActors("actors63.xml");
            parseAndInsertCasts("casts124.xml");

            // Commit the changes
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            // Rollback in case of any exception
            rollbackDatabase();
        } finally {
            // Close database connection
            closeDatabase();
        }
    }

    private void rollbackDatabase() {
        try (Connection conn = MyUtils.getReadConnection()) {
            if (conn != null && !conn.isClosed()) {
                conn.rollback();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void parseAndInsertMovies(String xmlFilePath) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFilePath);

            doc.getDocumentElement().normalize();

            NodeList directorFilmsList = doc.getElementsByTagName("directorfilms");

            for (int temp = 0; temp < directorFilmsList.getLength(); temp++) {
                Node directorFilmsNode = directorFilmsList.item(temp);

                if (directorFilmsNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element directorFilmsElement = (Element) directorFilmsNode;

                    insertMovieData(directorFilmsElement);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void insertMovieData(Element directorFilmsElement) {
        try {
            String directorName = getTagValue("dirname", directorFilmsElement);

            NodeList filmList = directorFilmsElement.getElementsByTagName("film");

            for (int temp = 0; temp < filmList.getLength(); temp++) {
                Node filmNode = filmList.item(temp);

                if (filmNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element filmElement = (Element) filmNode;

                    String movieTitle = getTagValue("t", filmElement);
                    try {
                        int movieYear = Integer.parseInt(getTagValue("year", filmElement));
                        if (!movieTitle.isEmpty()) {

                            // Check if the movie already exists in the database
                            if (!movieExists(movieTitle, movieYear, directorName)) {
                                // Movie does not exist, insert into movies table
                                String movieId = currentMovieId;
                                insertMovieIntoDatabase(movieId, movieTitle, movieYear, directorName);
                                currentMovieIdNum = currentMovieIdNum + 1;
                                currentMovieId = "tt" + Integer.toString(currentMovieIdNum);

                                // Insert genres into genres and genres_in_movies tables
                                insertGenres(filmElement, movieId);
                            }
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertMovieIntoDatabase(String movieId, String movieTitle, int movieYear, String directorName) {
        try (Connection conn = MyUtils.getReadConnection()) {
            String insertMovieQuery = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(insertMovieQuery);
            preparedStatement.setString(1, movieId);
            preparedStatement.setString(2, movieTitle);
            preparedStatement.setInt(3, movieYear);
            preparedStatement.setString(4, directorName);
            System.out.println("Movie Id: "+ movieId + "  movieTitle: "+ movieTitle + "  movieYear: "+ movieYear+ "  directorName: "+ directorName);
            preparedStatement.executeUpdate();
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        }
    }

    private void parseAndInsertActors(String xmlFilePath) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFilePath);

            doc.getDocumentElement().normalize();

            NodeList actorList = doc.getElementsByTagName("actor");

            for (int temp = 0; temp < actorList.getLength(); temp++) {
                Node actorNode = actorList.item(temp);

                if (actorNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element actorElement = (Element) actorNode;

                    insertActorData(actorElement);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertActorData(Element actorElement) {
        try {
            String starName = getTagValue("stagename", actorElement);
            Integer dob;
            if ((getTagValue("dob", actorElement) != null)){
                 dob = Integer.parseInt(getTagValue("dob", actorElement));
            }else{
             dob = null;}

            // Check if the actor already exists in the database
            if (!actorExists(starName)) {
                // Actor does not exist, insert into stars table
                insertStarIntoDatabase(starName, dob);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void parseAndInsertCasts(String xmlFilePath) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFilePath);

            doc.getDocumentElement().normalize();

            NodeList castsList = doc.getElementsByTagName("casts");

            for (int temp = 0; temp < castsList.getLength(); temp++) {
                Node castsNode = castsList.item(temp);

                if (castsNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element castsElement = (Element) castsNode;

                    insertCastsData(castsElement);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertCastsData(Element castsElement) {
        try {
            String movieTitle = getTagValue("t", castsElement);
            String starName = getTagValue("a", castsElement);

            // Check if the star already exists in the database
            String starId = getStarId(starName);

            if (starId == null) {
                // Star does not exist, insert into stars table
                starId = insertStarIntoDatabase(starName, null);
            }

            // Insert into stars_in_movies table
            insertStarInMovie(starId, movieTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void insertGenres(Element filmElement, String movieId) {
        NodeList catsList = filmElement.getElementsByTagName("cats");

        for (int temp = 0; temp < catsList.getLength(); temp++) {
            Node catsNode = catsList.item(temp);

            if (catsNode.getNodeType() == Node.ELEMENT_NODE) {
                Element catsElement = (Element) catsNode;

                NodeList catList = catsElement.getElementsByTagName("cat");

                for (int i = 0; i < catList.getLength(); i++) {
                    Node catNode = catList.item(i);

                    if (catNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element catElement = (Element) catNode;

                        String genreName = catElement.getTextContent().trim();

                        // Check if the genre already exists in the database
                        int genreId = getGenreId(genreName);

                        if (genreId == -1) {
                            // Genre does not exist, insert into genres table
                            genreId = insertGenreIntoDatabase(genreName);
                        }

                        // Insert into genres_in_movies table
                        insertGenreInMovie(genreId, movieId);
                    }
                }
            }
        }
    }


    private String getStarId(String starName) {
        try (Connection conn = MyUtils.getReadConnection()) {
            String selectStarIdQuery = "SELECT id FROM stars WHERE name = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(selectStarIdQuery);
            preparedStatement.setString(1, starName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("id");
            }

            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    private String insertStarIntoDatabase(String starName, Integer dob) {
        try (Connection conn = MyUtils.getReadConnection()) {
            String starId = currentStarId;

            currentStarIdNum = currentStarIdNum + 1;
            currentStarId = "nm" + Integer.toString(currentStarIdNum);

            if (dob == null){
                String insertStarQuery = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, NULL)";
                try (PreparedStatement preparedStatement = conn.prepareStatement(insertStarQuery)) {
                    preparedStatement.setString(1, starId);
                    preparedStatement.setString(2, starName);
                    preparedStatement.executeUpdate();
                }
            }else{
                String insertStarQuery = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
                try (PreparedStatement preparedStatement = conn.prepareStatement(insertStarQuery)) {
                    preparedStatement.setString(1, starId);
                    preparedStatement.setString(2, starName);
                    preparedStatement.setInt(3, dob);
                    preparedStatement.executeUpdate();
                }
            }
            System.out.println("starId: "+ starId + "  starName: "+ starName + "  dob: "+ dob);
            return starId;
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            return null;
        }
    }




    private void insertStarInMovie(String starId, String movieTitle) {
        try (Connection conn = MyUtils.getReadConnection()) {
            // Retrieve movieId based on the provided movieTitle
            String selectMovieIdQuery = "SELECT id FROM movies WHERE title = ?";
            try (PreparedStatement movieIdStatement = conn.prepareStatement(selectMovieIdQuery)) {
                movieIdStatement.setString(1, movieTitle);
                ResultSet movieIdResultSet = movieIdStatement.executeQuery();

                if (movieIdResultSet.next()) {
                    String movieId = movieIdResultSet.getString("id");

                    // Insert into stars_in_movies table
                    String insertStarInMovieQuery = "INSERT INTO stars_in_movies (starId, movieId) VALUES (?, ?)";
                    try (PreparedStatement preparedStatement = conn.prepareStatement(insertStarInMovieQuery)) {
                        preparedStatement.setString(1, starId);
                        preparedStatement.setString(2, movieId);
                        preparedStatement.executeUpdate();
                    }
                } else {
                    System.out.println("Movie not found with title: " + movieTitle);
                }
            }
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        }
    }

    private boolean movieExists(String movieTitle, Integer movieYear, String directorName) {
        try (Connection conn = MyUtils.getReadConnection()) {
            String selectMovieQuery = "SELECT * FROM movies WHERE title = ? AND year = ? AND director = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(selectMovieQuery);
            preparedStatement.setString(1, movieTitle);
            preparedStatement.setInt(2, movieYear);
            preparedStatement.setString(3, directorName);
            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next();
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int getGenreId(String genreName) {
        try (Connection conn = MyUtils.getReadConnection()) {
            String selectGenreIdQuery = "SELECT id FROM genres WHERE name = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(selectGenreIdQuery);
            preparedStatement.setString(1, genreName);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("id");
            }

            return -1;
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private int insertGenreIntoDatabase(String genreName) {
        try (Connection conn = MyUtils.getReadConnection()) {
            int genreId = currentGenreId;
            currentGenreId = currentGenreId + 1;

            String insertGenreQuery = "INSERT INTO genres (id, name) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = conn.prepareStatement(insertGenreQuery)) {
                preparedStatement.setInt(1, genreId);
                preparedStatement.setString(2, genreName);
                preparedStatement.executeUpdate();
            }
            System.out.println("genreId: "+ genreId + "  genreName: "+ genreName);
            return genreId;
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void insertGenreInMovie(int genreId, String movieId) {
        try (Connection conn = MyUtils.getReadConnection()) {
            String insertGenreInMovieQuery = "INSERT INTO genres_in_movies (genreId, movieId) VALUES (?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(insertGenreInMovieQuery);
            preparedStatement.setInt(1, genreId);
            preparedStatement.setString(2, movieId);
            preparedStatement.executeUpdate();
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        }
    }

    private boolean actorExists(String starName) {
        try (Connection conn = MyUtils.getReadConnection()) {
            // Check if the actor already exists in the database
            String query = "SELECT 1 FROM stars WHERE name = ?";
            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, starName);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void closeDatabase() {
        try (Connection conn = MyUtils.getReadConnection()) {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        }
    }
    private String getTagValue(String tagName, Element element) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            if (node.hasChildNodes()) {
                return node.getFirstChild().getNodeValue();
            }
        }
        return null;
    }



    private String generateStarId() {
        try (Connection conn = MyUtils.getReadConnection()) {
            String selectStarIdQuery = "SELECT CONCAT('nm', LPAD(IFNULL(CAST(SUBSTRING(MAX(id), 3) AS SIGNED), 0) + 1, 7, '0')) AS newStarId FROM stars";
            try (PreparedStatement preparedStatement = conn.prepareStatement(selectStarIdQuery)) {
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getString("newStarId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private int generateGenreId() {
        try (Connection conn = MyUtils.getReadConnection()) {
            String selectGenreIdQuery = "SELECT IFNULL(MAX(id), 0) + 1 AS newGenreId FROM genres";
            try (PreparedStatement preparedStatement = conn.prepareStatement(selectGenreIdQuery)) {
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt("newGenreId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }


    private String generateMovieId() {
        try (Connection conn = MyUtils.getReadConnection()) {
            String selectMovieIdQuery = "SELECT CONCAT('tt', LPAD(IFNULL(CAST(SUBSTRING(MAX(id), 3) AS SIGNED), 0) + 1, 7, '0')) AS newMovieId FROM movies";
            try (PreparedStatement preparedStatement = conn.prepareStatement(selectMovieIdQuery)) {
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getString("newMovieId");
                }
            }
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void main(String[] args) throws Exception {
        MovieDatabaseUpdater updater = new MovieDatabaseUpdater();
        updater.init();
        updater.setupData();
        updater.updateDatabase();
    }
}
