/**
 * Jebena-chatapp - Enhanced GUI Client
 * A professional multi-client chat application with real-time messaging
 */
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChatClient extends JFrame {
    private JTextPane chatArea = new JTextPane();
    private JTextField inputField = new JTextField();
    private JList<String> userList = new JList<>();
    private DefaultListModel<String> userListModel = new DefaultListModel<>();
    private PrintWriter out;
    private String username;
    private boolean soundEnabled = true;

    public ChatClient() {
        FlatDarkLaf.setup();
        
        if (!authenticate()) System.exit(0);

        setTitle("Jebena-chatapp: " + username);
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initializeGUI();
        connectToServer();
    }

    private boolean authenticate() {
        while (true) {
            // Login or Register dialog
            String[] options = {"Login", "Register", "Cancel"};
            int choice = JOptionPane.showOptionDialog(
                null,
                "Welcome to Jebena-chatapp!\nChoose an option:",
                "Jebena-chatapp Authentication",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );

            if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) {
                return false; // Cancel or close
            }

            // Get credentials
            String user = JOptionPane.showInputDialog("Jebena-chatapp - Username:");
            if (user == null || user.trim().isEmpty()) continue;
            
            String pass = JOptionPane.showInputDialog("Jebena-chatapp - Password:");
            if (pass == null || pass.trim().isEmpty()) continue;

            try {
                Socket s = new Socket("localhost", 12345);
                PrintWriter tempOut = new PrintWriter(s.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

                if (choice == 0) { // Login
                    tempOut.println("LOGIN|" + user + "|" + pass);
                    String response = in.readLine();
                    
                    if ("AUTH_SUCCESS".equals(response)) {
                        this.username = user;
                        this.out = tempOut;
                        new Thread(() -> listenForMessages(in)).start();
                        return true;
                    } else {
                        s.close();
                        JOptionPane.showMessageDialog(null, "Login failed! Invalid username or password.", 
                                                    "Login Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else { // Register
                    tempOut.println("REG|" + user + "|" + pass);
                    String response = in.readLine();
                    
                    if ("REG_SUCCESS".equals(response)) {
                        s.close();
                        JOptionPane.showMessageDialog(null, "Registration successful! Please login with your new account.", 
                                                    "Registration Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        s.close();
                        JOptionPane.showMessageDialog(null, "Registration failed! Username might already exist.", 
                                                    "Registration Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Connection error! Make sure the server is running.", 
                                            "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void initializeGUI() {
        // Main layout
        setLayout(new BorderLayout());

        // Chat area setup
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(40, 44, 52));
        chatArea.setForeground(Color.WHITE);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // User list setup
        userList.setModel(userListModel);
        userList.setBackground(new Color(33, 37, 43));
        userList.setForeground(Color.WHITE);
        userList.setSelectionBackground(new Color(97, 175, 239));
        userList.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(150, 0));
        userScroll.setBorder(BorderFactory.createTitledBorder("Online Users"));

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatScroll, userScroll);
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.8);
        add(splitPane, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        inputField.addActionListener(e -> sendMessage());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        // Emoji button
        JButton emojiBtn = new JButton("😊");
        emojiBtn.setPreferredSize(new Dimension(40, 25));
        emojiBtn.addActionListener(e -> showEmojiPicker());
        
        // Send button
        JButton sendBtn = new JButton("Send");
        sendBtn.setPreferredSize(new Dimension(60, 25));
        sendBtn.addActionListener(e -> sendMessage());
        
        // PM button
        JButton pmBtn = new JButton("PM");
        pmBtn.setPreferredSize(new Dimension(40, 25));
        pmBtn.addActionListener(e -> sendPrivateMessage());
        
        buttonPanel.add(emojiBtn);
        buttonPanel.add(pmBtn);
        buttonPanel.add(sendBtn);
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        JMenu optionsMenu = new JMenu("Options");
        JCheckBoxMenuItem soundItem = new JCheckBoxMenuItem("Sound Notifications", soundEnabled);
        soundItem.addActionListener(e -> soundEnabled = soundItem.isSelected());
        optionsMenu.add(soundItem);
        menuBar.add(optionsMenu);
        setJMenuBar(menuBar);
    }

    private void connectToServer() {
        // Connection is already established in authenticate()
        appendStyled("SYSTEM: ═══ You are now connected - Real-time messages start here ═══", Color.CYAN);
    }

    private void listenForMessages(BufferedReader in) {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                final String msg = message;
                SwingUtilities.invokeLater(() -> {
                    if (msg.startsWith("USERS_ONLINE: ")) {
                        updateUserList(msg.substring(14));
                    } else if (msg.startsWith("🔔 SERVER MESSAGE: ")) {
                        // Server message with popup
                        String serverMsg = msg.substring(19);
                        appendStyled(msg, Color.ORANGE);
                        JOptionPane.showMessageDialog(this, serverMsg, "Server Message", JOptionPane.INFORMATION_MESSAGE);
                        playNotificationSound();
                    } else {
                        appendStyledMessage(msg);
                    }
                });
            }
        } catch (IOException e) { appendStyled("SYSTEM: Jebena-chatapp connection lost.", Color.RED); }
    }

    private void appendStyledMessage(String message) {
        Color color = Color.WHITE;
        
        // Real-time Color Coding
        if (message.startsWith("SYSTEM:")) color = Color.YELLOW;
        else if (message.startsWith("SERVER")) color = Color.ORANGE;
        else if (message.startsWith("[PM from")) color = Color.CYAN;
        else if (message.contains(username + ":")) color = new Color(144, 238, 144); // Light green for own messages
        else color = Color.WHITE;
        
        appendStyled(message, color);
    }

    private void appendStyled(String text, Color color) {
        try {
            StyledDocument doc = chatArea.getStyledDocument();
            SimpleAttributeSet set = new SimpleAttributeSet();
            StyleConstants.setForeground(set, color);
            StyleConstants.setFontFamily(set, "Segoe UI");
            StyleConstants.setFontSize(set, 12);
            
            doc.insertString(doc.getLength(), text + "\n", set);
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void updateUserList(String users) {
        userListModel.clear();
        if (!users.trim().isEmpty()) {
            String[] userArray = users.split(", ");
            for (String user : userArray) {
                userListModel.addElement(user);
            }
        }
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            out.println(message);
            inputField.setText("");
        }
    }

    private void sendPrivateMessage() {
        String selectedUser = userList.getSelectedValue();
        if (selectedUser != null && !selectedUser.equals(username)) {
            String message = JOptionPane.showInputDialog("Private message to " + selectedUser + ":");
            if (message != null && !message.trim().isEmpty()) {
                out.println("/pm " + selectedUser + " " + message);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user to send a private message to.", "No User Selected", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showEmojiPicker() {
        String[] emojis = {"😊", "😂", "😍", "😢", "😡", "👍", "👎", "❤️", "🎉", "🔥", "💯", "🤔", "😎", "🙄", "😴"};
        String selected = (String) JOptionPane.showInputDialog(
            this, "Choose an emoji:", "Emoji Picker",
            JOptionPane.PLAIN_MESSAGE, null, emojis, emojis[0]
        );
        if (selected != null) {
            inputField.setText(inputField.getText() + selected);
            inputField.requestFocus();
        }
    }

    private void playNotificationSound() {
        if (soundEnabled) {
            try {
                Toolkit.getDefaultToolkit().beep();
            } catch (Exception e) {
                // Sound not available
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
            new ChatClient().setVisible(true);
        });
    }
}