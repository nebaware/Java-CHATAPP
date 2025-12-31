# Jebena-chatapp - Advanced Java Chat Application

A professional multi-client chat application (Jebena-chatapp) with enhanced GUI interfaces, bidirectional server-client communication, user authentication, and comprehensive server management.

## 🚀 Key Features

### Core Functionality
- **Bidirectional Communication** - Complete server-to-client and client-to-server messaging
- **User Authentication** - Secure login and registration system with MySQL backend
- **Real-time Messaging** - Instant message delivery with message history (last 50 messages)
- **Private Messaging** - Direct messages between users and from server to users
- **Multi-client Support** - Handle multiple simultaneous connections

### Enhanced GUI Features
- **Server GUI Management** - Professional server control panel with real-time monitoring
- **Enhanced Client Interface** - Modern dark theme with color-coded messages
- **Server-to-Client Messaging** - Direct messages with popup notifications
- **User Management** - Kick users, send direct messages, broadcast announcements
- **Emoji Support** - Built-in emoji picker for expressive messaging
- **Sound Notifications** - Optional audio alerts for new messages

### Server Administration
- **Real-time Chat Monitoring** - Live view of all chat activity
- **User Management Interface** - View, message, and manage connected users
- **Server Activity Logs** - Comprehensive logging of all server operations
- **Broadcast Messaging** - Send announcements to all connected users
- **Connection Management** - Monitor and control client connections

## 📋 Prerequisites

1. **Java JDK 8+** - Required for compilation and execution
2. **MySQL Server** - Running on localhost:33061
3. **Database Setup** - Import the provided SQL schema

## 🗄️ Database Setup

1. **Start MySQL Server** on port 33061
2. **Create Database and Tables:**
   ```sql
   mysql -u root -p < setup_database.sql
   ```

3. **Database Configuration:**
   - Database: `chat_db`
   - Username: `root`
   - Password: `Admin123`
   - Port: `33061`

## 🎮 Quick Start

### Method 1: Using Batch Files (Easiest)

1. **Start Server GUI:**
   ```cmd
   run_server_gui.bat
   ```
   - Click "Start Server" in the GUI

2. **Start Clients:**
   ```cmd
   run_client.bat
   ```

### Method 2: Manual Commands

1. **Compile:**
   ```cmd
   javac -cp ".;flatlaf-3.7.jar;mysql-connector-j-9.5.0.jar" *.java
   ```

2. **Start Server GUI:**
   ```cmd
   java -cp ".;flatlaf-3.7.jar;mysql-connector-j-9.5.0.jar" --enable-native-access=ALL-UNNAMED ChatServerGUI
   ```

3. **Start Client:**
   ```cmd
   java -cp ".;flatlaf-3.7.jar;mysql-connector-j-9.5.0.jar" --enable-native-access=ALL-UNNAMED ChatClient
   ```

## 👥 Default Test Users

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | Administrator |
| `user1` | `pass123` | Regular User |
| `user2` | `pass123` | Regular User |

## 🎯 How to Use

### Server Administration
1. **Start Server GUI** and click "Start Server"
2. **Monitor Chat** in the "Chat Monitor" tab
3. **Manage Users** in the "User Management" tab:
   - Select user and click "Message User" for direct messaging
   - Choose message type: Private Message, Server Message, or Announcement
   - Use "Message All Users" for server-wide announcements
   - Kick problematic users if needed
4. **View Logs** in the "Server Logs" tab

### Client Usage
1. **Login/Register** using the authentication dialog
2. **Send Messages** - Type and press Enter or click Send
3. **Private Messages** - Double-click users or use PM button
4. **Use Emojis** - Click the 😊 button for emoji picker
5. **Server Messages** - Receive notifications with popup alerts

### Available Commands
- `/pm <username> <message>` - Send private message
- `/users` - Request current online users list
- `/time` - Get current server time

## 🏗️ Architecture

### Core Components
- **ChatServerGUI.java** - Enhanced server with GUI management interface
- **ChatClient.java** - Enhanced client with modern GUI and notifications
- **DBManager.java** - Database operations and connection management

### Message Types
1. **Regular Messages** - `username: message` (color-coded by user)
2. **System Messages** - `SYSTEM: message` (yellow, join/leave notifications)
3. **Private Messages** - `[PM from user]: message` (cyan)
4. **Server Messages** - `SERVER MESSAGE: message` (orange + popup)
5. **Server Announcements** - `SERVER ANNOUNCEMENT: message` (orange, broadcast)

## 🔧 Troubleshooting

### System Check
Run the diagnostic tool to verify your setup:
```cmd
java -cp ".;flatlaf-3.7.jar;mysql-connector-j-9.5.0.jar" DiagnosticTool
```

### Common Issues

| Issue | Solution |
|-------|----------|
| **Connection Error** | Ensure MySQL is running on port 33061 |
| **Compilation Error** | Check if JAR files are present in directory |
| **Server Won't Start** | Verify port 12345 is available |
| **Database Connection Failed** | Check MySQL credentials and port |
| **Authentication Failed** | Use default accounts or register new user |

## 📁 Project Structure

```
Jebena-chatapp/
├── ChatClient.java             # Enhanced GUI client application  
├── ChatServerGUI.java          # GUI server with management interface
├── DBManager.java              # Database connection manager
├── setup_database.sql          # Database schema setup
├── run_server_gui.bat         # GUI server launcher
├── run_client.bat             # Client launcher
├── flatlaf-3.7.jar           # Modern UI theme library
├── mysql-connector-j-9.5.0.jar # MySQL JDBC driver
├── commands.txt              # Quick command reference
└── README.md                 # This documentation
```

## 🎉 Key Improvements

### Bidirectional Communication
- **Before**: Users could only send messages to server
- **After**: Complete server-to-client communication with notifications

### Enhanced Features
- Server can send direct messages to specific users
- Server can broadcast announcements to all users
- Users receive popup notifications for server messages
- Professional server management interface
- Color-coded message system with emojis
- Comprehensive user management tools

## 🚀 Getting Started

1. **Setup Database** - Run `setup_database.sql` in MySQL
2. **Compile** - Use provided batch files or manual commands
3. **Start Server** - Run `run_server_gui.bat` and click "Start Server"
4. **Connect Clients** - Run `run_client.bat` (multiple instances supported)
5. **Test Features** - Try messaging, private messages, and server administration

The application now provides complete bidirectional communication with professional GUI interfaces for both server administration and client usage!