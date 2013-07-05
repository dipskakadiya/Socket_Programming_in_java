package com.example.myserver;

import java.net.*;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class ChatServer implements Runnable
{  private List<ChatServerThread> clients = new ArrayList<ChatServerThread>();
   private ServerSocket server = null;
   private Thread       thread = null;
   DBConnection Db=null;

   public ChatServer(int port)
   {  try
      {  System.out.println("Binding to port " + port + ", please wait  ...");
         server = new ServerSocket(port);  
         System.out.println("Server started: " + server);
         start(); }
      catch(IOException ioe)
      {  System.out.println("Can not bind to port " + port + ": " + ioe.getMessage()); }
   }
   public void run()
   {  while (thread != null)
      {  try
         {  System.out.println("Waiting for a client ..."); 
            addThread(server.accept()); }
         catch(IOException ioe)
         {  System.out.println("Server accept error: " + ioe); stop(); }
      }
   }
   public void start()  { 
	   if (thread == null)
	      {  thread = new Thread(this); 
	         thread.start();
	      }
   }
   public void stop()   { 
	   if (thread != null)
	      {  thread.stop(); 
	         thread = null;
	      }
   }
   private int findClient(int ID)
   {  for (int i = 0; i < clients.size(); i++)
         if (clients.get(i).getID() == ID)
            return i;
      return -1;
   }
   
   public synchronized void handle(int ID, String input)
   { 
	   try
	   {
		   Db=DBConnection.db;
		   String sql="SELECT threadid,user_id FROM group_user WHERE group_id=1;";
		   ResultSet rs=Db.querys(sql);
		   while (rs.next())
		   {
			   clients.get(findClient(rs.getInt("threadid"))).send(rs.getString("user_id")+":"+input);
		   }
	   }
	   catch(Exception e)
	   {
		   e.printStackTrace();
	   }
   }
   public synchronized void remove(int ID)
   {  int pos = findClient(ID);
      if (pos >= 0)
      {  ChatServerThread toTerminate = clients.get(pos);
         System.out.println("Removing client thread " + ID + " at " + pos);
         try
         {  toTerminate.close(); }
         catch(IOException ioe)
         {  System.out.println("Error closing thread: " + ioe); }
         toTerminate.stop(); }
   }
   private void addThread(Socket socket)
   {  
	   try{
	   clients.add(new ChatServerThread(this, socket));
	   clients.get(clients.size()-1).open();
	   clients.get(clients.size()-1).start();
	   }catch(Exception e)
	   {
		   e.printStackTrace();
	   }
   }
   public static void main(String args[]) { 
	   
	   ChatServer c=new ChatServer(8008);
	  
   }
}