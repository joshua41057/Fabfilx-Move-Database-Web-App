// Function to handle increment, decrement, and delete actions in the cart
function handleCartAction(movieID, typeOfChange) {
    console.log("Submitting cart action: " + typeOfChange);

    $.ajax("cart", {
        method: "POST",
        data: {
            currentMovieId: movieID,
            typeOfChange: typeOfChange
        },
        success: function() {
            if (typeOfChange === "delete") {
                alert("Movie removed from cart");
            }
            location.reload();
        }
    });
}

// Function to populate the cart table
function handleCartInfoResult(resultData) {
    console.log("Populating cart table from resultData");
    let cartTableBodyElement = jQuery("#cart_table_body");
    let totalCartPrice = 0;

    resultData.forEach((item) => {
        const {
            movie_id: movieId,
            movie_title: movieTitle,
            movie_price: moviePrice,
            quantity_of_movie: movieQuantity,
        } = item;

        const totalPriceOfMovieQuantity = movieQuantity * moviePrice;

        let rowHTML = `
            <tr>
                <th>${movieTitle}</th>
                <td>
                    <button type="button" style="float: left" value="-1" onclick="handleCartAction(${movieId}, 'decrement')">-1</button>
                    ${movieQuantity}
                    <button type="button" style="float: right" value="+1" onclick="handleCartAction(${movieId}, 'increment')">+1</button>
                </td>
                <td>
                    <button type="button" value="Delete" onclick="handleCartAction(${movieId}, 'delete')">Delete</button>
                </td>
                <td>$${moviePrice}.00</td>
                <td>$${totalPriceOfMovieQuantity}.00</td>
            </tr>`;

        totalCartPrice += totalPriceOfMovieQuantity;
        cartTableBodyElement.append(rowHTML);
    });

    let totalCartPriceID = jQuery("#total_cart_price");
    let totalCartPriceDiv = `<p><b>Total Cart Price: $${totalCartPrice}.00</b></p>`;
    totalCartPriceID.append(totalCartPriceDiv);
}

// Fetch cart information and populate the cart table
$.ajax({
    dataType: "json",
    method: "GET",
    url: "cart",
    success: (resultData) => handleCartInfoResult(resultData)
});
