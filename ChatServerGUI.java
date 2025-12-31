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
        setTitle("Advanced Chat Server - Control Panel");
        setSize(1000, 700);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Add window closing handler
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (serverRunning) {
                    int option = JOptionPane.showConfirmDialog(
                        ChatServerGUI.this,
                        "Server is still running. Do you want to stop it and exit?",
                        "Confirm Exit",
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
        
        // Create main tabbed pane
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
        gbc.gridx = 1;
        JTextField portField = new JTextField("12345", 10);
        portField.setEditable(false);
        configPanel.add(portField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        configPanel.add(new JLabel("Max Clients:"), gbc);
        gbc.gridx = 1;
        JTextField maxClientsField = new JTextField("50", 10);
        configPanel.add(maxClientsField, gbc);
        
        panel.add(configPanel, BorderLayout.CENTER);
        
        // Broadcast panel
        JPanel broadcastPanel = new JPanel(new BorderLayout());
        broadcastPanel.setBorder(BorderFactory.createTitledBorder("Server Broadcast"));
        broadcastField = new JTextField();
        broadcastButton = new JButton("Broadcast");
        broadcastButton.setEnabled(false);
        broadcastButton.addActionListener(e -> sendBroadcast());
        broadcastField.addActionListener(e -> sendBroadcast());
        
        broadcastPanel.add(new JLabel("Message: "), BorderLayout.WEST);
        broadcastPanel.add(broadcastField, BorderLayout.CENTER);
        broadcastPanel.add(broadcastButton, BorderLayout.EAST);
        
        panel.add(broadcastPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createChatMonitorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        chatMonitorArea = new JTextArea();
        chatMonitorArea.setEditable(false);
        chatMonitorArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(chatMonitorArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Live Chat Monitor"));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton clearChatButton = new JButton("Clear Monitor");
        clearChatButton.addActionListener(e -> chatMonitorArea.setText(""));
        
        JCheckBox autoScrollCheckBox = new JCheckBox("Auto Scroll", true);
        
        buttonPanel.add(clearChatButton);
        buttonPanel.add(autoScrollCheckBox);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // User table
        String[] columnNames = {"Username", "IP Address", "Connected Since", "Messages Sent"};
        userTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        userTable = new JTable(userTableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScrollPane = new JScrollPane(userTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Connected Users"));
        
        panel.add(tableScrollPane, BorderLayout.CENTER);
        
        // User management buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton kickUserButton = new JButton("Kick User");
        JButton refreshButton = new JButton("Refresh");
        JButton messageUserButton = new JButton("Message User");
        JButton messageAllButton = new JButton("Message All Users");
        
        kickUserButton.addActionListener(e -> kickSelectedUser());
        refreshButton.addActionListener(e -> refreshUserTable());
        messageUserButton.addActionListener(e -> messageSelectedUser());
        messageAllButton.addActionListener(e -> messageAllUsers());
        
        buttonPanel.add(kickUserButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(messageUserButton);
        buttonPanel.add(messageAllButton);
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
            
            logMessage("Server started successfully on port 12345");
            
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
                                        "Server Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void stopServer() {
        serverRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            // Disconnect all clients gracefully
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    try {
                        writer.println("SERVER_SHUTDOWN");
                        writer.close();
                    } catch (Exception e) {
                        // Ignore individual client errors during shutdown
                    }
                }
                clientWriters.clear();
                userWriters.clear();
                activeUsers.clear();
            }
            
            startStopButton.setText("Start Server");
            broadcastButton.setEnabled(false);
            statusLabel.setText("Server Status: Stopped");
            userCountLabel.setText("Connected Users: 0");
            
            logMessage("Server stopped successfully");
            SwingUtilities.invokeLater(() -> refreshUserTable());
            
        } catch (IOException e) {
            logMessage("Error stopping server: " + e.getMessage());
        }
    }
    
    private void sendBroadcast() {
        String message = broadcastField.getText().trim();
        if (!message.isEmpty()) {
            broadcast("SERVER ANNOUNCEMENT: " + message);
            logMessage("Broadcast sent: " + message);
            logChatMessage("[BROADCAST] SERVER: " + message);
            broadcastField.setText("");
        }
    }
    
    private void kickSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            String username = (String) userTableModel.getValueAt(selectedRow, 0);
            PrintWriter userWriter = userWriters.get(username);
            if (userWriter != null) {
                userWriter.println("KICKED: You have been kicked from the server");
                userWriter.close();
                logMessage("User kicked: " + username);
            }
        }
    }
    
    private void messageSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0) {
            String username = (String) userTableModel.getValueAt(selectedRow, 0);
            
            // Create a more comprehensive message dialog
            JDialog messageDialog = new JDialog(this, "Send Message to " + username, true);
            messageDialog.setSize(400, 250);
            messageDialog.setLocationRelativeTo(this);
            
            JPanel panel = new JPanel(new BorderLayout());
            
            // Message input area
            JTextArea messageArea = new JTextArea(5, 30);
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            messageArea.setBorder(BorderFactory.createLoweredBevelBorder());
            JScrollPane scrollPane = new JScrollPane(messageArea);
            
            // Message type selection
            JPanel typePanel = new JPanel(new FlowLayout());
            JRadioButton privateMsg = new JRadioButton("Private Message", true);
            JRadioButton serverMsg = new JRadioButton("Server Message");
            JRadioButton announcement = new JRadioButton("Server Announcement");
            
            ButtonGroup group = new ButtonGroup();
            group.add(privateMsg);
            group.add(serverMsg);
            group.add(announcement);
            
            typePanel.add(new JLabel("Type: "));
            typePanel.add(privateMsg);
            typePanel.add(serverMsg);
            typePanel.add(announcement);
            
            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton sendBtn = new JButton("Send");
            JButton cancelBtn = new JButton("Cancel");
            
            sendBtn.addActionListener(e -> {
                String message = messageArea.getText().trim();
                if (!message.isEmpty()) {
                    PrintWriter userWriter = userWriters.get(username);
                    if (userWriter != null) {
                        String formattedMessage;
                        if (privateMsg.isSelected()) {
                            formattedMessage = "[PM from SERVER]: " + message;
                        } else if (serverMsg.isSelected()) {
                            formattedMessage = "SERVER MESSAGE: " + message;
                        } else {
                            formattedMessage = "SERVER ANNOUNCEMENT: " + message;
                        }
                        
                        userWriter.println(formattedMessage);
                        userWriter.flush();
                        
                        logMessage("Message sent to " + username + ": " + message);
                        logChatMessage("[SERVER -> " + username + "] " + message);
                        
                        // Show confirmation
                        statusLabel.setText("Message sent to " + username);
                    } else {
                        JOptionPane.showMessageDialog(messageDialog, 
                            "User " + username + " is no longer connected.", 
                            "User Offline", JOptionPane.WARNING_MESSAGE);
                    }
                }
                messageDialog.dispose();
            });
            
            cancelBtn.addActionListener(e -> messageDialog.dispose());
            
            buttonPanel.add(sendBtn);
            buttonPanel.add(cancelBtn);
            
            panel.add(new JLabel("Message to " + username + ":"), BorderLayout.NORTH);
            panel.add(scrollPane, BorderLayout.CENTER);
            
            JPanel southPanel = new JPanel(new BorderLayout());
            southPanel.add(typePanel, BorderLayout.NORTH);
            southPanel.add(buttonPanel, BorderLayout.SOUTH);
            panel.add(southPanel, BorderLayout.SOUTH);
            
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
        
        JDialog messageDialog = new JDialog(this, "Send Message to All Users", true);
        messageDialog.setSize(400, 200);
        messageDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new BorderLayout());
        
        JTextArea messageArea = new JTextArea(4, 30);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton sendBtn = new JButton("Send to All");
        JButton cancelBtn = new JButton("Cancel");
        
        sendBtn.addActionListener(e -> {
            String message = messageArea.getText().trim();
            if (!message.isEmpty()) {
                String formattedMessage = "SERVER ANNOUNCEMENT: " + message;
                broadcast(formattedMessage);
                logMessage("Announcement sent to all users: " + message);
                logChatMessage("[SERVER ANNOUNCEMENT] " + message);
                statusLabel.setText("Announcement sent to " + activeUsers.size() + " users");
            }
            messageDialog.dispose();
        });
        
        cancelBtn.addActionListener(e -> messageDialog.dispose());
        
        buttonPanel.add(sendBtn);
        buttonPanel.add(cancelBtn);
        
        panel.add(new JLabel("Message to all " + activeUsers.size() + " connected users:"), BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        messageDialog.add(panel);
        messageDialog.setVisible(true);
    }
    
    private void refreshUserTable() {
        userTableModel.setRowCount(0);
        synchronized (activeUsers) {
            for (String username : activeUsers) {
                userTableModel.addRow(new Object[]{
                    username, 
                    "127.0.0.1", // Placeholder - would need to track actual IPs
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    "N/A" // Would need to track message counts
                });
            }
        }
        userCountLabel.setText("Connected Users: " + activeUsers.size());
    }
    
    public static void logMessage(String message) {
        if (instance != null && instance.serverLogArea != null) {
            SwingUtilities.invokeLater(() -> {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                instance.serverLogArea.append("[" + timestamp + "] " + message + "\n");
                instance.serverLogArea.setCaretPosition(instance.serverLogArea.getDocument().getLength());
            });
        }
    }
    
    public static void logChatMessage(String message) {
        if (instance != null && instance.chatMonitorArea != null) {
            SwingUtilities.invokeLater(() -> {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                instance.chatMonitorArea.append("[" + timestamp + "] " + message + "\n");
                instance.chatMonitorArea.setCaretPosition(instance.chatMonitorArea.getDocument().getLength());
                instance.refreshUserTable();
            });
        }
    }
    
    private static void broadcast(String message) {
        synchronized (clientWriters) {
            for (PrintWriter writer : new HashSet<>(clientWriters)) {
                try {
                    writer.println(message);
                    writer.flush();
                } catch (Exception e) {
                    clientWriters.remove(writer);
                }
            }
        }
    }
    
    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private String clientIP;

        public ClientHandler(Socket socket) { 
            this.socket = socket; 
            this.clientIP = socket.getInetAddress().getHostAddress();
        }

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
                            logMessage("User authenticated: " + user + " from " + clientIP);
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

                synchronized (clientWriters) { 
                    clientWriters.add(out);
                    userWriters.put(username, out);
                    activeUsers.add(username);
                }

                // Send limited history and separator
                List<String> history = DBManager.getChatHistory();
                for (String msg : history) { 
                    out.println(msg); 
                }
                
                // Add separator to distinguish history from real-time messages
                out.println("SYSTEM: ═══ You are now connected - Real-time messages start here ═══");
                
                broadcast("SYSTEM: " + username + " has joined!");
                logChatMessage("SYSTEM: " + username + " has joined!");
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
                        // Regular chat message - THIS IS THE KEY FIX
                        DBManager.saveMessage(username, message);
                        String fullMessage = username + ": " + message;
                        broadcast(fullMessage);  // Broadcast to ALL clients
                        logChatMessage(fullMessage);
                    }
                }
            } catch (IOException e) {
                logMessage("Client disconnected: " + (username != null ? username : "Unknown") + " (" + clientIP + ")");
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
                    logChatMessage("SYSTEM: " + username + " has left.");
                    sendUserList();
                }
                try {
                    socket.close();
                } catch (IOException e) {}
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
                    logChatMessage("[PM] " + sender + " -> " + recipient + ": " + msg);
                } else {
                    out.println("SYSTEM: User '" + recipient + "' not found or offline.");
                }
            }
        }

        private void sendUserList() {
            String userList = "USERS_ONLINE: " + String.join(", ", activeUsers);
            broadcast(userList);
        }
    }

    public static void main(String[] args) {
        try { 
            UIManager.setLookAndFeel(new FlatDarkLaf()); 
        } catch (Exception e) {
            System.err.println("Failed to set look and feel: " + e.getMessage());
        }
        
        SwingUtilities.invokeLater(() -> {
            new ChatServerGUI().setVisible(true);
        });
    }
}