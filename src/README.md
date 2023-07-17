# CloudDrop - Client/Server Application

CloudDrop is a simple command-line-based client-server application for managing files on a personal cloud storage. The server is capable of handling multiple client connections concurrently using threads and sockets. The communication between the client and the server takes place over ports 5000 to 5500.

## Server

The CloudDrop server verifies the IP address and used port, establishes connections with clients, and handles their commands. It supports the following commands:

- `mkdir <New folder name>`: Create a new folder on the storage server.
- `cd <Directory on the server>`: Navigate to a child or parent directory on the server. Use '..' to move to a parent directory.
- `ls`: Display all folders and files in the current directory of the user on the server.
- `upload <File name>`: Upload a file from the client's local directory to the storage server.
- `download <File name>`: Download a file from the current directory of the user on the storage server to the client's local directory.
- `exit`: Disconnect the client from the storage server.

To run the server:

```bash
$ javac Server.java
$ java Server

```
## Client

The CloudDrop client establishes a connection with the server and interacts with it using the above commands. The client code communicates with the server using threads and sockets over ports 5000 to 5500.

To run the client:

```bash
$ javac Client.java
$ java Client
    
```
Please note that you can have multiple instances of the client running simultaneously to test concurrent connections to the server.

## Prerequisites

Java Development Kit (JDK) 19 or later

## Contributions

We welcome contributions to CloudDrop! Feel free to create pull requests or report any issues on our GitHub repository.