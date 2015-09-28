/* Author: Luigi Vincent
*
* TODO: 
* add meaningful logging for exceptions
*   e.g. Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex)
*
* Refactor to use be as non-static as possible
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Date;
import java.util.Set;
import java.util.Scanner;

public class ChatServer {
	private static final int PORT = 5290;
	private static Set<String> names;
	private static Set<String> userNames;
	private static Set<PrintWriter> writers;

	public static void main(String[] args) {
		names = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		userNames = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		writers = Collections.newSetFromMap(new ConcurrentHashMap<PrintWriter, Boolean>());

		System.out.println(new Date() + "\nChat Server online.\n");

		try (ServerSocket chatServer = new ServerSocket(PORT)) {
			while (true) {
				Socket socket = chatServer.accept();
				new Thread(new ClientHandler(socket)).start();
			}
		} catch (IOException ioe) {}
	}

	private static String names() {
		return "In lobby: " + String.join(", ", userNames);
	}

	private static class ClientHandler implements Runnable {
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

                out.println(Protocol.SUBMIT);
                name = in.readLine();
                serverSideName = name.toLowerCase();

                while (names.contains(serverSideName) || name == null || name.trim().isEmpty()) {
                	out.println(Protocol.RESUBMIT);
                	name = in.readLine();
                	serverSideName = name.toLowerCase();
                }

                out.println(Protocol.ACCEPT);
                System.out.println(name + " connected. IP: " + socket.getInetAddress().getHostAddress());

                messageAll(Protocol.CONNECT + name);
                userNames.add(name);
                names.add(serverSideName);
                writers.add(out);
                out.println(Protocol.INFORM.name() + userNames.size() + ',' + names());
              

               	while (true) {
	                String input = in.readLine();

	                if (input == null || input.trim().isEmpty()) {
	                    continue;
	                }

	                messageAll(Protocol.MESSAGE + name + ": " + input);
	            }
			} catch (IOException e) {
				if (name != null) {
					System.out.println(name + " disconnected.");
					userNames.remove(name);
	            	names.remove(serverSideName);
	            	writers.remove(out);
					messageAll(Protocol.DISCONNECT + name);
				}	
	        } finally { 	
	            try {
	                socket.close();
	            } catch (IOException e) {}
	        }
		}
	}

	private static void messageAll(String... messages) {
		if (writers.isEmpty()) {
			return;
		}
		
		for (String message : messages) {
			for (PrintWriter writer : writers) {
            	writer.println(message);
        	}
		}
	}
}