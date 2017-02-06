package edu.upenn.cis455.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

class HttpServer {
  
  private static int portNumber;
  private static String rootDirectory;
  protected static ServerSocket server;
  private static BlockingQueue bQueue;
 // private static PacketInformation packetInfo;
  private static ThreadPool threadPool;
  private static final int noOfThreads = 50;
  
  

public static void main(String args[])

  {
    /* your code here */
	  
	  if(args.length==0){
		  System.out.println("Name:Omkar Nalawade\nSEAS Login:omkarn");
		  return;
	  }
	  if(args.length!=2){
		  System.out.println("Incorrect Arguments");
		  return;  
	  }
	  try{
		  portNumber = Integer.valueOf(args[0]);
		  
		  rootDirectory = args[1].trim();
		  if(rootDirectory.endsWith("/")){
			  rootDirectory = rootDirectory.substring(0,rootDirectory.length()-1);
		  }
		  
		  if(!(new File(rootDirectory).exists())){
			  System.out.println("Hello");
			  System.out.println("Name:Omkar Nalawade\nSEAS Login:omkarn");
			  return;
		  }
	  }
	  catch(NumberFormatException e){
		  System.out.println("Please enter a valid port Number");
		  System.exit(1);
	  }
	  
	  
	  
	  try {
		server = new ServerSocket(portNumber);
		//bQueue = new BlockingQueue();
		threadPool = new ThreadPool(portNumber,rootDirectory,noOfThreads);
		threadPool.executeThreadPool();
		
		//packetInfo = new PacketInformation(portNumber,rootDirectory,server);
		
		System.out.println("Listening for connection on port: "+portNumber);
		while(threadPool.checkThreadPoolRunning()){
			Socket sock = server.accept();
			threadPool.add(sock);
			//bQueue.enqueue(sock);
		}
		//server.close();
	
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		System.out.println("Socket sock did not accept");
	} catch (IOException e) {
		// TODO Auto-generated catch block
		System.out.println("Server socket was not initialized");
	}
  }
  
}
