package edu.upenn.cis455.webserver;
/*References for Eventdriven:
 * http://www.programcreek.com/java-api-examples/java.nio.channels.SocketChannel
 * https://examples.javacodegeeks.com/core-java/nio/channels/selector-channels/java-nio-channels-selector-example/
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
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
  private static boolean useEventDriven = true;
  public static boolean isNotShutdown;

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
	  
	  if(!useEventDriven){
		  try {
			  threadPool = new ThreadPool(portNumber,rootDirectory,noOfThreads);
			  threadPool.executeThreadPool();
			  server = new ServerSocket(portNumber);
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
	  else{
		  Selector selector = null;
		  ServerSocketChannel server = null;
		  isNotShutdown = true;
		 
		  try {
			  selector = Selector.open();
			
			  server = ServerSocketChannel.open();
			  server.socket().bind(new InetSocketAddress(portNumber));
			  
			  server.configureBlocking(false);
			  server.register(selector, SelectionKey.OP_ACCEPT);
		  }catch (IOException e) {
					
					System.out.println("Issue while starting the event driven server");
		  }
		  System.out.println("Listening for connection on port: "+portNumber);
		  while(isNotShutdown){
				  try {
					 
					while(selector.select()>0){
					Iterator<SelectionKey> i = selector.selectedKeys().iterator(); 
					while(i.hasNext()){
						SelectionKey key = i.next(); 
						if(key.isAcceptable()){
							SocketChannel client = server.accept(); 
							client.configureBlocking(false); 
							client.socket().setTcpNoDelay(true); 
							client.register(selector, SelectionKey.OP_READ);
						}
						else if(key.isReadable()){
							SocketChannel sock = (SocketChannel)key.channel();
							sock.configureBlocking(false);
							
							java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(5000);
							
							sock.read(buffer);
							String output = new String(buffer.array()).trim();
							buffer.flip();
							
	
							System.out.println("Request is:\n"+output);
							RequestData parseReq = new RequestData(output+"\r\n");
							java.nio.ByteBuffer bufferResponse = java.nio.ByteBuffer.wrap(EventDrivenMgr.getResponse(parseReq,rootDirectory,portNumber));
							
							sock.write(bufferResponse);
							bufferResponse.clear();
							sock.close();
							if(isNotShutdown==false){
								System.exit(1);
							}
							
						}
						i.remove();
					}
					}
				} catch (IOException e) {
					
				} 
				catch(NullPointerException e){
				}
		  	}
	  	}
		  
	 }
}

	

	

  
