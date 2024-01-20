/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating movie table from resultData");

    // Populate the movie table
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Initialize a counter for row numbers
    let rowNumber = 1;

    // Iterate through resultData
    for (let i = 0; i < 20; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";

        // Add row number column
        rowHTML += "<td>" + rowNumber + "</td>";
        rowNumber++;

        rowHTML += '<td><a href="single-movie.html?id=' + resultData[i]["movie_id"] + '">'
            + resultData[i]["movie_title"] + '</a></td>';

        rowHTML += "<td>" + resultData[i]["movie_year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["movie_director"] + "</td>";

        rowHTML += "<td>";
        for (let j = 0; j < Math.min(3, resultData[i]["movie_genres"].length); j++) {
            rowHTML += resultData[i]["movie_genres"][j];
            if (j < Math.min(3, resultData[i]["movie_genres"].length) - 1) {
                rowHTML += ", "; // Add a comma between genres
            }
        }
        rowHTML += "</td>";

        rowHTML += "<td>";
        for (let j = 0; j < Math.min(3, resultData[i]["movie_stars"].length); j++) {
            let starInfo = resultData[i]["movie_stars"][j];
            let starId = starInfo["star_id"];
            let starName = starInfo["star_name"];
            rowHTML += '<a href="single-star.html?id=' + starId + '">' + starName + '</a>';
            if (j < Math.min(3, resultData[i]["movie_stars"].length) - 1) {
                rowHTML += ", "; // Add a comma between stars
            }
        }
        rowHTML += "</td>";

        rowHTML += "<td>" + resultData[i]["movie_rating"] + "</td>";

        rowHTML += "</tr>";
        movieTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleMovieResult
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/movies",
    success:(resultData) => handleMovieResult(resultData)
});