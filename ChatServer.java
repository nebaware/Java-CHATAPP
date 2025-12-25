import java.io.*;
import java.net.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatServer {
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static Map<String, PrintWriter> userWriters = new HashMap<>();
    private static Set<String> activeUsers = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Chat Server started on port 12345...");
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) { this.socket = socket; }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Protocol: LOGIN|user|pass OR REG|user|pass
                while (true) {
                    String line = in.readLine();
                    if (line == null) return;
                    String[] parts = line.split("\\|");
                    String cmd = parts[0];
                    String user = parts[1];
                    String pass = parts[2];

                    if (cmd.equals("LOGIN")) {
                        if (DBManager.validateLogin(user, pass)) {
                            this.username = user;
                            out.println("AUTH_SUCCESS");
                            break;
                        } else { out.println("AUTH_FAIL"); }
                    } else if (cmd.equals("REG")) {
                        if (DBManager.registerUser(user, pass)) {
                            out.println("REG_SUCCESS");
                        } else { out.println("REG_FAIL"); }
                    }
                }

                synchronized (clientWriters) { 
                    clientWriters.add(out);
                    userWriters.put(username, out);
                    activeUsers.add(username);
                }

                // Send History
                for (String msg : DBManager.getChatHistory()) { out.println(msg); }
                broadcast("SYSTEM: " + username + " has joined!");
                sendUserList();

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/pm ")) {
                        handlePrivateMessage(message, username);
                    } else if (message.equals("/users")) {
                        sendUserList();
                    } else if (message.equals("/time")) {
                        out.println("SYSTEM: Current time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    } else {
                        DBManager.saveMessage(username, message);
                        broadcast(username + ": " + message);
                    }
                }
            } catch (IOException e) {
            } finally {
                if (out != null) {
                    synchronized (clientWriters) { 
                        clientWriters.remove(out);
                        userWriters.remove(username);
                        activeUsers.remove(username);
                    }
                }
                if (username != null) {
                    broadcast("SYSTEM: " + username + " has left.");
                    sendUserList();
                }
            }
        }

        private void broadcast(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }

        private void handlePrivateMessage(String message, String sender) {
            String[] parts = message.split(" ", 3);
            if (parts.length >= 3) {
                String recipient = parts[1];
                String msg = parts[2];
                PrintWriter recipientWriter = userWriters.get(recipient);
                if (recipientWriter != null) {
                    recipientWriter.println("[PM from " + sender + "]: " + msg);
                    out.println("[PM to " + recipient + "]: " + msg);
                } else {
                    out.println("SYSTEM: User '" + recipient + "' not found or offline.");
                }
            }
        }

        private void sendUserList() {
            String userList = "USERS_ONLINE: " + String.join(", ", activeUsers);
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(userList);
                }
            }
        }
    }
}