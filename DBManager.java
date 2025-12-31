import java.sql.*;
import java.util.*;

public class DBManager {
    private static final String URL = "jdbc:mysql://127.0.0.1:33061/chat_db?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "Admin123";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static String getConnectionStatus() {
        try (Connection conn = getConnection()) { return "Connected to MySQL 9.5"; }
        catch (SQLException e) { return "Offline: " + e.getMessage(); }
    }

    public static boolean validateLogin(String u, String p) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE username=? AND password=?")) {
            ps.setString(1, u); ps.setString(2, p);
            return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public static boolean registerUser(String u, String p) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO users (username, password) VALUES (?,?)")) {
            ps.setString(1, u); ps.setString(2, p);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { return false; }
    }

    public static void saveMessage(String s, String m) {
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO messages (sender, content) VALUES (?,?)")) {
            ps.setString(1, s); ps.setString(2, m);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<String> getChatHistory() {
        List<String> history = new ArrayList<>();
        try (Connection c = getConnection(); Statement s = c.createStatement(); 
             ResultSet rs = s.executeQuery("SELECT sender, content FROM messages ORDER BY timestamp ASC LIMIT 50")) {
            while (rs.next()) history.add(rs.getString("sender") + ": " + rs.getString("content"));
        } catch (SQLException e) { e.printStackTrace(); }
        return history;
    }
}