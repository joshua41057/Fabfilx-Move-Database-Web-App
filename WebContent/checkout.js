// Get the form element with the id "place_order"
let place_order_form = $("#place_order");

// Handle the credit card input data
function handleCreditCardInput(responseData) {
    console.log("Raw response:", responseData);
    console.log("valid_checkout:", responseData.valid_checkout);
    console.log("message:", responseData.message);



    let resultMessage = jQuery("#payment_result_message");

    if (responseData.valid_checkout) {
        resultMessage.text("Payment Successful: " + responseData.message); // Display the success message
    } else {
        resultMessage.text("Payment Failed: " + responseData.message); // Display the failure message
    }
}


// Submit a POST request to process the placed order
function processPlacedOrder(event) {
    console.log("Submit order form");
    event.preventDefault();

    $.ajax("checkout", {
        method: "POST",
        data: place_order_form.serialize(),
        success: handleCreditCardInput
    });
}

// Show the total sales price
function showPrice(resultData) {
    jQuery("#total_sales_price").append("$" + resultData.cart_total_price + ".00");
}

// Attach a submit event handler to the "place_order_form" form
place_order_form.submit(processPlacedOrder);

// Make a GET request to retrieve price information
$.ajax({
    dataType: "json", // Setting return data type
    method: "POST", // Setting request method
    url: "checkout", // Setting request URL, mapped by MoviePageServlet in Stars.java
    success: showPrice // Handle data returned successfully by the MoviePageServlet
});
