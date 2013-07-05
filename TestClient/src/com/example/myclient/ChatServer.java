package com.example.myclient;

// $Id: ChatServer.java,v 1.3 2012/02/19 06:12:34 cheon Exp $

import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.util.*;

import edu.UTEP.android.Message;

public class ChatServer {

	private static final String USAGE = "Usage: java ChatServer";

	private static final int PORT_NUMBER = 8008;
	private HashMap<String,PrintWriter> clients;
	private DBConnection Db;
	/** Creates a new server. */
	public ChatServer() {
		clients = new HashMap<String,PrintWriter>();
	}
	

	/** Starts the server. */
	public void start() {
		System.out.println("AndyChat server started on port " + PORT_NUMBER
				+ "!");
		try {
			ServerSocket s = new ServerSocket(PORT_NUMBER);
			for (;;) {
				Socket incoming = s.accept();
				new ClientHandler(incoming).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("AndyChat server stopped.");
	}

	/** Adds a new client identified by the given print writer. */
	private void addClient(String ip, PrintWriter out) {
		synchronized (clients) {
			clients.put(ip,out);
		}
	}

	/** Adds the client with given print writer. */
	private void removeClient(PrintWriter out) {
		synchronized (clients) {
			clients.remove(out);
		}
	}

	/** Broadcasts the given text to all clients. */
	private void broadcast(String msg) {
		int GroupID=1;
		Db=DBConnection.db;
		String query="SELECT u.ip_address FROM user u,group_user gu WHERE u.email=gu.user_id AND gu.group_id="+GroupID+";";
		ResultSet rs=Db.querys(query);
		try {
			while(rs.next())
			{
				for (PrintWriter out : clients.values()) {
					if(out==clients.get(rs.getString("ip_address"))){
					out.println(msg);
					out.flush();
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			System.out.println(USAGE);
			System.exit(-1);
		}
		new ChatServer().start();
	}

	/**
	 * A thread to serve a client. This class receive messages from a client and
	 * broadcasts them to all clients including the message sender.
	 */
	private class ClientHandler extends Thread {

		/** Socket to read client messages. */
		private Socket incoming;

		/** Creates a hander to serve the client on the given socket. */
		public ClientHandler(Socket incoming) {
			this.incoming = incoming;
		}

		/** Starts receiving and broadcasting messages. */
		public void run() {
			PrintWriter out = null;
			try {
				out = new PrintWriter(new OutputStreamWriter(
						incoming.getOutputStream()));

				ChatServer.this.addClient(incoming.getInetAddress().getHostAddress(),out);
				
				out.print("Welcome to AndyChat! ");
				out.println("Enter BYE to exit.");
				out.flush();

				ObjectInputStream ois = new ObjectInputStream(
						incoming.getInputStream());
				for (;;) {
					Message objMessage = (Message) ois.readObject();
					if (objMessage.getMsg() == null) {
						break;
					} else {
						
						
						System.out.println("From Client :"
								+ objMessage.getMsg());
						ChatServer.this.broadcast(objMessage.getMsg());
					}
				}

				ChatServer.this.removeClient(out);
				incoming.close();
			} catch (Exception e) {
				if (out != null) {
					ChatServer.this.removeClient(out);
				}
				e.printStackTrace();
			}
		}
	}
	
	
}

