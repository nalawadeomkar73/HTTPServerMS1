package edu.upenn.cis455.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;

class HttpServer {
  
  private static int portNumber;
  private static String rootDirectory;
  protected static ServerSocket server;
  private static ThreadPool threadPool;
  private static final int noOfThreads = 50;

public static void main(String args[])

  {
	  if(args.length==0){
		  System.out.println("Name:Omkar Nalawade\nSEAS Login:omkarn");
		  return;
	  }
	  if(!(args.length==2)){
		  System.out.println("Incorrect Arguments");
		  return;  
	  }
	  
	  if(args.length==2){
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
		  return;
	  }
	  
	
		  try {
			  server = new ServerSocket(portNumber);
			  threadPool = new ThreadPool(portNumber,rootDirectory,noOfThreads);
			  threadPool.executeThreadPool();
			  System.out.println("Listening for connection on port: "+portNumber);
			  while(true){
				  Socket sock = server.accept();
				  threadPool.add(sock);
				  
			  }
			
		  } catch (InterruptedException e) {
			  System.out.println("I am here");
		  } catch (IOException e) {
			  System.out.println("Kill the process id. Command is ps ax | grep HW1 ");
		  }
	  }
  	}
	
}
  
