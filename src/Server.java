import java.util.Scanner;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
/*
 * This server class verifies that the IP address and used port are valid
 * and establishes the connexion between one or multiple users
 *
 * @author Abderrahim Zebiri
 * @author Aymane Chalh
 * @author Jeremy Ear
 */

public class Server
{
	private static ServerSocket listener;


	/**
	 * This main function starts by verifying that the entered IP address and port are valid using
	 * our two functions (getIp and getPort) and establishes the connexion between the users
	 *
	 * @param args							Collection of Strings that are separated by a space and can be
	 * 										typed into the program on the terminal directly
	 *
	 * @throws IllegalArgumentException 	This exception is thrown if the argument is
	 *                                      invalid when establishing the socket
	 *
	 * @throws java.net.BindException		This exception is thrown if it's impossible to establish the connexion
	 *
	 */
	public static void main(String[] args) throws Exception
	{
		String serverIpAddress = ClientTools.getIP();
		int clientId = 0, serverPort = ClientTools.getPort();
		System.out.format("\nIP address:Server port -> %s:%s", serverIpAddress, serverPort);
		try
		{
			listener = new ServerSocket();
			listener.setReuseAddress(true); // enables the SO_REUSEADDR socket option
			listener.bind(new InetSocketAddress(serverIpAddress, serverPort));


			while (true) {
				new ClientHandler(listener.accept(), clientId++).start();
			}
		}

		catch (IllegalArgumentException e)
		{
			System.out.println("\nInvalid argument for the IP address or used port. Please try again.\n");
		}

		catch (java.net.BindException e)
		{
			System.out.println("\nIt is impossible to establish the connexion. Please try again.\n");
		}
		finally
		{
			listener.close();
		}
	}


}
