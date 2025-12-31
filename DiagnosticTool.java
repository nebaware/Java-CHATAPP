import java.sql.Connection;

public class DiagnosticTool {
    public static void main(String[] args) {
        System.out.println("--- Chat App Diagnostic ---");
        try {
            Connection conn = DBManager.getConnection();
            if (conn != null) {
                System.out.println("[SUCCESS] Connected to MySQL 9.5 on port 33061.");
                System.out.println("[SUCCESS] Database 'chat_db' is ready.");
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Could not connect to Database!");
            System.err.println("Details: " + e.getMessage());
            System.err.println("\nChecklist:");
            System.err.println("1. Is MySQL 9.5 Service Running?");
            System.err.println("2. Is the port 33061 open?");
            System.err.println("3. Is the password 'Admin123' correct?");
        }
    }
}