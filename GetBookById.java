import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import redis.clients.jedis.Jedis;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

@WebServlet(urlPatterns = "/GetBookById")
public class GetBookById extends HttpServlet {
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://106.12.146.161/linux_final";
    static final String USER = "root";
    static final String PASS = "Sxy123456@";
    static final String SQL_QURERY_Book_BY_ID = "SELECT * FROM t_Book WHERE id=?";
    static final String REDIS_URL = "106.12.146.161";

    static Connection conn = null;
    static Jedis jedis = null;

    public void init() {
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            jedis = new Jedis(REDIS_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        try {
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        //getServletContext().log(request.getParameter("id"));

        String json = jedis.get(request.getParameter("id"));

        if (json == null) {
            Book not = getBook(Integer.parseInt(request.getParameter("id")));

            Gson gson = new Gson();
            json = gson.toJson(not, new TypeToken<Book>() {
            }.getType());

            jedis.set(request.getParameter("id"), json);
            out.println(json);

        } else {
            out.println(json);
        }
        out.flush();
        out.close();
    }

    public Book getBook(int id) {
        Book not = new Book();
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(SQL_QURERY_Book_BY_ID);
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                not.id = rs.getInt("id");
                not.name = rs.getString("name");
		not.author = rs.getString("author");
            }

            rs.close();
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        return not;

    }

}