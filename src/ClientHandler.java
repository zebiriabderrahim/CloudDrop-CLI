import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
/*
 * INF3405 Réseaux informatiques TP1 Groupe 04
 * Gestionnaire de fichier
 * Chargé: Mehdi Kadi
 *
 *`ClientHandler class handles commands from a client connected to a server via a socket.
 *`The code includes a constructor that sets up input and output streams and the socket number of the connected client.
 * It also has the following methods:
 *  exit: closes the socket connection with the client and prints a message indicating that the client has disconnected.
 *  ls: sends a list of files present in the current directory to the client.
 *  mkdir: creates a new directory in the current directory specified by the client.
 *  cd: changes the current directory to the one specified by the client.
 *
 * @author Abderrahim Zebiri
 * @author Aymane Chalh
 * @author Jeremy Ear
 */
public class ClientHandler extends Thread
{
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";

    private final Socket socket;
    private final int clientNumber;


    /*
     * Prints the client number and socket details on the screen.
     */
    public ClientHandler(Socket socket, int clientNumber)
    {
        this.socket 		= socket;
        this.clientNumber = clientNumber;
        System.out.println(ANSI_GREEN+"\nConnection established successfully for client #" + clientNumber + " (" + socket + ")\n"+ANSI_RESET);
    }


    /*
     * The main code that runs for all clients when the connection is successfully established.
     * @throws 	[IOException] 	If there was a problem opening or closing the socket with the client.
     *
     */
    public void run()
    {
        try
        {
            final String	SERVER_READY 					= "ready";
            final String 	DATE_TIME_FORMAT			= "yyyy-MM-dd@HH:mm:ss";
            final int 		MIN_CLIENT_COMMAND_LENGTH = 0;
            final int 		MAX_CLIENT_COMMAND_LENGTH = 2;

            // Incoming channel that receives messages sent by the server.
            DataOutputStream dataSent = new DataOutputStream(socket.getOutputStream());
            dataSent.writeUTF(ANSI_GREEN+"Hi:), client #" + clientNumber + "!\n"
                    + "The connection is established and ready."
                    + "\n\nPlease choose a command without space between arguments from :\n\n"
                    + "- cd <Name of a directory on the server>\n\t	Command to move the user to a child or parent directory.\n"
                    + "- ls\n		Command to display all folders and files in the user's current directory on the server.\n"
                    + "- mkdir <Name of the new directory>\n		Command to create a directory on the server storage.\n"
                    + "- upload <File name>\n		Command to upload a file, located in the client's local directory, to the storage server.\n"
                    + "- download <File name>\n		Command to download a file, located in the user's current directory on the storage server, to the client's local directory.\n"
                    + "- exit\n		Command to disconnect the client from the storage server.\n"+ANSI_RESET);


            DataInputStream dataReceived = new DataInputStream(socket.getInputStream());
            ClientHandlerCommands clientHandlerCommands = new ClientHandlerCommands(dataSent, dataReceived, socket, clientNumber);
            String			clientResponse;
            String[] 		clientCommandsSeparated;


            while (true)
            {
                dataSent.writeUTF("\nVotre commande: ");
                dataSent.flush();

                // Notifying the client that the server is ready to receive the command.
                dataSent.writeUTF(SERVER_READY);

                clientResponse = dataReceived.readUTF();

                // Sending client commands via the console.
                System.out.format("\n\n["+ANSI_GREEN+"%s - %s] : %s", socket.getRemoteSocketAddress().toString().substring(1),
                        new SimpleDateFormat(DATE_TIME_FORMAT).format(Calendar.getInstance().getTime())+ANSI_RESET, clientResponse);

                // Separating the category of the command and the name precision for commands that require it.
                clientCommandsSeparated = clientResponse.split(" ");

                // The command must be 1 or 2 strings long.
                if (clientCommandsSeparated.length > MIN_CLIENT_COMMAND_LENGTH && !(clientCommandsSeparated.length > MAX_CLIENT_COMMAND_LENGTH))
                {
                    // Valid length command.
                    switch (clientCommandsSeparated[0].toLowerCase()) {
                        case "cd" -> clientHandlerCommands.cd(clientCommandsSeparated);
                        case "ls" -> clientHandlerCommands.ls();
                        case "mkdir" -> clientHandlerCommands.mkdir(clientCommandsSeparated);
                        case "upload" -> clientHandlerCommands.upload(clientCommandsSeparated);
                        case "download" -> clientHandlerCommands.download(clientCommandsSeparated);
                        case "exit" -> {
                            clientHandlerCommands.exit();
                            return;
                        }
                        default -> dataSent.writeUTF(ANSI_RED + "\n command not found ." + ANSI_RESET);
                    }
                }

                else
                    dataSent.writeUTF(ANSI_RED+"\ninvalid command length. Please refer to the command list quoted above."+ANSI_RESET);
            }
        }

        catch (IOException e)
        {
            System.out.println("\nERROR ! Unable to establish connection with client #" + clientNumber + ". (" + e + ")");
        }

    }

}