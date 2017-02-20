package edu.upenn.cis455.webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import org.apache.tools.ant.taskdefs.condition.Http;

public class WorkerThread extends Thread{
	
	private RequestData requestHttp;
	private ResponseMessage responseHttp;
	private int threadId;
	private String rootDirectory;
	private ThreadPool threadPool;
	private BlockingQueue bq;
	private Socket mySock;
	private String hrefPath;
	
	

	public WorkerThread(int threadId, String rootDirectory, ThreadPool threadPool, BlockingQueue bq) {

		this.threadId = threadId;
		this.rootDirectory = rootDirectory;
		this.threadPool = threadPool;
		this.bq = bq;
		this.mySock = new Socket();
		this.requestHttp = null;
		this.responseHttp = null;
	}
	
	public void run(){
		while(threadPool.checkThreadPoolRunning()){
		try {
			mySock = bq.dequeue();
			DataOutputStream outtoClient = new DataOutputStream(mySock.getOutputStream());
			InputStream mySockInput = mySock.getInputStream();
			InputStreamReader mySockInputReader = new InputStreamReader(mySockInput);
			BufferedReader inputData = new BufferedReader(mySockInputReader);
			requestHttp = reqParser(inputData);
			if(requestHttp == null){
				
				outtoClient.write(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
			}
			
			else if(!requestHttp.isCorrectMessage()){
				
				outtoClient.write(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
				
			}
			
			else{
				if(requestHttp.getFilePath().contains("http://localhost:"+threadPool.getPortNumber()))
				{
					
					requestHttp.setFilePath(requestHttp.getFilePath().substring(("http://localhost:"+threadPool.getPortNumber()).length()));
				}
				
				if((requestHttp.getParserMap().containsKey("expect"))&&(requestHttp.getVersionNumber().equals("1.1"))){
					if(requestHttp.getParserMap().get("expect").equals("100-continue")){
						String output = new String(("HTTP"+"/"+requestHttp.getVersionNumber()+" "+"100"+" "+"Continue"+"\r\n"+"\r\n"));
						outtoClient.write(output.getBytes());
					}
				}
				String absolutePath = rootDirectory+requestHttp.getFilePath();
				hrefPath = requestHttp.getFilePath();
				String finalPath = getRequiredPath(absolutePath);
				if(!finalPath.startsWith(rootDirectory)){
					outtoClient.write(HTTPHandler.get403StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
				}
				
				else{
				
				requestHttp.setFilePath(finalPath);
				
				if(HttpServer.getURLMap()!= null){
					int longestLength = -1;
					System.out.println("Finally found a servlet");
					String urlMatch = "";
					for(Map.Entry<String, String> mapElement : HttpServer.getURLMap().entrySet()){
						String fpath;
						Pattern url = Pattern.compile(mapElement.getKey());
						if(requestHttp.getFilePath().contains("?")){
							fpath = requestHttp.getFilePath().split("\\?")[0];
						}
						else{
							fpath = requestHttp.getFilePath();
						}
						if(url.matcher(fpath).matches())
						{
							int actLen = mapElement.getKey().contains("*")?mapElement.getKey().indexOf("*")-1:mapElement.getKey().length();
							if(actLen>longestLength)
							{
								urlMatch = mapElement.getKey();
								longestLength = actLen;
							
							}
						}
					}
				
				if(HttpServer.getURLMap().containsKey(urlMatch)){
					String servlet = HttpServer.getURLMap().get(urlMatch);
					if(HttpServer.getServletMap().containsKey(servlet)){
						requestHttp.setServletPath(urlMatch);
						requestHttp.parsePath();
						responseHttp = new ResponseMessage("HTTP","200","OK");
						responseHttp.setVersion(requestHttp.getVersionNumber());
						Request req = new Request(requestHttp);
						Response resp= new Response(responseHttp, req);
						req.setparameters(requestHttp.propertiesMap());
						req.setCookies(requestHttp.getCookies());
						for(Cookie c : req.getCookies()){
							if(c.getName().equalsIgnoreCase("JSESSIONID"))
							{
								synchronized(HttpServer.getSessionMap())
								{
									if(HttpServer.getSessionMap().containsKey(c.getValue()))
									{
										Session session = HttpServer.getSessionMap().get(c.getValue());
										session.update();
										req.saveSession(session);
									}
								}
								break;
							}
						}
						req.setSocket(mySock);
						resp.setSocket(mySock);
						try {
							HttpServer.getServletMap().get(servlet).service(req, resp);
						} catch (ServletException e) {
							// TODO Auto-generated catch block
							System.out.println("In the worker thread");
						}
						inputData.close();
						mySockInputReader.close();
						mySockInput.close();
						outtoClient.flush();
						outtoClient.close();
						mySock.close();	
						requestHttp = null;
						responseHttp= null;
						continue;
					}
					
				}
				}
				File fp = new File(requestHttp.getFilePath());
				String contentOutput ="";
				Map<String,ArrayList<String>> requestHttpMessages = new HashMap<String,ArrayList<String>>();
				ArrayList<String> headerValues;
				if(fp.exists()){
					if(fp.canRead()==false){
						outtoClient.write(HTTPHandler.get403StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
					}

					else if(fp.isFile()){
						FileInputStream inputStream = new FileInputStream(fp);
						byte[] bytesArray = new byte[(int) fp.length()];
						if(inputStream.read(bytesArray, 0, bytesArray.length)!=fp.length())
						{
							outtoClient.write(HTTPHandler.get500StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
						}
						else{
							if(Files.probeContentType(fp.toPath())==null){
								outtoClient.write(HTTPHandler.get404StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
							}
							else{
								if((requestHttp.getParserMap().containsKey("if-modified-since"))||(requestHttp.getParserMap().containsKey("if-unmodified-since"))){
									Calendar dateWhenModified = new GregorianCalendar();
									Calendar dateWhenFileModified = new GregorianCalendar();
									dateWhenFileModified.setTimeInMillis(fp.lastModified());
									
									
									if(requestHttp.getParserMap().containsKey("if-modified-since")){
										dateWhenModified =  parseDateMethod(requestHttp.getParserMap().get("if-modified-since").get(0));
									}
									else{
										dateWhenModified =  parseDateMethod(requestHttp.getParserMap().get("if-unmodified-since").get(0));
									}
									
									if(dateWhenModified!=null){
									
									//dateWhenModified.setTime(requestHttp.getParserMap().containsKey("if-modified-since")?HTTPHandler.dateFormat().parse(requestHttp.getParserMap().get("if-modified-since")):HTTPHandler.dateFormat().parse(requestHttp.getParserMap().get("if-unmodified-since")));

									if(requestHttp.getParserMap().containsKey("if-unmodified-since")){
										if(dateWhenFileModified.after(dateWhenModified)){
											outtoClient.write(HTTPHandler.get412StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
										}
										else{
											contentOutput = new String(bytesArray);
											headerValues = new ArrayList<String>();
											headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
											requestHttpMessages.put("Date", headerValues);
											headerValues = new ArrayList<String>();
											headerValues.add(""+contentOutput.length());
											requestHttpMessages.put("Content-Length",headerValues);
											headerValues = new ArrayList<String>();
											headerValues.add(Files.probeContentType(fp.toPath()) +"; charset=utf-8");
											requestHttpMessages.put("Content-type",headerValues);
											headerValues = new ArrayList<String>();
											headerValues.add("Close");
											requestHttpMessages.put("Connection", headerValues);
											responseHttp = new ResponseMessage(bytesArray, "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
											if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
												outtoClient.write(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
											}
											else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
												outtoClient.write(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
											}
											else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
												outtoClient.write(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
											}
											else{
												outtoClient.write(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
											}
										}	
									}
									if(requestHttp.getParserMap().containsKey("if-modified-since")){
										if(!dateWhenFileModified.after(dateWhenModified)){
										//requestHttpMessages.put("Content-type",Files.probeContentType(fp.toPath()) +"; charset=utf-8");
										//responseHttp = new ResponseMessage(contentOutput, "304", HTTPHandler.getHttpResponseMessages().get("304"), requestHttpMessages);
											//System.out.println(HTTPHandler.get304StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()).toString());
											outtoClient.write(HTTPHandler.get304StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
										}
										else{
											contentOutput = new String(bytesArray);
											
											headerValues = new ArrayList<String>();
											headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
											requestHttpMessages.put("Date", headerValues);
											headerValues = new ArrayList<String>();
											headerValues.add(""+contentOutput.length());
											requestHttpMessages.put("Content-Length",headerValues);
											headerValues = new ArrayList<String>();
											headerValues.add(Files.probeContentType(fp.toPath()) +"; charset=utf-8");
											requestHttpMessages.put("Content-type",headerValues);
											headerValues = new ArrayList<String>();
											headerValues.add("Close");
											requestHttpMessages.put("Connection", headerValues);
											headerValues = new ArrayList<String>();
											headerValues.add(HTTPHandler.dateFormat().format(fp.lastModified()));
											requestHttpMessages.put("Last-Modified",headerValues);
										
											responseHttp = new ResponseMessage(bytesArray, "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
											if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
												outtoClient.write(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
											}
											else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
												outtoClient.write(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
											}
											else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
												outtoClient.write(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
											}
											else{
												outtoClient.write(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
											}
										}
									}
									}else{
										contentOutput = new String(bytesArray);
										
										headerValues = new ArrayList<String>();
										headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
										requestHttpMessages.put("Date", headerValues);
										headerValues = new ArrayList<String>();
										headerValues.add(""+contentOutput.length());
										requestHttpMessages.put("Content-Length",headerValues);
										headerValues = new ArrayList<String>();
										headerValues.add(Files.probeContentType(fp.toPath()) +"; charset=utf-8");
										requestHttpMessages.put("Content-type",headerValues);
										headerValues = new ArrayList<String>();
										headerValues.add("Close");
										requestHttpMessages.put("Connection", headerValues);
										headerValues = new ArrayList<String>();
										headerValues.add(HTTPHandler.dateFormat().format(fp.lastModified()));
										requestHttpMessages.put("Last-Modified",headerValues);
									
										
						
										responseHttp = new ResponseMessage(bytesArray, "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
										if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
											outtoClient.write(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
										}
										else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
											outtoClient.write(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
										}
										else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
											outtoClient.write(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
										}
										else{
											outtoClient.write(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
										}
										
									}
									
							}else{
								contentOutput = new String(bytesArray);
								
								headerValues = new ArrayList<String>();
								headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
								requestHttpMessages.put("Date", headerValues);
								headerValues = new ArrayList<String>();
								headerValues.add(""+contentOutput.length());
								requestHttpMessages.put("Content-Length",headerValues);
								headerValues = new ArrayList<String>();
								headerValues.add(Files.probeContentType(fp.toPath()) +"; charset=utf-8");
								requestHttpMessages.put("Content-type",headerValues);
								headerValues = new ArrayList<String>();
								headerValues.add("Close");
								requestHttpMessages.put("Connection", headerValues);
								headerValues = new ArrayList<String>();
								headerValues.add(HTTPHandler.dateFormat().format(fp.lastModified()));
								requestHttpMessages.put("Last-Modified",headerValues);
							
								responseHttp = new ResponseMessage(bytesArray, "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
								if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
									outtoClient.write(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
								}
								else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
									outtoClient.write(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
								}
								else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
									outtoClient.write(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
								}
								else{
									outtoClient.write(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
								}
							}
						}
						
					}
						inputStream.close();
					}
					else if(fp.isDirectory()){
						if((requestHttp.getParserMap().containsKey("if-modified-since"))||(requestHttp.getParserMap().containsKey("if-unmodified-since"))){
							Calendar dateWhenModified = new GregorianCalendar();
							Calendar dateWhenFileModified = new GregorianCalendar();
							dateWhenFileModified.setTimeInMillis(fp.lastModified());
						
							if(requestHttp.getParserMap().containsKey("if-modified-since")){
								dateWhenModified =  parseDateMethod(requestHttp.getParserMap().get("if-modified-since").get(0));
							}
							else{
								dateWhenModified =  parseDateMethod(requestHttp.getParserMap().get("if-unmodified-since").get(0));
							}
							
							
							if(dateWhenModified!=null){
							
							
							
							
							if(requestHttp.getParserMap().containsKey("if-unmodified-since")){
								if(dateWhenFileModified.after(dateWhenModified)){
								
									outtoClient.write(HTTPHandler.get412StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
								}
								else{
									File[] allFiles = fp.listFiles();
									String htmlStart = "<html><body>";
									String htmlEnd = "</body></html>";
									
									StringBuilder filesInfo = new StringBuilder();
									filesInfo.append(htmlStart);
									filesInfo.append(hrefPath);
								
									filesInfo.append("<br/>");
									for(File file:allFiles){
										if(!file.getName().endsWith("~")){
											
											filesInfo.append("<a href=\"http://localhost:"+threadPool.getPortNumber()+hrefPath+"/"+file.getName()+"\">"+file.getName()+"</a><br/>");	
											
										}
										
									}
									filesInfo.append(htmlEnd);
									contentOutput = filesInfo.toString();
									
									headerValues = new ArrayList<String>();
									headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
									requestHttpMessages.put("Date", headerValues);
									headerValues = new ArrayList<String>();
									headerValues.add(""+contentOutput.length());
									requestHttpMessages.put("Content-Length",headerValues);
									headerValues = new ArrayList<String>();
									headerValues.add("text/html; charset=utf-8");
									requestHttpMessages.put("Content-type",headerValues);
									headerValues = new ArrayList<String>();
									headerValues.add("Close");
									requestHttpMessages.put("Connection", headerValues);
									headerValues = new ArrayList<String>();
									headerValues.add(HTTPHandler.dateFormat().format(fp.lastModified()));
									requestHttpMessages.put("Last-Modified",headerValues);
								
							
									responseHttp = new ResponseMessage(contentOutput.getBytes(), "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
									if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
										outtoClient.write(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
									}
									else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
										outtoClient.write(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
									}
									else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
										outtoClient.write(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
									}
									else{
										outtoClient.write(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
									}
								}	
							}
							
							if(requestHttp.getParserMap().containsKey("if-modified-since")){
								if(!dateWhenFileModified.after(dateWhenModified)){
								
									outtoClient.write(HTTPHandler.get304StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
								}
								else{
									File[] allFiles = fp.listFiles();
									String htmlStart = "<html><body>";
									String htmlEnd = "</body></html>";
									
									StringBuilder filesInfo = new StringBuilder();
									filesInfo.append(htmlStart);
									filesInfo.append(hrefPath);
								
									filesInfo.append("<br/>");
									for(File file:allFiles){
										if(!file.getName().endsWith("~")){
											
											filesInfo.append("<a href=\"http://localhost:"+threadPool.getPortNumber()+hrefPath+"/"+file.getName()+"\">"+file.getName()+"</a><br/>");	
											
										}
										
									}
									filesInfo.append(htmlEnd);
									contentOutput = filesInfo.toString();
									
									headerValues = new ArrayList<String>();
									headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
									requestHttpMessages.put("Date", headerValues);
									headerValues = new ArrayList<String>();
									headerValues.add(""+contentOutput.length());
									requestHttpMessages.put("Content-Length",headerValues);
									headerValues = new ArrayList<String>();
									headerValues.add("text/html; charset=utf-8");
									requestHttpMessages.put("Content-type",headerValues);
									headerValues = new ArrayList<String>();
									headerValues.add("Close");
									requestHttpMessages.put("Connection", headerValues);
									headerValues = new ArrayList<String>();
									headerValues.add(HTTPHandler.dateFormat().format(fp.lastModified()));
									requestHttpMessages.put("Last-Modified",headerValues);
									
									responseHttp = new ResponseMessage(contentOutput.getBytes(), "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
									if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
										outtoClient.write(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
									}
									else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
										outtoClient.write(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
									}
									else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
										outtoClient.write(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
									}
									else{
										outtoClient.write(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
									}
								}
							}
							}else{
								File[] allFiles = fp.listFiles();
								String htmlStart = "<html><body>";
								String htmlEnd = "</body></html>";
								
								StringBuilder filesInfo = new StringBuilder();
								filesInfo.append(htmlStart);
								filesInfo.append(hrefPath);
							
								filesInfo.append("<br/>");
								for(File file:allFiles){
									if(!file.getName().endsWith("~")){
										
										filesInfo.append("<a href=\"http://localhost:"+threadPool.getPortNumber()+hrefPath+"/"+file.getName()+"\">"+file.getName()+"</a><br/>");	
										
									}
									
								}
								filesInfo.append(htmlEnd);
								contentOutput = filesInfo.toString();
								
								headerValues = new ArrayList<String>();
								headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
								requestHttpMessages.put("Date", headerValues);
								headerValues = new ArrayList<String>();
								headerValues.add(""+contentOutput.length());
								requestHttpMessages.put("Content-Length",headerValues);
								headerValues = new ArrayList<String>();
								headerValues.add("text/html; charset=utf-8");
								requestHttpMessages.put("Content-type",headerValues);
								headerValues = new ArrayList<String>();
								headerValues.add("Close");
								requestHttpMessages.put("Connection", headerValues);
								headerValues = new ArrayList<String>();
								headerValues.add(HTTPHandler.dateFormat().format(fp.lastModified()));
								requestHttpMessages.put("Last-Modified",headerValues);

								responseHttp = new ResponseMessage(contentOutput.getBytes(), "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
								if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
									outtoClient.write(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
								}
								else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
									outtoClient.write(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
								}
								else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
									outtoClient.write(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
								}
								else{
									outtoClient.write(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
								}
							}
							
							
						}
						else{
						File[] allFiles = fp.listFiles();
						String htmlStart = "<html><body>";
						String htmlEnd = "</body></html>";
						
						StringBuilder filesInfo = new StringBuilder();
						filesInfo.append(htmlStart);
						filesInfo.append(hrefPath);
					
						filesInfo.append("<br/>");
						for(File file:allFiles){
							if(!file.getName().endsWith("~")){
								
								filesInfo.append("<a href=\"http://localhost:"+threadPool.getPortNumber()+hrefPath+"/"+file.getName()+"\">"+file.getName()+"</a><br/>");	
								
							}
							
						}
						filesInfo.append(htmlEnd);
						contentOutput = filesInfo.toString();
						
						headerValues = new ArrayList<String>();
						headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
						requestHttpMessages.put("Date", headerValues);
						headerValues = new ArrayList<String>();
						headerValues.add(""+contentOutput.length());
						requestHttpMessages.put("Content-Length",headerValues);
						headerValues = new ArrayList<String>();
						headerValues.add("text/html; charset=utf-8");
						requestHttpMessages.put("Content-type",headerValues);
						headerValues = new ArrayList<String>();
						headerValues.add("Close");
						requestHttpMessages.put("Connection", headerValues);
						headerValues = new ArrayList<String>();
						headerValues.add(HTTPHandler.dateFormat().format(fp.lastModified()));
						requestHttpMessages.put("Last-Modified",headerValues);
						
						responseHttp = new ResponseMessage(contentOutput.getBytes(), "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
						if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
							outtoClient.write(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
						}
						else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
							outtoClient.write(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
						}
						else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
							outtoClient.write(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
						}
						else{
							outtoClient.write(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
						}
						}
					}else{
						outtoClient.write(HTTPHandler.get404StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
					}
				}else{
					if(hrefPath.equalsIgnoreCase("/control")){
						StringBuilder controlOutput = new StringBuilder();
						controlOutput.append("<html><body><br/>Total Worker Threads: "+threadPool.getListOfThreads().size()+"<br/>");
						controlOutput.append("Busy Threads: "+(threadPool.getListOfThreads().size()-threadPool.getThreadPool().size())+"<br/>");
						controlOutput.append("Available Threads: "+(threadPool.getThreadPool().size())+"<br/>");
						controlOutput.append("<a href=\"http://localhost:"+threadPool.getPortNumber()+"/shutdown\">SHUTDOWN</a><br/>");
						controlOutput.append("<br/>All Worker Threads: <br/>");
						for (WorkerThread worker : threadPool.getListOfThreads()){
							if(worker.getState().equals(Thread.State.RUNNABLE)){
								controlOutput.append(worker.threadId+" "+worker.hrefPath+"<br/>");
							}else{
								controlOutput.append(worker.threadId+" "+worker.getState()+"<br/>");
							}
							
						}
						controlOutput.append("</body></html>");
						contentOutput = controlOutput.toString();
						
						headerValues = new ArrayList<String>();
						headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
						requestHttpMessages.put("Date", headerValues);
						headerValues = new ArrayList<String>();
						headerValues.add(""+contentOutput.length());
						requestHttpMessages.put("Content-Length",headerValues);
						headerValues = new ArrayList<String>();
						headerValues.add("text/html; charset=utf-8");
						requestHttpMessages.put("Content-type",headerValues);
						headerValues = new ArrayList<String>();
						headerValues.add("Close");
						requestHttpMessages.put("Connection", headerValues);
						
						responseHttp = new ResponseMessage(contentOutput.getBytes(), "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
						if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
							outtoClient.write(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
						}
						else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
							outtoClient.write(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
						}
						else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
							outtoClient.write(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
						}
						else{
							outtoClient.write(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
						}
						
					}else if(hrefPath.equalsIgnoreCase("/shutdown")){
						threadPool.setRunningStatus(false);
						
						
						//spawn a new thread
						headerValues = new ArrayList<String>();
						headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
						requestHttpMessages.put("Date", headerValues);
						headerValues = new ArrayList<String>();
						headerValues.add(""+contentOutput.length());
						requestHttpMessages.put("Content-Length",headerValues);
						headerValues = new ArrayList<String>();
						headerValues.add("text/html; charset=utf-8");
						requestHttpMessages.put("Content-type",headerValues);
						headerValues = new ArrayList<String>();
						headerValues.add("Close");
						requestHttpMessages.put("Connection", headerValues);
						
						
						responseHttp = new ResponseMessage(contentOutput.getBytes(), "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
						if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
							outtoClient.write(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
						}
						else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
							outtoClient.write(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
						}
						else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
							outtoClient.write(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
						}
						else{
							outtoClient.write(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
						}
						ShutdownThread sd = new ShutdownThread(threadPool);
						sd.start();
						for (WorkerThread worker : threadPool.getListOfThreads()){
							worker.interrupt();
						}
						
					}
					else{
						outtoClient.write(HTTPHandler.get404StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
					}
				}
				
			}
			
			}	
			inputData.close();
			mySockInputReader.close();
			mySockInput.close();
			outtoClient.flush();
			outtoClient.close();
			mySock.close();	
			requestHttp = null;
			responseHttp= null;
			
		} catch (InterruptedException e) {
			
		} catch (IOException e) {
			
		} catch (NullPointerException e){
		} 
	}
		
		ThreadPool.count++;
		System.out.println(ThreadPool.count);
	}
	

	private Calendar parseDateMethod(String dateWhenFileModified) {
		Calendar cd = new GregorianCalendar();
		Date d1 = null;
		String formats[] = {"EEE, dd MMM yyyy HH:mm:ss z","EEEE, dd-MMM-yy HH:mm:ss z","EEE MMM dd HH:mm:ss yyyy"};
		SimpleDateFormat sd = null;
		try{
			sd = new SimpleDateFormat(formats[0]);
			sd.setTimeZone(TimeZone.getTimeZone("GMT"));
			d1 = sd.parse(dateWhenFileModified);
			
		}
		catch(Exception e){
			
		}
		if(d1!=null){
			cd.setTime(d1);
			return cd;
		}
		
		try{
			sd = new SimpleDateFormat(formats[1]);
			sd.setTimeZone(TimeZone.getTimeZone("GMT"));
			d1 = sd.parse(dateWhenFileModified);
			
		}
		catch(Exception e){
			
		}
		if(d1!=null){
			cd.setTime(d1);
			return cd;
		}
		try{
			sd = new SimpleDateFormat(formats[2]);
			sd.setTimeZone(TimeZone.getTimeZone("GMT"));
			d1 = sd.parse(dateWhenFileModified);
			
		}
		catch(Exception e){
			
		}
		if(d1!=null){
			cd.setTime(d1);
			return cd;
		}
		return null;
		
	}

	private RequestData reqParser(BufferedReader inputData) throws IOException {
		StringBuilder readInputData = new StringBuilder();
		String input = "";
		try {
			while (((input = inputData.readLine())!=null && !input.equals(""))){
				readInputData.append(input + "\n");
			}
		} catch (IOException e) {
		}
		RequestData reqHTTP = null;
		if(readInputData!=null){
			reqHTTP = new RequestData(readInputData.toString());
			if(reqHTTP.getMethodName().equals("POST")){
				if(reqHTTP.getParserMap().containsKey("content-length")){
					int contentLen = Integer.valueOf(reqHTTP.getParserMap().get("content-length").get(0));
					char[] contentData = new char[contentLen];
					if(inputData.read(contentData)== contentLen){
						String contentBody = new String(contentData);
						reqHTTP.setContentBody(contentBody);
					}
					
				}
			}
			
		}
		return reqHTTP;
	}
	
	public String getRequiredPath(String path) {
	    Stack<String> stack = new Stack<String>();
	 
	    while(path.length()> 0 && path.charAt(path.length()-1) =='/'){
	        path = path.substring(0, path.length()-1);
	    }
	 
	    int start = 0;
	    for(int i=1; i<path.length(); i++){
	        if(path.charAt(i) == '/'){
	            stack.push(path.substring(start, i));
	            start = i;
	        }else if(i==path.length()-1){
	            stack.push(path.substring(start));
	        }
	    }
	 
	    LinkedList<String> result = new LinkedList<String>();
	    int back = 0;
	    while(!stack.isEmpty()){
	        String top = stack.pop();
	 
	        if(top.equals("/.") || top.equals("/")){
	           
	        }else if(top.equals("/..")){
	            back++;
	        }else{
	            if(back > 0){
	                back--;
	            }else{
	                result.push(top);
	            }
	        }
	    }
	    if(result.isEmpty()){
	        return "/";
	    }
	 
	    StringBuilder sb = new StringBuilder();
	    while(!result.isEmpty()){
	        String s = result.pop();
	        sb.append(s);
	    }
	 
	    return sb.toString();
	}

	
}
