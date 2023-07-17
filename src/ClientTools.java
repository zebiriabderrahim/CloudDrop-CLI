import java.io.*;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ClientTools {
    // Define constants for minimum and maximum port numbers and a regular expression pattern for IP addresses
    private static final int MIN_PORT = 5000;
    private static final int MAX_PORT = 5050;
    private static final Pattern IP_ADDRESS_PATTERN =
            Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");

    // Method to validate an IP address using the IP address pattern
    private static boolean validateIP(String ip) {
        Matcher matcher = IP_ADDRESS_PATTERN.matcher(ip);
        if (!matcher.matches()) {
            return false;
        }
        for (int i = 1; i <= 4; i++) {
            int part = Integer.parseInt(matcher.group(i));
            if (part < 0 || part > 255) {
                return false;
            }
        }
        return true;
    }

    // Method to validate a port number within the specified range
    private static boolean validatePort(int port) {
        return port >= MIN_PORT && port <= MAX_PORT;
    }

    // Method to prompt the user to enter an IP address and validate it
    public static String getIP() {
        Scanner ipInput = new Scanner(System.in);
        String ip = "";
        while (!validateIP(ip)) {
            System.out.println("Please enter a valid IP address in the format of xxx.xxx.xxx.xxx");
            ip = ipInput.nextLine();
        }
        return ip;
    }

    // Method to prompt the user to enter a port number and validate it
    public static int getPort() {
        Scanner portInput = new Scanner(System.in);
        int port = 0;
        while (!validatePort(port)) {
            System.out.println("Please enter a port number in the range of " + MIN_PORT + "-" + MAX_PORT);
            try {
                port = Integer.parseInt(portInput.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number. Please try again.");
            }
        }
        return port;
    }

    // Method to download a file from the server
    public static void download(DataInputStream receivedData) {
        try {
            // Read the file name and size from the input stream
            final String FILE_NAME = receivedData.readUTF();
            final int FILE_SIZE = Integer.parseInt(receivedData.readUTF());
            byte[] receivedBytes = new byte[FILE_SIZE];
            int bytes = 0;

            // Read the file contents into a byte array
            while (bytes < FILE_SIZE) {
                bytes += receivedData.read(receivedBytes, bytes, FILE_SIZE - bytes);
                System.out.format("\rDownloading... %d/%d bytes", bytes, FILE_SIZE);
            }

            // Save the file using the original file name, after sanitizing it
            File file = new File(FILE_NAME);
            String safeFileName = file.getName().replaceAll("[^a-zA-Z0-9.\\-]", "_");
            File outputFile = new File(file.getParent(), safeFileName);

            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.write(receivedBytes);
            fos.close();

            System.out.println("\nFile <" + FILE_NAME + "> has been downloaded successfully.");
        } catch (IOException e) {
            System.out.println("An error occurred while downloading the file: " + e.getMessage());
        }
    }
    public static String ready(DataOutputStream dataUploaded, Scanner INPUT) throws IOException {
        // Read the command from the user
        String command = INPUT.nextLine();
        // Write the command to the DataOutputStream object
        dataUploaded.writeUTF(command);
        // Return the command as a String
        return command;
    }

    public static void upload(DataInputStream receivedData, DataOutputStream dataUploaded) throws IOException {
        // Read the name of the file to upload from the DataInputStream object
        final String nameFileUploaded = receivedData.readUTF();
        // Create a File object for the file to be uploaded
        File uploadedFile = new File(System.getProperty("user.dir"), nameFileUploaded);

        // Check if the file exists, is a file (not a directory), and is readable
        if (uploadedFile.exists() && uploadedFile.isFile() && uploadedFile.canRead()) {
            // Write "ready" to the DataOutputStream object
            dataUploaded.writeUTF("ready");
            // Read the file into a byte array
            byte[] bytesFile = Files.readAllBytes(uploadedFile.toPath());
            // Write the length of the byte array as a String to the DataOutputStream object
            dataUploaded.writeUTF(Long.toString(bytesFile.length));
            // Write the byte array to the DataOutputStream object
            dataUploaded.write(bytesFile, 0, bytesFile.length);
        } else {
            // Write "not_ready" to the DataOutputStream object
            dataUploaded.writeUTF("not_ready");
            // Print an error message to the console
            System.out.printf("\nFile <%s> not found in client directory <%s> (or not a download file).\n%n", nameFileUploaded, "");
        }
    }



}
