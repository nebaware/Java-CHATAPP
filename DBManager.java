import java.sql.*;
import java.util.*;

public class DBManager {
    // Port 33061 for MySQL 9.5
    private static final String URL = "jdbc:mysql://localhost:33061/chat_db?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "Admin123";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // Authenticate user
    public static boolean validateLogin(String u, String p) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u);
            ps.setString(2, p);
            return ps.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    // Register user
    public static boolean registerUser(String u, String p) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u);
            ps.setString(2, p);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { return false; }
    }

    // Save message to MySQL
    public static void saveMessage(String sender, String content) {
        String sql = "INSERT INTO messages (sender, content) VALUES (?, ?)";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sender);
            ps.setString(2, content);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Get rolling history (Last 50)
    public static List<String> getChatHistory() {
        List<String> history = new ArrayList<>();
        // Note: Subquery gets latest 50, outer query sorts them chronologically
        String sql = "SELECT sender, content FROM (SELECT * FROM messages ORDER BY timestamp DESC LIMIT 50) AS sub ORDER BY timestamp ASC";
        try (Connection c = getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                history.add(rs.getString("sender") + ": " + rs.getString("content"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return history;
    }
}