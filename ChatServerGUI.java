/**
 * Jebena-chatapp - Server GUI with Management Interface
 * Professional server control panel with real-time monitoring and user management
 */
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatServerGUI extends JFrame {
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static Map<String, PrintWriter> userWriters = new HashMap<>();
    private static Set<String> activeUsers = new HashSet<>();
    private static ChatServerGUI instance;
    
    // GUI Components
    private JTextArea serverLogArea;
    private JTextArea chatMonitorArea;
    private DefaultTableModel userTableModel;
    private JTable userTable;
    private JLabel statusLabel;
    private JLabel userCountLabel;
    private JButton startStopButton;
    private JTextField broadcastField;
    private JButton broadcastButton;
    private ServerSocket serverSocket;
    private boolean serverRunning = false;
    
    public ChatServerGUI() {
        instance = this;
        initializeGUI();
    }
    
    private void initializeGUI() {
        setTitle("Jebena-chatapp Server - Control Panel");
        setSize(1000, 700);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Window closing handler
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (serverRunning) {
                    int option = JOptionPane.showConfirmDialog(
                        ChatServerGUI.this,
                        "Jebena-chatapp Server is still running. Do you want to stop it and exit?",
                        "Confirm Exit - Jebena-chatapp",
                        JOptionPane.YES_NO_OPTION
                    );
                    if (option == JOptionPane.YES_OPTION) {
                        stopServer();
                        System.exit(0);
                    }
                } else {
                    System.exit(0);
                }
            }
        });
        
        // Create tabbed interface
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Server Control Tab
        tabbedPane.addTab("Server Control", createServerControlPanel());
        
        // Chat Monitor Tab
        tabbedPane.addTab("Chat Monitor", createChatMonitorPanel());
        
        // User Management Tab
        tabbedPane.addTab("User Management", createUserManagementPanel());
        
        // Server Logs Tab
        tabbedPane.addTab("Server Logs", createServerLogsPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Server Status: Stopped");
        userCountLabel = new JLabel("Connected Users: 0");
        statusBar.add(statusLabel);
        statusBar.add(Box.createHorizontalStrut(20));
        statusBar.add(userCountLabel);
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private JPanel createServerControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Top control panel
        JPanel controlPanel = new JPanel(new FlowLayout());
        startStopButton = new JButton("Start Server");
        startStopButton.addActionListener(e -> toggleServer());
        controlPanel.add(startStopButton);
        
        JButton clearLogsButton = new JButton("Clear Logs");
        clearLogsButton.addActionListener(e -> serverLogArea.setText(""));
        controlPanel.add(clearLogsButton);
        
        panel.add(controlPanel, BorderLayout.NORTH);
        
        // Server configuration panel
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(BorderFactory.createTitledBorder("Server Configuration"));
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        configPanel.add(new JLabel("Port:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField portField = new JTextField("12345");
        portField.setEditable(false);
        configPanel.add(portField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        configPanel.add(new JLabel("Database:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JTextField dbField = new JTextField("chat_db (MySQL 9.5)");
        dbField.setEditable(false);
        configPanel.add(dbField, gbc);
        
        panel.add(configPanel, BorderLayout.CENTER);
        
        // Broadcast panel
        JPanel broadcastPanel = new JPanel(new BorderLayout());
        broadcastPanel.setBorder(BorderFactory.createTitledBorder("Server Broadcast"));
        broadcastField = new JTextField();
        broadcastButton = new JButton("Broadcast");
        broadcastButton.setEnabled(false);
        broadcastButton.addActionListener(e -> broadcastMessage());
        
        JPanel broadcastInputPanel = new JPanel(new BorderLayout());
        broadcastInputPanel.add(broadcastField, BorderLayout.CENTER);
        broadcastInputPanel.add(broadcastButton, BorderLayout.EAST);
        broadcastPanel.add(broadcastInputPanel, BorderLayout.NORTH);
        
        panel.add(broadcastPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createChatMonitorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        chatMonitorArea = new JTextArea();
        chatMonitorArea.setEditable(false);
        chatMonitorArea.setBackground(new Color(40, 44, 52));
        chatMonitorArea.setForeground(Color.CYAN);
        chatMonitorArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(chatMonitorArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Live Chat Monitor - God View"));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Monitor controls
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton clearMonitorButton = new JButton("Clear Monitor");
        clearMonitorButton.addActionListener(e -> chatMonitorArea.setText(""));
        controlPanel.add(clearMonitorButton);
        
        JCheckBox autoScrollCheckBox = new JCheckBox("Auto-scroll", true);
        controlPanel.add(autoScrollCheckBox);
        
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // User table
        String[] columnNames = {"Username", "IP Address", "Connection Time", "Status"};
        userTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        userTable = new JTable(userTableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane tableScrollPane = new JScrollPane(userTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Connected Users"));
        panel.add(tableScrollPane, BorderLayout.CENTER);
        
        // User management buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton messageUserButton = new JButton("Message User");
        JButton kickUserButton = new JButton("Kick User");
        JButton refreshButton = new JButton("Refresh List");
        JButton messageAllButton = new JButton("Message All Users");
        JButton viewUserInfoButton = new JButton("View User Info");
        JButton banUserButton = new JButton("Ban User (Future)");
        
        messageUserButton.addActionListener(e -> messageSelectedUser());
        kickUserButton.addActionListener(e -> kickSelectedUser());
        refreshButton.addActionListener(e -> refreshUserTable());
        messageAllButton.addActionListener(e -> messageAllUsers());
        viewUserInfoButton.addActionListener(e -> viewSelectedUserInfo());
        banUserButton.setEnabled(false); // Future feature
        
        buttonPanel.add(messageUserButton);
        buttonPanel.add(kickUserButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(messageAllButton);
        buttonPanel.add(viewUserInfoButton);
        buttonPanel.add(banUserButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createServerLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        serverLogArea = new JTextArea();
        serverLogArea.setEditable(false);
        serverLogArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scrollPane = new JScrollPane(serverLogArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Server Activity Logs"));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void toggleServer() {
        if (!serverRunning) {
            startServer();
        } else {
            stopServer();
        }
    }
    
    private void startServer() {
        try {
            serverSocket = new ServerSocket(12345);
            serverRunning = true;
            startStopButton.setText("Stop Server");
            broadcastButton.setEnabled(true);
            statusLabel.setText("Server Status: Running on port 12345");
            
            logMessage("Jebena-chatapp server started successfully on port 12345");
            
            // Start accepting clients in a separate thread
            new Thread(() -> {
                while (serverRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        new ClientHandler(clientSocket).start();
                        logMessage("New client connected from: " + clientSocket.getInetAddress());
                    } catch (IOException e) {
                        if (serverRunning) {
                            logMessage("Error accepting client: " + e.getMessage());
                        }
                    }
                }
            }).start();
            
        } catch (IOException e) {
            logMessage("Failed to start server: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to start server: " + e.getMessage(), 
                                        "Jebena-chatapp Server Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void stopServer() {
        serverRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            // Disconnect all clients
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println("SERVER_SHUTDOWN");
                    writer.close();
                }
                clientWriters.clear();
            }
            
            synchronized (userWriters) {
                userWriters.clear();
            }
            
            synchronized (activeUsers) {
                activeUsers.clear();
            }
            
            startStopButton.setText("Start Server");
            broadcastButton.setEnabled(false);
            statusLabel.setText("Server Status: Stopped");
            userCountLabel.setText("Connected Users: 0");
            
            // Clear user table
            SwingUtilities.invokeLater(() -> {
                userTableModel.setRowCount(0);
            });
            
            logMessage("Server stopped successfully");
            
        } catch (IOException e) {
            logMessage("Error stopping server: " + e.getMessage());
        }
    }
    
    private void broadcastMessage() {
        String message = broadcastField.getText().trim();
        if (!message.isEmpty()) {
            String serverAnnouncement = "🔔 SERVER ANNOUNCEMENT: " + message;
            broadcastToAll(serverAnnouncement);
            logMessage("[BROADCAST] " + message);
            chatMonitorArea.append("[" + getCurrentTime() + "] [BROADCAST] " + message + "\n");
            broadcastField.setText("");
        }
    }
    
    private void messageSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow != -1) {
            String username = (String) userTableModel.getValueAt(selectedRow, 0);
            
            // Create message dialog
            JDialog messageDialog = new JDialog(this, "Send Message to " + username, true);
            messageDialog.setSize(400, 200);
            messageDialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new BorderLayout());
            
            // Message type selection
            JPanel typePanel = new JPanel(new FlowLayout());
            JRadioButton privateMsg = new JRadioButton("Private Message", true);
            JRadioButton serverMsg = new JRadioButton("Server Message (Popup)");
            JRadioButton announcement = new JRadioButton("Server Announcement");
            
            ButtonGroup group = new ButtonGroup();
            group.add(privateMsg);
            group.add(serverMsg);
            group.add(announcement);
            
            typePanel.add(privateMsg);
            typePanel.add(serverMsg);
            typePanel.add(announcement);
            panel.add(typePanel, BorderLayout.NORTH);
            
            // Message input
            JTextArea messageArea = new JTextArea(5, 30);
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            panel.add(new JScrollPane(messageArea), BorderLayout.CENTER);
            
            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton sendButton = new JButton("Send");
            JButton cancelButton = new JButton("Cancel");
            
            JLabel statusLabel = new JLabel(" ");
            
            sendButton.addActionListener(e -> {
                String message = messageArea.getText().trim();
                if (!message.isEmpty()) {
                    PrintWriter userWriter = userWriters.get(username);
                    if (userWriter != null) {
                        if (privateMsg.isSelected()) {
                            userWriter.println("[PM from Server]: " + message);
                        } else if (serverMsg.isSelected()) {
                            userWriter.println("🔔 SERVER MESSAGE: " + message);
                        } else {
                            userWriter.println("🔔 SERVER ANNOUNCEMENT: " + message);
                        }
                        
                        logMessage("[SERVER -> " + username + "] " + message);
                        chatMonitorArea.append("[" + getCurrentTime() + "] [SERVER -> " + username + "] " + message + "\n");
                        statusLabel.setText("Message sent to " + username);
                    } else {
                        JOptionPane.showMessageDialog(messageDialog, 
                            "User " + username + " is no longer connected.", 
                            "User Offline", JOptionPane.WARNING_MESSAGE);
                    }
                }
            });
            
            cancelButton.addActionListener(e -> messageDialog.dispose());
            
            buttonPanel.add(sendButton);
            buttonPanel.add(cancelButton);
            buttonPanel.add(statusLabel);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            
            messageDialog.add(panel);
            messageDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user to send a message to.", 
                                        "No User Selected", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void messageAllUsers() {
        if (activeUsers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No users are currently connected.", 
                                        "No Users Online", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String message = JOptionPane.showInputDialog(this, "Enter message for all users:");
        if (message != null && !message.trim().isEmpty()) {
            String serverMessage = "🔔 SERVER MESSAGE: " + message;
            broadcastToAll(serverMessage);
            logMessage("[SERVER -> ALL] " + message);
            chatMonitorArea.append("[" + getCurrentTime() + "] [SERVER -> ALL] " + message + "\n");
        }
    }
    
    private void kickSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow != -1) {
            String username = (String) userTableModel.getValueAt(selectedRow, 0);
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to kick user: " + username + "?", 
                "Confirm Kick", JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                PrintWriter userWriter = userWriters.get(username);
                if (userWriter != null) {
                    userWriter.println("KICKED: You have been kicked by the server administrator.");
                    userWriter.close();
                    
                    // Remove from collections
                    synchronized (userWriters) {
                        userWriters.remove(username);
                    }
                    synchronized (activeUsers) {
                        activeUsers.remove(username);
                    }
                    
                    logMessage("User kicked: " + username);
                    chatMonitorArea.append("[" + getCurrentTime() + "] SYSTEM: " + username + " was kicked by admin\n");
                    refreshUserTable();
                    updateUserCount();
                    broadcastUserList();
                }
            }
        }
    }
    
    private void viewSelectedUserInfo() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow != -1) {
            String username = (String) userTableModel.getValueAt(selectedRow, 0);
            String ip = (String) userTableModel.getValueAt(selectedRow, 1);
            String connectionTime = (String) userTableModel.getValueAt(selectedRow, 2);
            String status = (String) userTableModel.getValueAt(selectedRow, 3);
            
            String info = "Username: " + username + "\n" +
                         "IP Address: " + ip + "\n" +
                         "Connected Since: " + connectionTime + "\n" +
                         "Status: " + status + "\n" +
                         "Messages Sent: [Feature coming soon]";
                         
            JOptionPane.showMessageDialog(this, info, "User Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void refreshUserTable() {
        SwingUtilities.invokeLater(() -> {
            userTableModel.setRowCount(0);
            synchronized (activeUsers) {
                for (String user : activeUsers) {
                    userTableModel.addRow(new Object[]{user, "Connected", getCurrentTime(), "Online"});
                }
            }
        });
    }
    
    private void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = getCurrentTime();
            serverLogArea.append("[" + timestamp + "] " + message + "\n");
            serverLogArea.setCaretPosition(serverLogArea.getDocument().getLength());
        });
    }
    
    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    private void updateUserCount() {
        SwingUtilities.invokeLater(() -> {
            userCountLabel.setText("Connected Users: " + activeUsers.size());
        });
    }
    
    private void broadcastToAll(String message) {
        synchronized (clientWriters) {
            Iterator<PrintWriter> iterator = clientWriters.iterator();
            while (iterator.hasNext()) {
                PrintWriter writer = iterator.next();
                try {
                    writer.println(message);
                    if (writer.checkError()) {
                        iterator.remove();
                    }
                } catch (Exception e) {
                    iterator.remove();
                }
            }
        }
    }
    
    private void broadcastUserList() {
        String userList;
        synchronized (activeUsers) {
            userList = "USERS_ONLINE: " + String.join(", ", activeUsers);
        }
        broadcastToAll(userList);
    }
    
    // Static methods for ClientHandler access
    public static void addMessage(String message) {
        if (instance != null) {
            SwingUtilities.invokeLater(() -> {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                instance.chatMonitorArea.append("[" + timestamp + "] " + message + "\n");
                instance.chatMonitorArea.setCaretPosition(instance.chatMonitorArea.getDocument().getLength());
                instance.refreshUserTable();
            });
        }
    }
    
    public static void addUser(String username, PrintWriter writer) {
        synchronized (activeUsers) {
            activeUsers.add(username);
        }
        synchronized (userWriters) {
            userWriters.put(username, writer);
        }
        synchronized (clientWriters) {
            clientWriters.add(writer);
        }
        
        if (instance != null) {
            instance.updateUserCount();
            instance.broadcastUserList();
            instance.refreshUserTable();
        }
    }
    
    public static void removeUser(String username, PrintWriter writer) {
        synchronized (activeUsers) {
            activeUsers.remove(username);
        }
        synchronized (userWriters) {
            userWriters.remove(username);
        }
        synchronized (clientWriters) {
            clientWriters.remove(writer);
        }
        
        if (instance != null) {
            instance.updateUserCount();
            instance.broadcastUserList();
            instance.refreshUserTable();
        }
    }
    
    public static void broadcastMessage(String message) {
        if (instance != null) {
            instance.broadcastToAll(message);
        }
    }
    
    // ClientHandler class for handling individual client connections
    class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private String clientIP;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.clientIP = socket.getInetAddress().getHostAddress();
        }
        
        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                // Protocol: LOGIN|user|pass OR REG|user|pass
                while (true) {
                    String line = in.readLine();
                    if (line == null) break;
                    
                    String[] parts = line.split("\\|");
                    if (parts.length != 3) continue;
                    
                    String cmd = parts[0];
                    String user = parts[1];
                    String pass = parts[2];
                    
                    if (cmd.equals("LOGIN")) {
                        if (DBManager.validateLogin(user, pass)) {
                            this.username = user;
                            out.println("AUTH_SUCCESS");
                            
                            // Add user to active lists
                            addUser(username, out);
                            
                            logMessage("User logged in: " + user + " from " + clientIP);
                            addMessage("SYSTEM: " + user + " has joined!");
                            
                            // Send chat history (last 5 messages)
                            List<String> history = DBManager.getChatHistory();
                            for (String historyMsg : history) {
                                out.println(historyMsg);
                            }
                            
                            // Send separator
                            out.println("SYSTEM: ═══ You are now connected - Real-time messages start here ═══");
                            
                            // Handle messages
                            handleMessages();
                            break;
                        } else { 
                            out.println("AUTH_FAIL"); 
                            logMessage("Failed login attempt: " + user + " from " + clientIP);
                        }
                    } else if (cmd.equals("REG")) {
                        if (DBManager.registerUser(user, pass)) {
                            out.println("REG_SUCCESS");
                            logMessage("New user registered: " + user + " from " + clientIP);
                        } else {
                            out.println("REG_FAIL");
                            logMessage("Failed registration attempt: " + user + " from " + clientIP);
                        }
                    }
                }
            } catch (IOException e) {
                logMessage("Client handler error: " + e.getMessage());
            } finally {
                cleanup();
            }
        }
        
        private void handleMessages() throws IOException {
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("/pm ")) {
                    // Private message handling
                    String[] parts = message.substring(4).split(" ", 2);
                    if (parts.length == 2) {
                        String targetUser = parts[0];
                        String pmMessage = parts[1];
                        
                        PrintWriter targetWriter = userWriters.get(targetUser);
                        if (targetWriter != null) {
                            targetWriter.println("[PM from " + username + "]: " + pmMessage);
                            out.println("[PM to " + targetUser + "]: " + pmMessage);
                            addMessage("[PM] " + username + " -> " + targetUser + ": " + pmMessage);
                        } else {
                            out.println("SYSTEM: User " + targetUser + " is not online.");
                        }
                    }
                } else if (message.equals("/users")) {
                    // Send user list
                    broadcastUserList();
                } else if (message.equals("/time")) {
                    // Send server time
                    out.println("SYSTEM: Server time is " + getCurrentTime());
                } else {
                    // Regular message
                    DBManager.saveMessage(username, message);
                    String fullMessage = username + ": " + message;
                    addMessage(fullMessage);
                    broadcastMessage(fullMessage);
                }
            }
        }
        
        private void cleanup() {
            try {
                if (username != null) {
                    removeUser(username, out);
                    addMessage("SYSTEM: " + username + " has left!");
                    logMessage("User disconnected: " + username + " from " + clientIP);
                }
                
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                logMessage("Error during cleanup: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new ChatServerGUI().setVisible(true);
        });
    }
}