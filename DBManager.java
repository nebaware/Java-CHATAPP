import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private static final String URL = "jdbc:mysql://127.0.0.1:33061/chat_db?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASS = "Admin123";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static boolean validateLogin(String user, String pass) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user);
            pstmt.setString(2, pass);
            return pstmt.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public static boolean registerUser(String user, String pass) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user);
            pstmt.setString(2, pass);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) { return false; }
    }

    public static void saveMessage(String sender, String content) {
        String sql = "INSERT INTO messages (sender, content) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sender);
            pstmt.setString(2, content);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<String> getChatHistory() {
        List<String> history = new ArrayList<>();
        String sql = "SELECT sender, content, timestamp FROM messages ORDER BY timestamp DESC LIMIT 50";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String timestamp = rs.getTimestamp("timestamp").toString().substring(0, 19);
                history.add(0, "[" + timestamp + "] " + rs.getString("sender") + ": " + rs.getString("content"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return history;
    }
}