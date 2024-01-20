public class CartItem {
    private int movieId;
    private String movieTitle;
    private int quantity;
    private double price;

    public CartItem(int movieId, String movieTitle, int quantity, double price) {
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.quantity = quantity;
        this.price = price;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public void incrementQuantity() {
        quantity++;
    }

    public void decrementQuantity() {
        if (quantity > 0) {
            quantity--;
        }
    }
}