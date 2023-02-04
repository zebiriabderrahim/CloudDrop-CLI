import java.io.*;
import java.net.Socket;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientHandlerCommands {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    private static final String EMPTY_REPO = "";
    private final String GLOBAL = "*";
    private final int LENGTH_OF_COMMAND_NAME = 2;
    private final DataOutputStream dataSent;
    private final DataInputStream dataReceived;
    private final Socket socket;
    private final int clientNumber;
    private String currentRepo = "";
    private String path = "";

    public ClientHandlerCommands(DataOutputStream dataSent, DataInputStream dataReceived, Socket socket, int clientNumber) {
        this.dataSent = dataSent;
        this.dataReceived = dataReceived;
        this.socket = socket;
        this.clientNumber = clientNumber;
    }

    /**

     Returns the current directory path without a trailing slash.
     @param currentRepo [String] The path of the current directory.
     @return [String] The path of the current directory without a trailing slash.
     */
    public static String getPathWithoutSlash(String currentRepo) {
        String path;

        if (!currentRepo.equals(EMPTY_REPO))
            path = currentRepo.substring(1);

        else
            path = currentRepo;

        return path;
    }

    /**
     Closes the socket connection and prints a message indicating that the client has disconnected.
     */

    public void exit() {
        try {
            socket.close();
            System.out.println(ANSI_YELLOW + "\nclient #" + clientNumber + " disconnected.\n" + ANSI_RESET);
        } catch (IOException e) {
            System.out.println(ANSI_RED + "\nERREUR ! Nous n'avons pas pu fermer la connexion avec le client #"
                    + clientNumber + "\n" + ANSI_RESET);
        }
    }

    /**
     * Sends a list of files and folders present in the current directory to the client.
     *
     * @throws IOException If an I/O error occurs while sending data to the client.
     */

    public void ls() throws IOException {
        dataSent.writeUTF("\nFiles present in the directory:\n");

        path = getPathWithoutSlash(currentRepo);
        Path directory = Paths.get(path);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, GLOBAL)) {
            for (Path file : stream) {
                String fileType = file.toFile().isDirectory() ? "[Folder]" : "[File]";
                String color = file.toFile().isDirectory() ? ANSI_GREEN : "";
                dataSent.writeUTF(String.format("%s %s%s%s", fileType, color, file, ANSI_RESET));
            }
        } catch (IOException e) {
            dataSent.writeUTF(ANSI_RED + "\nOops! I couldn't list the files in the current directory.\n" + ANSI_RESET);
        }
    }


    /**
     * Creates a new directory in the current directory with the given name.
     * The name must consist only of alphanumeric characters, underscores and hyphens.
     * @param commandesClientSeparees The command and folder name sent by the client
     * @throws IOException If an I/O error occurs while creating the directory
     */
    public void mkdir(String[] commandesClientSeparees) throws IOException {
        if (commandesClientSeparees.length != 2) {
            dataSent.writeUTF(ANSI_RED + "\nInvalid <mkdir> command; missing file name.\n" + ANSI_RESET);
            return;
        }

        String folderName = commandesClientSeparees[1];

        if (!folderName.matches("[a-zA-Z0-9_-]+")) {
            dataSent.writeUTF(ANSI_RED + "\n Invalid <mkdir> command:not accepted directory name .\n" + ANSI_RESET);
            return;
        }

        String pathWithoutSlash = getPathWithoutSlash(currentRepo);
        File folder = new File(pathWithoutSlash.isEmpty() ? folderName : pathWithoutSlash + File.separator + folderName);

        if (folder.exists()) {
            dataSent.writeUTF(ANSI_YELLOW + String.format("\n/%s is already present within the directory.\n"
                    , folderName) + ANSI_RESET);
        } else {
            folder.mkdir();
            dataSent.writeUTF(ANSI_GREEN + String.format("\n/%s successfully created within the directory.\n"
                    , folderName) + ANSI_RESET);
        }
    }

    /**
     * Changes the current directory to the directory specified by the user.
     * If the user enters "..", the current directory will be changed to the parent directory.
     *
     * @param parsedClientCommand [String[]]  An array of strings representing the parsed client command.
     *
     * @throws IOException If an I/O error occurs.
     */

    public void cd(String[] parsedClientCommand) throws IOException {
        if (parsedClientCommand.length != LENGTH_OF_COMMAND_NAME) {
            dataSent.writeUTF("\nCommande <cd> prend obligatoirement un chemin.\n");
            return;
        }

        String parentDirectory = "..";
        if (parsedClientCommand[1].equals(parentDirectory)) {
            int positionLastOccurrenceSlash = currentRepo.lastIndexOf(File.separator);
            int INDEX_ELEMENT_NON_FOUND = -1;
            if (positionLastOccurrenceSlash != INDEX_ELEMENT_NON_FOUND) {
                currentRepo = currentRepo.substring(0, positionLastOccurrenceSlash);

                if (currentRepo.equals(EMPTY_REPO)) {
                    dataSent.writeUTF(String.format("\nyou are located  <%s> the server root.\n", currentRepo));
                } else {
                    dataSent.writeUTF(String.format("\ncurrent directory: <%s>\n", currentRepo));
                }
            } else {
                dataSent.writeUTF("\nYou are already ? server root.\n");
            }
        } else {
            path = getPathWithoutSlash(currentRepo);

            boolean currentDirectory = false;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path), GLOBAL)) {
                for (Path file : stream) {
                    if (file.getFileName().toString().equals(parsedClientCommand[1]) && Files.isDirectory(file)) {
                        currentDirectory = true;
                        break;
                    }
                }
            }

            if (!currentDirectory) {
                dataSent.writeUTF(String.format("\nFolder <%s> is not present in directory <%s>.\n",
                        parsedClientCommand[1], currentRepo));
            } else {
                currentRepo += (File.separator + parsedClientCommand[1]);
                dataSent.writeUTF(String.format("\ncurrent directory: <%s>\n", currentRepo));
            }
        }
    }

    /**
     * Uploads a file from the client to the server.
     *
     * @param parsedClientCommand [String[]] An array of strings representing the parsed client command. The second element
     *                                      should be the name of the file to upload.
     *
     * @throws IOException If an I/O error occurs.
     */
    public void upload(String[] parsedClientCommand) throws IOException {
        if (parsedClientCommand.length != LENGTH_OF_COMMAND_NAME) {
            dataSent.writeUTF(ANSI_RED + "\nmust entre an filename!\n" + ANSI_RESET);
            return;
        }

        // Inform the client that the server is ready to upload the file
        dataSent.writeUTF("upload");
        dataSent.writeUTF(parsedClientCommand[1]);

        // Verify that the client is uploading an existing file
        final String VERIFICATION_CLIENT_FILE = dataReceived.readUTF();
        if (!VERIFICATION_CLIENT_FILE.equals("ready")) {
            dataSent.writeUTF(ANSI_RED + "\nFile does not exist!\n" + ANSI_RESET);
            return;
        }

        // Receive the size of the file from the client
        final int FILE_SIZE = Integer.parseInt(dataReceived.readUTF());

        // Create the byte buffer
        byte[] receivedBytes = new byte[FILE_SIZE];
        int bytesRead = 0;

        // Read the received bytes
        while (bytesRead < FILE_SIZE)
            bytesRead += dataReceived.read(receivedBytes, bytesRead, FILE_SIZE - bytesRead);

        String path = getPathWithoutSlash(currentRepo);

        // Write to the file from the received bytes
        FileOutputStream fos = path.equals(EMPTY_REPO)
                ? new FileOutputStream(parsedClientCommand[1])
                : new FileOutputStream(path + File.separator + parsedClientCommand[1]);

        fos.write(receivedBytes);
        fos.close();

        dataSent.writeUTF(ANSI_GREEN + String.format("\nThe file <%s> has been uploaded.\n", parsedClientCommand[1]) + ANSI_RESET);
    }

    /**
     * Downloads a file from the server with the specified name.
     *
     * @param parsedClientCommand An array of strings representing the parsed client command. The second element
     *                            should contain the name of the file to be downloaded.
     * @throws IOException If an I/O error occurs.
     */
    public void download(String[] parsedClientCommand) throws IOException {
        // The download command requires a file name.
        if (parsedClientCommand.length != LENGTH_OF_COMMAND_NAME) {
            dataSent.writeUTF(ANSI_RED + "\nmust enter a filename\n" + ANSI_RESET);
            return;
        }

        String filename = parsedClientCommand[1];
        String currentDirectory = getPathWithoutSlash(currentRepo);
        Path filePath = currentDirectory.equals(EMPTY_REPO) ?
                Paths.get(filename) : Paths.get(currentDirectory, filename);

        if (Files.notExists(filePath) || Files.isDirectory(filePath) || !Files.isReadable(filePath)) {
            dataSent.writeUTF(ANSI_RED + String.format(
                    "\nrequested file <%s> does not exist within the server directory <%s>.\n"
                    , filename, currentRepo) + ANSI_RESET);
            return;
        }

        dataSent.writeUTF("download");
        dataSent.writeUTF(filename);
        byte[] fileBytes = Files.readAllBytes(filePath);
        dataSent.writeUTF(Long.toString(fileBytes.length));
        dataSent.write(fileBytes, 0, fileBytes.length);
    }

}
