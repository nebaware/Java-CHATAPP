import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ChatClient extends JFrame {
    private JTextPane chatArea = new JTextPane();
    private JTextField inputField = new JTextField();
    private DefaultListModel<String> userListModel = new DefaultListModel<>();
    private JList<String> userList = new JList<>(userListModel);
    private PrintWriter out;
    private String username;

    public ChatClient() {
        if (!authenticate()) System.exit(0);

        setTitle("Real-time Chat: " + username);
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Styled Chat Area
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(30, 30, 30));
        
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(chatArea), new JScrollPane(userList));
        split.setDividerLocation(600);
        add(split, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(inputField, BorderLayout.CENTER);
        JButton sendBtn = new JButton("Send");
        bottom.add(sendBtn, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        sendBtn.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
    }

    private boolean authenticate() {
        // Standard Login Dialog
        String user = JOptionPane.showInputDialog("Username:");
        String pass = JOptionPane.showInputDialog("Password:");
        try {
            Socket s = new Socket("localhost", 12345);
            out = new PrintWriter(s.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            out.println("LOGIN|" + user + "|" + pass);
            if ("AUTH_SUCCESS".equals(in.readLine())) {
                this.username = user;
                // Start the REAL-TIME listening thread
                new Thread(() -> listen(in)).start();
                return true;
            }
        } catch (Exception e) { return false; }
        return false;
    }

    private void listen(BufferedReader in) {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                if (msg.startsWith("USERS_ONLINE: ")) {
                    updateList(msg.substring(14));
                } else {
                    appendStyled(msg);
                }
            }
        } catch (IOException e) { appendStyled("SYSTEM: Connection lost."); }
    }

    private void updateList(String users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String u : users.split(", ")) userListModel.addElement(u);
        });
    }

    private void appendStyled(String msg) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = chatArea.getStyledDocument();
                SimpleAttributeSet set = new SimpleAttributeSet();
                
                // Real-time Color Coding
                if (msg.startsWith("SYSTEM:")) StyleConstants.setForeground(set, Color.YELLOW);
                else if (msg.startsWith("SERVER")) StyleConstants.setForeground(set, Color.ORANGE);
                else StyleConstants.setForeground(set, Color.WHITE);
                
                doc.insertString(doc.getLength(), msg + "\n", set);
            } catch (Exception e) {}
        });
    }

    private void sendMessage() {
        if (!inputField.getText().trim().isEmpty()) {
            out.println(inputField.getText());
            inputField.setText("");
        }
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        SwingUtilities.invokeLater(() -> new ChatClient().setVisible(true));
    }
}