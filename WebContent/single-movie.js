/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByTitle(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating movie info from resultData");

    // populate the movie info h3
    // find the empty h3 body by id "movie_info"
    let movieInfoElement = jQuery("#movie_info");

    // append two html <p> created to the h3 body, which will refresh the page

    movieInfoElement.append("<p> Title: " + resultData["movie_title"] + "</p>" +
        "<p> release: " + resultData["movie_year"] + "</p>");


    console.log("handleResult: populating movie table from resultData");

    // Populate the movie info table
    // Find the empty table body by id "movie_info_table_body"
    let movieTableBodyElement = jQuery("#movie_info_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    let rowHTML = "";
    rowHTML += "<tr>";
    rowHTML += "<td>" + resultData["movie_director"] + "</td>";

    rowHTML += "<th>";
    for (let j = 0; j < resultData["movie_genres"].length; j++) {
        let genreInfo = resultData["movie_genres"][j];
        let genreId = genreInfo["genre_id"];
        let genreName = genreInfo["genre_name"];
        rowHTML += "<a href='results.html?genre=" + genreId +"&prefix="+ "'>" + jQuery("<div>").text(genreName).html() + "</a>";
        if (j < resultData["movie_genres"].length - 1) {
            rowHTML += ", "; // Add a comma between genres
        }
    }
    rowHTML += "</th>";

    rowHTML += "<td>";
    for (let j = 0; j < resultData["movie_stars"].length; j++) {
        let starInfo = resultData["movie_stars"][j];
        let starId = starInfo["star_id"];
        let starName = starInfo["star_name"];
        rowHTML += '<a href="single-star.html?id=' + starId + '">' + starName + '</a>';
        if (j < resultData["movie_stars"].length - 1) {
            rowHTML += ", "; // Add a comma between stars
        }
    }
    rowHTML += "</td>";

    rowHTML += "<td>" + resultData["movie_rating"] + "</td>";
    rowHTML += "</tr>";

    // Append the row created to the table body, which will refresh the page
    movieTableBodyElement.append(rowHTML);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByTitle('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/single-movie?id=" + movieId,
    success: (resultData) => handleResult(resultData)
});