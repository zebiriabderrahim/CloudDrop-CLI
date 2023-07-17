import java.net.Socket;
import java.util.Scanner;
import java.io.DataInputStream;
import java.io.DataOutputStream;
/*
 * INF3405 Réseaux informatiques TP1 Groupe 04
 * Gestionnaire de fichier
 * Chargé: Mehdi Kadi
 *
 * Client class that establishes a connection with a server and allows the user to upload and download files.
 * It uses sockets to communicate with the server and has a main loop to listen for server messages.
 * The program terminates when the user inputs the "exit" command or the connection is lost.
 *
 * @author Abderrahim Zebiri
 * @author Aymane Chalh
 * @author Jeremy Ear
 */

public class Client {

	public static void main(String[] args) throws Exception {
		final Scanner INPUT = new Scanner(System.in);
		final String DOWNLOAD = "download";
		final String UPLOAD = "upload";
		final String EXIT = "exit";
		final String READY = "ready";
		final String SERVER_IP = ClientTools.getIP();
		final int SERVER_PORT = ClientTools.getPort();

		System.out.format("\nServer IP:port -> %s:%s", SERVER_IP, SERVER_PORT);
		System.out.println("\nTrying to establish connection... ");

		Socket socket;
		try {
			// Establish a new connection with the server
			socket = new Socket(SERVER_IP, SERVER_PORT);
			System.out.println(socket.getRemoteSocketAddress().toString());
		} catch (java.net.ConnectException e) {
			System.out.println("Could not establish connection to the server. Exiting.");
			INPUT.close();
			return;
		}

		DataInputStream receivedData = new DataInputStream(socket.getInputStream());
		DataOutputStream dataUploaded = new DataOutputStream(socket.getOutputStream());
		String response;
		label:
		// Start the main loop to listen for server messages
		while (true) {
			response = receivedData.readUTF();
			switch (response) {
				case READY -> {
					// If the input equals exit, terminate the connection between the client and the server
					if (ClientTools.ready(dataUploaded, INPUT).split(" ")[0].equals(EXIT)) {
						break label;
					}
				}
				case UPLOAD -> ClientTools.upload(receivedData, dataUploaded);
				case DOWNLOAD -> ClientTools.download(receivedData);
				default -> System.out.println(response);
			}
		}

		// Close the input stream and end the program
		System.out.println("\nSuccessfully disconnected. End of program.");
		INPUT.close();
	}
}
