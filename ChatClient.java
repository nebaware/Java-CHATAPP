import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClient extends JFrame {
    private JTextArea chatArea = new JTextArea();
    private JTextField inputField = new JTextField();
    private JList<String> userList = new JList<>();
    private DefaultListModel<String> userListModel = new DefaultListModel<>();
    private PrintWriter out;
    private String username;

    public ChatClient() {
        if (!authenticate()) System.exit(0);

        setTitle("Advanced Chat - " + username);
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        chatArea.setEditable(false);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        // Main chat area
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setPreferredSize(new Dimension(600, 400));
        
        // User list panel
        userList.setModel(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setPreferredSize(new Dimension(180, 400));
        userScroll.setBorder(BorderFactory.createTitledBorder("Online Users"));
        
        // Split pane for chat and users
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatScroll, userScroll);
        splitPane.setDividerLocation(600);
        add(splitPane, BorderLayout.CENTER);

        // Bottom panel with input and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(inputField, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton sendBtn = new JButton("Send");
        JButton pmBtn = new JButton("PM");
        JButton usersBtn = new JButton("Users");
        JButton timeBtn = new JButton("Time");
        
        buttonPanel.add(sendBtn);
        buttonPanel.add(pmBtn);
        buttonPanel.add(usersBtn);
        buttonPanel.add(timeBtn);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        sendBtn.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
        pmBtn.addActionListener(e -> sendPrivateMessage());
        usersBtn.addActionListener(e -> requestUserList());
        timeBtn.addActionListener(e -> requestTime());
        
        // Double-click user for PM
        userList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) sendPrivateMessage();
            }
        });

        connectToServer();
    }

    private boolean authenticate() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        panel.add(new JLabel("Username:")); panel.add(userField);
        panel.add(new JLabel("Password:")); panel.add(passField);

        Object[] options = {"Login", "Register", "Exit"};
        int res = JOptionPane.showOptionDialog(null, panel, "Welcome",
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (res == 2 || res == -1) return false;

        String cmd = (res == 0) ? "LOGIN" : "REG";
        try {
            Socket s = new Socket("localhost", 12345);
            PrintWriter tempOut = new PrintWriter(s.getOutputStream(), true);
            BufferedReader tempIn = new BufferedReader(new InputStreamReader(s.getInputStream()));

            tempOut.println(cmd + "|" + userField.getText() + "|" + new String(passField.getPassword()));
            String response = tempIn.readLine();

            if (response.equals("AUTH_SUCCESS")) {
                this.username = userField.getText();
                this.out = tempOut;
                new Thread(() -> listen(tempIn)).start();
                return true;
            } else if (response.equals("REG_SUCCESS")) {
                JOptionPane.showMessageDialog(null, "Account Created! Please Login.");
                return authenticate();
            } else {
                JOptionPane.showMessageDialog(null, "Error: " + response);
                return authenticate();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Server not running.");
            return false;
        }
    }

    private void connectToServer() { /* Already handled in authenticate */ }

    private void listen(BufferedReader in) {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                if (msg.startsWith("USERS_ONLINE: ")) {
                    updateUserList(msg.substring(14));
                } else {
                    chatArea.append(msg + "\n");
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                }
            }
        } catch (IOException e) { chatArea.append("Connection Lost."); }
    }

    private void sendMessage() {
        if (!inputField.getText().trim().isEmpty()) {
            out.println(inputField.getText());
            inputField.setText("");
        }
    }
    
    private void sendPrivateMessage() {
        String selectedUser = userList.getSelectedValue();
        if (selectedUser != null && !selectedUser.equals(username)) {
            String message = JOptionPane.showInputDialog(this, "Private message to " + selectedUser + ":");
            if (message != null && !message.trim().isEmpty()) {
                out.println("/pm " + selectedUser + " " + message);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user to send a private message.");
        }
    }
    
    private void requestUserList() {
        out.println("/users");
    }
    
    private void requestTime() {
        out.println("/time");
    }
    
    private void updateUserList(String users) {
        userListModel.clear();
        if (!users.trim().isEmpty()) {
            for (String user : users.split(", ")) {
                userListModel.addElement(user);
            }
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(new FlatDarkLaf()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new ChatClient().setVisible(true));
    }
}