let login_form = $("#login_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataObject
 */
function handleLoginResult(resultDataObject) {
    console.log("handle login response");
    console.log(resultDataObject);

    if (resultDataObject["status"] === "success" && resultDataObject["role"] === "employee") {
        window.location.replace("_dashboard.html");
    } else if (resultDataObject["status"] === "success") {
        window.location.replace("index.html");
    } else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataObject["message"]);
        $("#login_error_message").text(resultDataObject["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the URL defined in the HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax({
        url: "api/login",
        method: "POST",
        data: login_form.serialize(),
        dataType: "json",
        success: handleLoginResult,
        error: function(jqXHR, textStatus, errorThrown) {
            console.error("AJAX Error:", textStatus, errorThrown);
        }
    });
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);

$(document).ready(function () {
    // Check if a new search is initiated by looking for a session marker
    const isNewSearch = sessionStorage.getItem('isNewSearch') === 'true';

    if (isNewSearch) {
        // A new search has been performed
        sessionStorage.setItem('isNewSearch', 'false'); // Reset the marker
        // Process the new search here
    } else {
        // No new search has been initiated, load data from the session
        const lastSearchQuery = sessionStorage.getItem('lastSearchQuery');

        if (lastSearchQuery) {
            // Fill the search form with the last search query
            const searchForm = $('#search-form');
            searchForm.find('#title').val('');
            searchForm.find('#year').val('');
            searchForm.find('#director').val('');
            searchForm.find('#starName').val('');
            searchForm.find('#title').val(lastSearchQuery);
        }

        // Rest of your movies.js code...
    }
});

// When a new search is initiated, set the session marker and store the new search query
function initiateNewSearch(query) {
    sessionStorage.setItem('isNewSearch', 'true');
    sessionStorage.setItem('lastSearchQuery', query);
    // Redirect the user to the search results or do other processing as needed
}