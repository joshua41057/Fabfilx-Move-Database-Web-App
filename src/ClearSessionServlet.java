// ClearSessionServlet.java
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/api/clearSession")
public class ClearSessionServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        session.removeAttribute("title");
        session.removeAttribute("year");
        session.removeAttribute("director");
        session.removeAttribute("star");
        session.removeAttribute("genre");
        session.removeAttribute("prefix");
        session.removeAttribute("sort");
        session.removeAttribute("page");
        session.removeAttribute("n");
    }
}
