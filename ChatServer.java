/* Author: Luigi Vincent */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;

public class ChatServer {
	private static final int PORT = 9001;
	private static HashSet<String> names = new HashSet<>();
	private static HashSet<String> userNames = new HashSet<>();
	private static HashSet<PrintWriter> writers = new HashSet<>();
	private static int usersConnected = 0;

	public static void main(String[] args) {
		System.out.println(new Date() + "\nChat Server online.");

		try (ServerSocket chatServer = new ServerSocket(PORT)) {
			while (true) {
				Socket socket = chatServer.accept();
				System.out.println("IP: " + socket.getInetAddress().getHostAddress() + " connected.");
				new ClientHandler(socket).start();
			}
		} catch (IOException ioe) {}
	}

	private static String names() {
		StringBuilder nameList = new StringBuilder();

		for (String name : userNames) {
			nameList.append(", ").append(name);
		}

		return nameList.substring(2);
	}

	private static class ClientHandler extends Thread {
		private String name;
		private String serverSideName;
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;

		public ClientHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println("SUBMIT_NAME");
                name = in.readLine();
                serverSideName = name.toLowerCase();

                synchronized (names) {
                	while (names.contains(serverSideName) || name == null || name.trim().isEmpty()) {
                		out.println("RESUBMIT_NAME");
                		name = in.readLine();
                		serverSideName = name.toLowerCase();
                	}
                }

                out.println("NAME_ACCEPTED");
                userNames.add(name);
                names.add(serverSideName);
                writers.add(out);
                out.println("INFO" + ++usersConnected + names());
              

               	while (true) {
	                String input = in.readLine();

	                if (input == null || input.isEmpty()) {
	                    continue;
	                }

	                for (PrintWriter writer : writers) {
	                	writer.println("MESSAGE " + name + ": " + input);
	                }
	            }
			} catch (IOException e) {
                for (PrintWriter writer : writers) {
                	writer.println("DISCONNECT" + name);
                }
	        } finally {
	            names.remove(serverSideName);
	            writers.remove(out);
	            usersConnected--;

	            try {
	                socket.close();
	            } catch (IOException e) {}
	        }
		}
	}
}