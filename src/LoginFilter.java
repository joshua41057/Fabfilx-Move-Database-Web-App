import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("LoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        // Redirect to login page if the "user" attribute doesn't exist in session
        if (httpRequest.getSession().getAttribute("user") == null) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        return allowedURIs.stream().anyMatch(requestURI.toLowerCase()::endsWith);
    }

    public void init(FilterConfig fConfig) {
        // Add URLs that are allowed without login (e.g., resources)
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("api/login");
        allowedURIs.add("result.html");
        allowedURIs.add("result.js");
        allowedURIs.add("api/result");
        allowedURIs.add("index.html");
        allowedURIs.add("index.js");
        allowedURIs.add("api/index");
        allowedURIs.add("movies.html");
        allowedURIs.add("movies.js");
        allowedURIs.add("api/movies");
        allowedURIs.add("single-star.html");
        allowedURIs.add("single-star.js");
        allowedURIs.add("api/single-star");
        allowedURIs.add("single-movie.html");
        allowedURIs.add("single-movie.js");
        allowedURIs.add("api/single-movie");

    }

    public void destroy() {
        // ignored.
    }

}
