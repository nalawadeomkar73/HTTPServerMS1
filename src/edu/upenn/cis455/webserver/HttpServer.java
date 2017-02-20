package edu.upenn.cis455.webserver;
/*References for Eventdriven:
 * http://www.programcreek.com/java-api-examples/java.nio.channels.SocketChannel
 * https://examples.javacodegeeks.com/core-java/nio/channels/selector-channels/java-nio-channels-selector-example/
 */



import java.io.File;
import java.io.IOException;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;





class HttpServer {
  
  private static int portNumber;
  private static String rootDirectory;
  protected static ServerSocket server;
  private static ThreadPool threadPool;
  private static final int noOfThreads = 50;
  private static boolean useEventDriven = false;
  public static boolean isNotShutdown;
  private static File webxml;
  private static 	Context context;
  private static HashMap<String, String> urlPatterns;
private static HashMap<String, Session> sessionMap;
private static HashMap<String, HttpServlet> servlets;
protected static Timer timer;
private static TimerTask newSession;

  

public static void main(String args[])

  {
	  if(args.length==0){
		  System.out.println("Name:Omkar Nalawade\nSEAS Login:omkarn");
		  return;
	  }
	  if(!(args.length==3)){
		  System.out.println("Incorrect Arguments");
		  return;  
	  }
	  
	  
	  try{
		  portNumber = Integer.valueOf(args[0]); 
		  webxml = new File(args[2].trim());
		  rootDirectory = args[1].trim();
		  if(rootDirectory.endsWith("/")){
			  rootDirectory = rootDirectory.substring(0,rootDirectory.length()-1);
		  }
		  
		  if(!(new File(rootDirectory).exists())){
			  System.out.println("Name:Omkar Nalawade\nSEAS Login:omkarn");
			  return;
		  }
		  
		  if(!(webxml.exists())){
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
				Handler h= parseWebdotxml(args[2]);
				
				context = createContext(h);
				HashMap<String,HttpServlet> servlets = createServlets(h,context);
				sessionMap = new HashMap<String, Session>();
				urlPatterns = new HashMap<String, String>(h.urlPattern);
				timer = new Timer();
				timer.schedule(newSession, 0,7000);
				
					
			} catch (Exception e) {
				//e.printStackTrace();
			}
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
			  
		  } catch (IOException e) {
			  System.out.println("Kill the process id. Command is ps ax | grep HW1 ");
		  }
	  }
	 
	 /* else{
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
	  	}*/
	  newSession = new TimerTask(){

			@Override
			public void run() {
				synchronized (sessionMap) {
					Iterator<Map.Entry<String, Session>> it = sessionMap.entrySet().iterator();
					while(it.hasNext()){
						 
						Map.Entry<String, Session> sMap =it.next();
						Session currentSession = sMap.getValue();
						Date d = new Date();
						if(currentSession.getMaxInactiveInterval()!=1 && (d.getTime()-currentSession.getLastAccessedTime())>1000*currentSession.getMaxInactiveInterval()){
							currentSession.invalidate();
							it.remove();
						}
					}
				}
				
			}
		};
		  
	 }


private static Handler parseWebdotxml(String webdotxml) throws Exception {
	Handler h = new Handler();
	File file = new File(webdotxml);
	if (file.exists() == false) {
		System.err.println("error: cannot find " + file.getPath());
		System.exit(-1);
	}
	SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
	parser.parse(file, h);
	
	return h;
}

private static Context createContext(Handler h) {
	Context fc = new Context();
	for (String param : h.m_contextParams.keySet()) {
		fc.setInitParam(param, h.m_contextParams.get(param));
	}
	return fc;
}

private static HashMap<String,HttpServlet> createServlets(Handler h, Context fc) throws Exception {
	servlets = new HashMap<String,HttpServlet>();
	for (String servletName : h.m_servlets.keySet()) {
		Config config = new Config(servletName, fc);
		String className = h.m_servlets.get(servletName);
		Class servletClass = Class.forName(className);
		HttpServlet servlet = (HttpServlet) servletClass.newInstance();
		HashMap<String,String> servletParams = h.m_servletParams.get(servletName);
		if (servletParams != null) {
			for (String param : servletParams.keySet()) {
				config.setInitParam(param, servletParams.get(param));
			}
		}
		servlet.init(config);
		servlets.put(servletName, servlet);
	}
	return servlets;
}
public static int  getPortNumber() {
	// TODO Auto-generated method stub
	return portNumber;
}


public static  HashMap<String, String> getURLMap() {
	// TODO Auto-generated method stub
	return urlPatterns;
}


public static  HashMap<String, HttpServlet> getServletMap() {
	// TODO Auto-generated method stub
	return servlets;
}


public static HashMap<String, Session> getSessionMap() {
	// TODO Auto-generated method stub
	return sessionMap;
}


}

	

	

  
