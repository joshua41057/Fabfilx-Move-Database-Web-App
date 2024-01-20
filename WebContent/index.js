// Define variables to store user input data
let titleInput, yearInput, directorInput, starInput;

let search_form = jQuery("#search");

// Define variables
let searchCache = {}; // Cache for storing autocomplete suggestions
let debounceTimer; // Timer for debouncing

// Select the .full-search-bar element once and store it in a variable
let fullSearchBar = $('.full-search-bar');

// Autocomplete initialization
fullSearchBar.autocomplete({
    lookup: function (query, doneCallback) {
        handleAutocomplete(query, doneCallback);
    },
    onSelect: function (suggestion) {
        handleSelectSuggestion(suggestion);
    },
    deferRequestBy: 300,
    minChars: 3,  // Minimum characters before triggering autocomplete
});

// Autocomplete search handler
function handleAutocomplete(query, doneCallback) {
    console.log("Autocomplete initiated");

    // Check if the query is in the cache
    if (searchCache[query]) {
        console.log("Using cached results for query:", query);
        doneCallback({ suggestions: searchCache[query] });
    } else {
        console.log("Sending AJAX request to backend for query:", query);

        // Sending the HTTP GET request to the backend server
        jQuery.ajax({
            method: "GET",
            url: "movie-autocomplete?query=" + encodeURIComponent(query),
            success: function (data) {
                handleAutocompleteAjaxSuccess(data, query, doneCallback);
            },
            error: function (errorData) {
                console.log("Autocomplete AJAX error");
                console.log(errorData);
            }
        });
    }
}

// Autocomplete success callback
function handleAutocompleteAjaxSuccess(data, query, doneCallback) {
    console.log("Autocomplete AJAX successful");

    try {
        // Log the received data for debugging
        console.log("Received data:", data);

        // If the data is an array, directly use it
        let jsonData = Array.isArray(data) ? data : JSON.parse(data);

        // Log the parsed JSON for debugging
        console.log("Autocomplete Suggestions:", jsonData);

        // Cache the suggestions for the query
        searchCache[query] = jsonData;

        // Call the callback function provided by the autocomplete library
        // Add "{ suggestions: jsonData }" to satisfy the library response format
        doneCallback({ suggestions: jsonData });
    } catch (error) {
        console.error("Error parsing JSON response:", error);

        // Ensure that the autocomplete library receives a valid response
        doneCallback({ suggestions: [] });
    }
}

function handleSelectSuggestion(suggestion) {
    console.log("You selected: " + suggestion["value"]);

    // Get the movieId from the suggestion
    let movieId = suggestion["id"];

    // Optionally, redirect to the Single Movie Page using movieId
    window.location.replace("single-movie.html?id=" + encodeURIComponent(movieId));
}






/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleSearchResult(resultDataString) {
    console.log("Response Data:", resultDataString);
    let resultDataJson = resultDataString;

    console.log("Handling search response");
    console.log(resultDataJson);

    if (resultDataJson && resultDataJson.length > 0) {
        // There are search results, so redirect to results.html using stored user input data
        console.log("Redirecting to results.html");
        window.location.replace(constructSearchRedirectURL(titleInput, yearInput, directorInput, starInput));
    } else {
        console.log("No search results, nothing is going to happen");
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */


function submitSearchForm(formSubmitEvent) {
    console.log("submit search form on main page");

    // Capture user input and store it in variables
    titleInput = jQuery("input[name='title']").val();
    yearInput = jQuery("input[name='year']").val();
    directorInput = jQuery("input[name='director']").val();
    starInput = jQuery("input[name='star']").val();

    // Prevent the default form submission
    formSubmitEvent.preventDefault();

    jQuery.ajax(
        "api/results", {
            method: "POST",
            data: search_form.serialize(),
            success: handleSearchResult
        }
    );
}


/**
* Dynamically generate genre links
* @param genresArray Array of genre objects {id, name}
*/
function generateGenreLinks(genresArray) {
    let genreLinksContainer = jQuery("#browse_genre_links");

    // Clear existing genre links
    genreLinksContainer.empty();

    // Generate genre links dynamically
    genresArray.forEach((genre, index) => {
        let genreLink = `<a href="results.html?genre=${genre.id}&prefix=">[${index + 1}. ${genre.name}]</a>`;
        genreLinksContainer.append(genreLink);
    });
}


// Fetch genres from the server on page load
jQuery(document).ready(function () {
    jQuery.ajax({
        url: "api/index", // Update the URL to the correct endpoint
        method: "GET",
        success: function (data) {
            console.log("Server response:", data);
            // Assuming the server returns genres as an array
            generateGenreLinks(data);
        },
        error: function (error) {
            console.error("Error fetching genres:", error);
        }
    });
});



function constructSearchRedirectURL(title, year, director, star)
{
    return "results.html?title=" +
        title + "&" +
        "year=" + year + "&" +
        "director=" + director + "&" +
        "star=" + star;
}


// Bind the submit action of the form to a handler function
search_form.submit(submitSearchForm);