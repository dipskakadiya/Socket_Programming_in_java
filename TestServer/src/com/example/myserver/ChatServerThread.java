package com.example.myserver;

import java.net.*;
import java.io.*;

import edu.UTEP.android.Message;

public class ChatServerThread extends Thread
{  private ChatServer       server    = null;
   private Socket           socket    = null;
   private int              ID        = -1;
   private DataInputStream  streamIn  =  null;
   private DataOutputStream streamOut = null;
   private ObjectInputStream ois=null;

   public ChatServerThread(ChatServer _server, Socket _socket)
   {  super();
      server = _server;
      socket = _socket;
      ID     = socket.getPort();
   }
   public void send(String msg)
   {   try
       {  streamOut.writeUTF(msg);
          streamOut.flush();
       }
       catch(IOException ioe)
       {  System.out.println(ID + " ERROR sending: " + ioe.getMessage());
          server.remove(ID);
          stop();
       }
   }
   public int getID()
   {  return ID;
   }
   public void run()
   {  System.out.println("Server Thread " + ID + " running.");
      while (true)
      {  try
         {  
    	  Message objMessage=(Message)ois.readObject();
    	  server.handle(ID, objMessage.getMsg());
         }
         catch(Exception ioe)
         {  System.out.println(ID + " ERROR reading: " + ioe.getMessage());
            server.remove(ID);
            stop();
         }
      }
   }
   public void open() throws IOException
   {  //streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
      streamOut = new DataOutputStream(new
                        BufferedOutputStream(socket.getOutputStream()));
      ois= new ObjectInputStream(socket.getInputStream());
   }
   public void close() throws IOException
   {  if (socket != null)    socket.close();
      if (streamIn != null)  streamIn.close();
      if (streamOut != null) streamOut.close();
      if (ois != null) ois.close();
   }
}