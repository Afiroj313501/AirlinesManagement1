# Airlines Management Chat System

This document explains how to use the chat system that has been fixed and improved.

## Overview

The chat system consists of:
1. **ChatServer** - The main server that handles all chat communications
2. **SimpleUser** - A command-line client for regular users
3. **AdminChatController** - A JavaFX-based admin interface

## Fixed Issues

The following issues have been resolved:

1. **Port Mismatch**: All components now use port 5000 (ChatServer default)
2. **Protocol Handling**: Proper name submission protocol implementation
3. **Message Format**: Correct handling of private messages and broadcasts
4. **Error Handling**: Better error messages and connection handling
5. **User Experience**: Improved console output and instructions

## How to Start the Chat System

### Step 1: Compile the Java Files

```bash
javac -cp . src/main/java/com/example/airlinesmanagement1/*.java
```

### Step 2: Start the Chat Server

```bash
java -cp src/main/java com.example.airlinesmanagement1.ChatServer
```

You should see output like:
```
Chat Server is starting on port 5000
Chat Server is running and accepting connections on port 5000
Server is ready to accept client connections.
```

### Step 3: Start User Clients

In a new terminal window, run:
```bash
java -cp src/main/java com.example.airlinesmanagement1.SimpleUser
```

You can run multiple instances of SimpleUser to simulate multiple users.

### Step 4: Use Admin Interface

The admin interface is integrated into the JavaFX application. Access it through the admin panel.

## Chat Commands

### For Regular Users (SimpleUser):

- **Broadcast Message**: Simply type any message and press Enter
- **Private Message to Admin**: Type `/msg admin <your message>`
- **Exit**: Type `exit`

### For Admin (AdminChatController):

- **Broadcast Message**: Type message and click "Send" (without selecting a recipient)
- **Private Message**: Select a user from the list and type your message
- **View Connected Users**: The user list updates automatically

## Features

1. **Real-time Messaging**: Instant message delivery
2. **Private Messaging**: Users can send private messages to admin
3. **User Management**: Admin can see all connected users
4. **Automatic Reconnection**: Better error handling for connection issues
5. **Name Conflict Resolution**: Automatic handling of duplicate usernames

## Troubleshooting

### Common Issues:

1. **"Could not connect to server"**
   - Make sure ChatServer is running
   - Check if port 5000 is available
   - Try running `netstat -an | findstr 5000` to check port usage

2. **"Name taken"**
   - The system will automatically try a new name
   - No action needed from user

3. **Connection lost**
   - Check if the server is still running
   - Restart the client

### Port Configuration:

If you need to change the port, modify these files:
- `ChatServer.java` - Change `DEFAULT_PORT` constant
- `SimpleUser.java` - Change `port` variable
- `AdminChatController.java` - Change port in `connectToServer()` method

## Testing the System

1. Start the server
2. Start 2-3 SimpleUser clients in different terminals
3. Test broadcast messages
4. Test private messages to admin
5. Test admin responses
6. Test user disconnection/reconnection

## File Structure

```
src/main/java/com/example/airlinesmanagement1/
├── ChatServer.java          # Main chat server
├── SimpleUser.java          # Command-line user client
├── AdminChatController.java # JavaFX admin interface
└── ... (other files)
```

## Security Notes

- This is a basic chat system for demonstration
- No encryption or authentication
- Suitable for local development/testing only
- For production use, implement proper security measures 