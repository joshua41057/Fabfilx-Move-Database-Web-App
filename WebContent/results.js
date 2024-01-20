function getParameterByName(target) {
    let url = window.location.href;
    target = target.replace(/[\[\]]/g, "\\$&");
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)");
    let results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}


// Remove this line if it's already in your code
const pageDisplay = jQuery("#page_number_display");

// Initialize the current page number
let currentPage = 1;

// Function to update the current page
function updateCurrentPage(newPage) {
    currentPage = newPage;
}

// Function to get the current page
function getCurrentPage() {
    return currentPage;
}



// Function to handle navigation based on URL parameters
function handleNavigation() {
    const currentURL = new URL(window.location.href);
    const page = currentURL.searchParams.get("page") || 1;
    const title = currentURL.searchParams.get("title") || "";
    const year = currentURL.searchParams.get("year") || "";
    const director = currentURL.searchParams.get("director") || "";
    const star = currentURL.searchParams.get("star") || "";
    const sortIndex = currentURL.searchParams.get("sort") || "0";
    const numResultsPerPage = currentURL.searchParams.get("n") || "10";
    const genre = currentURL.searchParams.get("genre");
    const prefix = currentURL.searchParams.get("prefix");

// Create an object to store the parameters
    const queryParams = {
        title,
        year,
        director,
        star,
        sort: sortIndex,
        n: numResultsPerPage,
        genre,
        prefix
    };

// Filter out parameters with null values
    const filteredParams = {};
    for (const key in queryParams) {
        if (queryParams[key] !== null) {
            filteredParams[key] = queryParams[key];
        }
    }

// Construct the API URL with the filtered parameters
    const apiURL = `api/results?${Object.entries(filteredParams)
        .map(([key, value]) => `${key}=${value}`)
        .join("&")}`;

// Update the page content based on URL parameters
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: apiURL,
        success: handleResultData,
        error: function(xhr, status, error) {
            console.error("AJAX request failed:", error);
            // Add error handling logic here
        }
    });
    // Update your page display based on the page parameter
    updatePageNumberDisplay(page);
}

// Listen for URL changes (forward/backward navigation)
window.addEventListener("popstate", handleNavigation);

// Function to navigate to a new page
function navigateToPage(newPage) {
    const currentURL = new URL(window.location.href);
    currentURL.searchParams.set("page", newPage.toString());
    const newURL = currentURL.toString();
    window.history.pushState({ path: newURL }, "", newURL);
    updateCurrentPage(newPage); // Update the current page
    updatePageNumberDisplay(newPage);
}

// Update your "Previous" and "Next" click handlers
jQuery("#previous_page").on("click", function() {
    const currentPage = getCurrentPage();
    if (currentPage > 1) {
        navigateToPage(currentPage - 1);
    }
});


jQuery("#next_page").on("click", function() {
    const currentPage = getCurrentPage();
    // Add your logic to determine if there are more pages
    // For example, you could check if there are more results to fetch
    const morePagesExist = true; // Replace this with your logic
    if (morePagesExist) {
        navigateToPage(currentPage + 1);
    }
});

// Function to update the page number display
function updatePageNumberDisplay(newPage) {
    pageDisplay.val(newPage);
}
function handleResultData(resultData) {
    console.log("Populating movie page table from results.js");

    let moviesTableBodyElement = jQuery("#results_table_body");
    let movieResults = resultData; // The JSON structure should match your servlet response
    updatePageNumberDisplay(resultData["page_number"]);

    if (movieResults.length === 0) {
        jQuery("#results_text").append("No Results Found");
    } else {
        jQuery("#results_text").append("Results");
    }

    for (let i = 0; i < movieResults.length; i++) {
        const movie = movieResults[i];

        // Create HTML for each row in the table
        let rowHTML = "<tr>";
        rowHTML += "<th><a href='single-movie.html?id=" + movie["movie_id"] + "'>" +
            jQuery("<div>").text(movie["movie_title"]).html() + "</a></th>";
        rowHTML += "<th>" + movie["movie_year"] + "</th>";
        rowHTML += "<td>" + jQuery("<div>").text(movie["movie_director"]).html() + "</td>";

        rowHTML += "<th>";
        for (let j = 0; j < Math.min(3, movie["movie_genres"].length); j++) {
            let genreInfo = movie["movie_genres"][j];
            let genreId = genreInfo["genre_id"];
            let genreName = genreInfo["genre_name"];
            rowHTML += "<a href='results.html?genre=" + genreId +"&prefix="+ "'>" + jQuery("<div>").text(genreName).html() + "</a>";
            if (j < Math.min(3, movie["movie_genres"].length) - 1) {
                rowHTML += ", "; // Add a comma between genres
            }
        }
        rowHTML += "</th>";

        rowHTML += "<th>";
        for (let j = 0; j < Math.min(3, movie["movie_stars"].length); j++) {
            let starInfo = movie["movie_stars"][j];
            let starId = starInfo["star_id"];
            let starName = starInfo["star_name"];
            rowHTML += '<a href="single-star.html?id=' + starId + '">' + jQuery("<div>").text(starName).html() + '</a>';
            if (j < Math.min(3, movie["movie_stars"].length) - 1) {
                rowHTML += ", "; // Add a comma between stars
            }
        }
        rowHTML += "</th>";

        rowHTML += "<td>" + movie["movie_rating"] + "</td>";
        rowHTML += "<td>" + writeGetToCartButtonHTML(movie["movie_id"]) + "</td>";
        rowHTML += "</tr>";

        // Append the row HTML to the table body
        moviesTableBodyElement.append(rowHTML);
    }

    jQuery("#page_number_display").val(resultData["page_number"]);
}

function writeGetToCartButtonHTML(movieId) {
    return "<button id='add_to_cart' type='submit' " + "movie_id='" + movieId + "' class='add_to_cart_button'>" +
        "Add To Cart</button>";
}

function submitSearchForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();

    clearPageContent();

    const sortingOption = jQuery("#sorting_option").val();
    const numResultsPerPage = jQuery("#num_results_per_page").val();

    const title = jQuery("#title").val();
    const year = jQuery("#year").val();
    const director = jQuery("#director").val();
    const star = jQuery("#star").val();

    const searchURL = `api/results?title=${title}&year=${year}&director=${director}&star=${star}&sort=${sortingOption}&n=${numResultsPerPage}&page=1`;

    jQuery.ajax({
        method: "GET",
        url: searchURL,
        success: handleResultData
    });
}

function handleAjaxError(error) {
    console.error("AJAX request failed:", error);
    // Add error handling logic here
}

function constructGETSearchURI(title, year, director, star) {
    return `api/results?title=${title}&year=${year}&director=${director}&star=${star}`;
}

function constructGETBrowseURI(genreId, prefixChar) {
    return `api/results?genre=${genreId}&prefix=${prefixChar}`;
}


function constructAPIURL(currentURL, sortOption, numResultsPerPage) {
    const queryParams = {
        title: currentURL.searchParams.get("title"),
        year: currentURL.searchParams.get("year"),
        director: currentURL.searchParams.get("director"),
        star: currentURL.searchParams.get("star"),
        sort: sortOption,
        n: numResultsPerPage,
        genre: currentURL.searchParams.get("genre"),
        prefix: currentURL.searchParams.get("prefix")
    };

    const filteredParams = {};
    for (const key in queryParams) {
        if (queryParams[key] !== null) {
            filteredParams[key] = queryParams[key];
        }
    }

    return `api/results?${Object.entries(filteredParams)
        .map(([key, value]) => `${key}=${value}`)
        .join("&")}`;
}

function submitSortForm(sortSubmitEvent) {
    sortSubmitEvent.preventDefault();
    const sortOption = jQuery("#sorting_option").val();
    const numResultsPerPage = jQuery("#num_results_per_page").val();
    const currentURL = new URL(window.location.href);
    const currentPage = currentURL.searchParams.get("page") || 1;

    // Construct the API URL
    const apiURL = constructAPIURL(currentURL, sortOption, numResultsPerPage);

    // Update the URL with the new sorting option
    currentURL.searchParams.set("sort", sortOption);
    const newURL = currentURL.toString();
    window.history.pushState({ path: newURL }, "", newURL);

    // Clear the existing content
    clearPageContent();

    // Load the new content with the selected sorting option
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: apiURL,
        success: function (resultData) {
            handleResultData(resultData);
            updatePageNumberDisplay(currentPage);
        },
        error: handleAjaxError
    });
}


function clearPageContent() {
    // Replace this with your logic to clear the existing content on the page.
    // For example, you can empty the results table or hide the old content.
    jQuery("#results_table_body").empty();
    jQuery("#results_text").empty()
}

function submitPageChange(pageChange) {
    const currentURL = new URL(window.location.href);
    const numResultsPerPage = jQuery("#num_results_per_page").val();

    // Extract the parameters from the URL
    const queryParams = {
        title: currentURL.searchParams.get("title") || "",
        year: currentURL.searchParams.get("year") || "",
        director: currentURL.searchParams.get("director") || "",
        star: currentURL.searchParams.get("star") || "",
        sort: currentURL.searchParams.get("sort") || "0",
        page: parseInt(currentURL.searchParams.get("page")) || 1,
        genre: currentURL.searchParams.get("genre") || "",
        prefix: currentURL.searchParams.get("prefix") || ""
    };

    // Update the page based on the page change
    queryParams.page += pageChange;

    // Ensure that the page number is within a valid range
    queryParams.page = Math.max(queryParams.page, 1);

    // Update the URL with the new page number
    currentURL.searchParams.set("page", queryParams.page.toString());
    const newURL = currentURL.toString();
    window.history.pushState({ path: newURL }, "", newURL);

    // Clear the existing content
    clearPageContent();

    // Store the updated page number in the session and update the page number display
    pageDisplay.val(queryParams.page);

    // Construct the API URL with the updated parameters
    const apiURL = `api/results?${Object.entries(queryParams)
        .filter(([key, value]) => value !== "")
        .map(([key, value]) => `${key}=${value}`)
        .join("&")}`;

    // Load the new content for the page
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: apiURL,
        success: function (resultData) {
            handleResultData(resultData);
            updatePageNumberDisplay(queryParams.page); // Update the displayed page number
        },
        error: handleAjaxError // Handle AJAX errors
    });
}



jQuery("#results_table_body").on("click", ".add_to_cart_button", function() {
    let movieId = jQuery(this).attr("movie_id");
    console.log(movieId); // Use console.log() to print to the browser's console

    jQuery.ajax(
        "cart", {
            method: "POST",
            data: { currentMovieId: movieId, typeOfChange: "add" },
            success: function() {
                alert("Successfully Added To Cart")
            },
            error: handleAjaxError // Handle AJAX errors
        }
    );
});

let sort_form = jQuery("#sort_form");
sort_form.submit(submitSortForm);

// Check for parameters
let sortIndex = getParameterByName("sort");
let numResultsPerPage = getParameterByName("n");
let newPageNum = getParameterByName("page");

// Set the sorting and pagination values based on URL parameters
if (sortIndex !== null) {
    jQuery("#sorting_option").val(parseInt(sortIndex));
}
if (numResultsPerPage !== null) {
    jQuery("#num_results_per_page").val(parseInt(numResultsPerPage));
}
if (newPageNum !== null) {
    jQuery("#page_number_display").val(parseInt(newPageNum));
}

let search_form = jQuery("#search");
search_form.submit(submitSearchForm);

let title = getParameterByName("title");
let year = getParameterByName("year");
let director = getParameterByName("director");
let star = getParameterByName("star");

let genre = getParameterByName("genre");
let prefixChar = getParameterByName("prefix");

let getRequestURL = "";

if (title != null || year != null || director != null || star != null) {
    getRequestURL = constructGETSearchURI(title, year, director, star);
} else if (genre != null || prefixChar != null) {
    getRequestURL = constructGETBrowseURI(genre, prefixChar);
} else if (sortIndex !== null && numResultsPerPage !== null) {
    getRequestURL = `api/results?sort=${sortIndex}&n=${numResultsPerPage}`;
} else if (newPageNum !== null) {
    getRequestURL = `api/results?page=${newPageNum}`;
} else {
    getRequestURL = "api/results";
}

// Now make the AJAX request
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: getRequestURL,
    success: handleResultData
});
