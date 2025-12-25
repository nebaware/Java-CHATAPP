# Java Advanced Chat Application

A feature-rich multi-client chat application with user authentication, private messaging, and real-time user tracking.

## 🚀 Features

### Core Features
- **User Authentication** - Secure login and registration system
- **Real-time Messaging** - Instant message delivery to all connected users
- **Message History** - Persistent storage with timestamps (last 50 messages)
- **Multi-client Support** - Multiple users can chat simultaneously
- **Modern Dark UI** - Sleek FlatLaf dark theme interface

### Advanced Features
- **Private Messaging** - Send direct messages to specific users
- **Online User List** - Real-time display of connected users
- **Server Commands** - Built-in commands for enhanced functionality
- **Timestamped Messages** - All messages include date/time stamps
- **User Status Tracking** - Join/leave notifications

### Available Commands
- `/pm <username> <message>` - Send private message
- `/users` - Request current online users list
- `/time` - Get current server time

## 📋 Prerequisites

1. **Java Development Kit (JDK)** - Version 8 or higher
2. **MySQL Server** - Running on localhost:33061
3. **Database Setup** - Import the provided SQL schema

## 🗄️ Database Setup

1. **Start MySQL Server** on port 33061
2. **Import Database Schema:**
   ```sql
   mysql -u root -p < setup_database.sql
   ```
   Or manually execute the SQL commands in `setup_database.sql`

3. **Database Configuration:**
   - Database: `chat_db`
   - Username: `root`
   - Password: `Admin123`
   - Port: `33061`

## 🎮 Running the Application

### Method 1: Using Batch Files (Recommended)

1. **Start the Server:**
   ```cmd
   run_server.bat
   ```
   Wait for "Chat Server started on port 12345..." message

2. **Start Client(s):**
   ```cmd
   run_client.bat
   ```
   You can run multiple instances for testing

### Method 2: Manual Compilation and Execution

1. **Compile All Files:**
   ```cmd
   javac -cp ".;flatlaf-3.7.jar;mysql-connector-j-9.5.0.jar" *.java
   ```

2. **Run Server:**
   ```cmd
   java -cp ".;flatlaf-3.7.jar;mysql-connector-j-9.5.0.jar" ChatServer
   ```

3. **Run Client:**
   ```cmd
   java -cp ".;flatlaf-3.7.jar;mysql-connector-j-9.5.0.jar" ChatClient
   ```

## 👥 Default Test Users

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | Administrator |
| `user1` | `pass123` | Regular User |
| `user2` | `pass123` | Regular User |

## 🎯 How to Use

### Basic Chat
1. **Login** with existing credentials or **register** a new account
2. **Type messages** in the input field and press Enter or click Send
3. **View message history** when joining (last 50 messages with timestamps)

### Private Messaging
1. **Select a user** from the Online Users list
2. **Double-click** the username or click the **PM button**
3. **Type your private message** in the dialog box
4. Messages appear as `[PM from/to username]: message`

### Server Commands
- Type `/users` to refresh the online users list
- Type `/time` to get current server time
- Use `/pm username message` directly in chat

## 🏗️ Architecture

### Server Components
- **ChatServer.java** - Main server handling client connections
- **DBManager.java** - Database operations and connection management
- **Multi-threading** - Each client runs in separate thread

### Client Components
- **ChatClient.java** - GUI client application
- **Authentication Dialog** - Login/registration interface
- **Split-pane Layout** - Chat area and user list

### Database Schema
- **users** table - User credentials and metadata
- **messages** table - Chat history with timestamps

## 🔧 Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| **Connection Error** | Ensure MySQL is running on port 33061 |
| **Compilation Error** | Check if JAR files are present in directory |
| **Server Not Running** | Start server before launching clients |
| **Port Already in Use** | Kill existing process: `netstat -ano \| findstr :12345` |
| **Database Connection Failed** | Verify MySQL credentials and port |
| **Authentication Failed** | Check username/password or register new user |

### Debug Steps
1. **Check MySQL Status:** Ensure database server is running
2. **Verify JAR Files:** Confirm `flatlaf-3.7.jar` and `mysql-connector-j-9.5.0.jar` exist
3. **Test Database:** Try connecting to MySQL manually
4. **Check Ports:** Ensure ports 12345 (server) and 33061 (MySQL) are available

## 📁 Project Structure

```
Java_chatApp/
├── ChatServer.java          # Main server application
├── ChatClient.java          # GUI client application
├── DBManager.java           # Database connection manager
├── setup_database.sql       # Database schema setup
├── run_server.bat          # Server startup script
├── run_client.bat          # Client startup script
├── flatlaf-3.7.jar         # UI theme library
├── mysql-connector-j-9.5.0.jar # MySQL JDBC driver
└── README.md               # This documentation
```

## 🔮 Future Enhancements

- File sharing capabilities
- Emoji support
- Chat rooms/channels
- Message encryption
- User profiles and avatars
- Message search functionality
- Audio/video call integration