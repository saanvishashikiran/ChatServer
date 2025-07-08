# Chatty Chat Chat

A lightweight multithreaded chat application implemented in Java using sockets. Chatty Chat Chat supports real-time communication between multiple clients through a central server, with basic support for direct messaging, nicknames, and graceful disconnects.

## Features

- Multi-client chat server using Java sockets and threads
- Command-line client interface
- Broadcast and direct (private) messaging via '/dm <nickname> <message>'
- Configurable usernames via `/nick <name>`
- Clean client exit via `/quit`
- Simple text-based protocol (CCC Protocol)

## Usage

### Start the Server

java ChattyChatChatServer <port>
# Example:
java ChattyChatChatServer 9876

### Start a Client

java ChattyChatChatClient <hostname> <port>
# Example:
java ChattyChatChatClient localhost 9876

