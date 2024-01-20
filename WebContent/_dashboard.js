// Wait for the DOM to be ready before executing any code
$(document).ready(function () {

    // Add event listener for adding a new star
    $("#add_star_form").submit(function (event) {
        event.preventDefault();
        // Handle form submission for adding a new star
        const starName = $("#starName").val();
        const birthYear = $("#birthYear").val();

        // Example: Call a function to add a new star via AJAX
        addNewStar(starName, birthYear);
    });

    // Add event listener for adding a new movie
    $("#add_movie_form").submit(function (event) {
        event.preventDefault();
        // Handle form submission for adding a new movie
        const movieTitle = $("#movieTitle").val();
        const movieYear = $("#movieYear").val();
        const movieDirector = $("#movieDirector").val();
        const starName = $("#starNameMovie").val();
        const genreName = $("#genreName").val();

        // Example: Call a function to add a new movie via AJAX
        addNewMovie(movieTitle, movieYear, movieDirector, starName, genreName);
    });

    // Function to add a new star via AJAX
    function addNewStar(starName, birthYear) {
        // Your AJAX logic for adding a new star goes here
        $.ajax({
            type: "POST",
            url: "/cs122b_project1_glhf_war/_dashboard",
            data: {
                action: "addStar",
                starName: starName,
                birthYear: birthYear
            },
            success: function (response) {
                // Handle success response
                console.log(response);
                if (response.includes("successfully")) {
                    alert("Star added successfully");
                } else {
                    // Display the error message
                    alert(response);
                }
            },
            error: function (error) {
                console.log(error);
                // Handle error response, e.g., show an error message
                alert("Error adding star");
            }
        });
    }

    // Function to add a new movie via AJAX
    function addNewMovie(movieTitle, movieYear, movieDirector, starName, genreName) {
        $.ajax({
            type: "POST",
            url: "/cs122b_project1_glhf_war/_dashboard",
            data: {
                action: "addMovie",
                movieTitle: movieTitle,
                movieYear: movieYear,
                movieDirector: movieDirector,
                starName: starName,
                genreName: genreName
            },
            success: function (response) {
                // Handle success response
                console.log(response);
                if (response.includes("successfully")) {
                    alert("Movie added successfully");
                } else {
                    // Display the error message
                    alert(response);
                }
            },
            error: function (error) {
                // Handle error response
                console.error(error);
                alert("Error adding movie");
            }
        });
    }
});
