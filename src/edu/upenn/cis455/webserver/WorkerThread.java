package edu.upenn.cis455.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class WorkerThread extends Thread{
	
	private RequestData requestHttp;
	private ResponseMessage responseHttp;
	private int threadId;
	private String rootDirectory;
	private ThreadPool threadPool;
	private BlockingQueue bq;
	private Socket mySock;
	//private boolean isShutdown;
	//private int ThreadPool.count;
	

	public WorkerThread(int threadId, String rootDirectory, ThreadPool threadPool, BlockingQueue bq) {
		// TODO Auto-generated constructor stub
		this.threadId = threadId;
		this.rootDirectory = rootDirectory;
		this.threadPool = threadPool;
		this.bq = bq;
		this.mySock = new Socket();
		//this.requestHttp = null;
		//this.responseHttp = null;
	}
	
	public void run(){
		while(threadPool.checkThreadPoolRunning()){
		try {
			mySock = bq.dequeue();
			OutputStream outtoClient = mySock.getOutputStream();
			InputStream mySockInput = mySock.getInputStream();
			InputStreamReader mySockInputReader = new InputStreamReader(mySockInput);
			BufferedReader inputData = new BufferedReader(mySockInputReader);
			requestHttp = reqParser(inputData);
			
			
			if(requestHttp == null){
				System.out.println("Invalid HTTP Request");
				outtoClient.write(HTTPHandler.get400StatusMessage().giveHttpResponse().getBytes());
			}
			else if(!requestHttp.isCorrectMessage()){
				System.out.println("Invalid HTTP Request with wrong input message");
				outtoClient.write(HTTPHandler.get400StatusMessage().giveHttpResponse().getBytes());
			}
			else{
				if(requestHttp.getParserMap().containsKey("expect")){
					outtoClient.write(HTTPHandler.get100StatusMessage().giveHttpResponse().getBytes());
				}
				if(requestHttp.getFilePath().contains("http://localhost:"+threadPool.getPortNumber())){
					String newFilePath = requestHttp.getFilePath().substring(("http://localhost:"+threadPool.getPortNumber()).length());
					requestHttp.setFilePath(newFilePath);
				}
				File fp = new File(rootDirectory.concat(requestHttp.getFilePath()));
				String contentOutput ="";
				Map<String,String> requestHttpMessages = new HashMap<String,String>();
				if(fp.exists()){
					if(fp.canRead()==false){
						outtoClient.write(HTTPHandler.get403StatusMessage().giveHttpResponse().getBytes());
					}
					else if(fp.isFile()){
						FileInputStream inputStream = new FileInputStream(fp);
						byte[] bytesArray = new byte[(int) fp.length()];
						
						if(Files.probeContentType(fp.toPath())==null){
							outtoClient.write(HTTPHandler.get404StatusMessage().giveHttpResponse().getBytes());
						}
						else{
							if((requestHttp.getParserMap().containsKey("if-modified-since"))||(requestHttp.getParserMap().containsKey("if-unmodified-since"))){
								Calendar dateWhenModified = new GregorianCalendar();
								Calendar dateWhenFileModified = new GregorianCalendar();
								dateWhenFileModified.setTimeInMillis(fp.lastModified());
								try {
									dateWhenModified.setTime(requestHttp.getParserMap().containsKey("if-modified-since")?HTTPHandler.dateFormat().parse(requestHttp.getParserMap().get("if-modified-since")):HTTPHandler.dateFormat().parse(requestHttp.getParserMap().get("if-unmodified-since")));
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									outtoClient.write(HTTPHandler.get500StatusMessage().giveHttpResponse().getBytes());
								}
								if(requestHttp.getParserMap().containsKey("if-unmodified-since")){
									if(dateWhenFileModified.after(dateWhenModified)){
										outtoClient.write(HTTPHandler.get412StatusMessage().giveHttpResponse().getBytes());
									}
									else{
										outtoClient.write(HTTPHandler.get200StatusMessage().giveHttpResponse().getBytes());
									}
								}
								if(requestHttp.getParserMap().containsKey("if-modified-since")){
									if(!dateWhenFileModified.after(dateWhenModified)){
										outtoClient.write(HTTPHandler.get304StatusMessage().giveHttpResponse().getBytes());
									}
									else{
										outtoClient.write(HTTPHandler.get200StatusMessage().giveHttpResponse().getBytes());
									}
								}
									
							}else{
								contentOutput = new String(bytesArray);
								requestHttpMessages.put("Date", HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
								requestHttpMessages.put("Content-Length",""+contentOutput.length());
								requestHttpMessages.put("Content-type",Files.probeContentType(fp.toPath()) +"; charset=utf-8");
								requestHttpMessages.put("Connection", "Close");
								responseHttp = new ResponseMessage(contentOutput, "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
								if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
									outtoClient.write(responseHttp.giveHttpResponseWithHeaders().getBytes());
								}
								else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
									outtoClient.write(responseHttp.giveHttpResponse().getBytes());
								}
								else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
									outtoClient.write(HTTPHandler.get405StatusMessage().giveHttpResponse().getBytes());
								}
								else{
									outtoClient.write(HTTPHandler.get400StatusMessage().giveHttpResponse().getBytes());
								}
							}
						}
						inputStream.close();
					}
					else if(fp.isDirectory()){
						File[] allFiles = fp.listFiles();
						String htmlStart = "<html><body>";
						String htmlEnd = "</body></html>";
						
						StringBuilder filesInfo = new StringBuilder();
						filesInfo.append(htmlStart);
						filesInfo.append(requestHttp.getFilePath());
						filesInfo.append("<br/>");
						for(File file:allFiles){
							if(!file.getName().endsWith("~")){
								filesInfo.append("<a href=\"http://localhost:"+threadPool.getPortNumber()+requestHttp.getFilePath()+"/"+file.getName()+"\">"+file.getName()+"</a><br/>");	
							}
							
						}
						filesInfo.append(htmlEnd);
						contentOutput = filesInfo.toString();
						requestHttpMessages.put("Date", HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
						requestHttpMessages.put("Content-Length",""+contentOutput.length());
						requestHttpMessages.put("Content-type", "text/html; charset=utf-8");
						requestHttpMessages.put("Connection", "Close");
						responseHttp = new ResponseMessage(contentOutput, "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
						if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
							outtoClient.write(responseHttp.giveHttpResponseWithHeaders().getBytes());
						}
						else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
							outtoClient.write(responseHttp.giveHttpResponse().getBytes());
						}
						else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
							outtoClient.write(HTTPHandler.get405StatusMessage().giveHttpResponse().getBytes());
						}
						else{
							outtoClient.write(HTTPHandler.get400StatusMessage().giveHttpResponse().getBytes());
						}
					}else{
						outtoClient.write(HTTPHandler.get404StatusMessage().giveHttpResponse().getBytes());
					}
				}else{
					if(requestHttp.getFilePath().equalsIgnoreCase("/control")){
						StringBuilder controlOutput = new StringBuilder();
						controlOutput.append("<html><body><br/>Total Worker Threads: "+threadPool.getListOfThreads().size()+"<br/>");
						controlOutput.append("Busy Threads: "+(threadPool.getListOfThreads().size()-threadPool.getThreadPool().size())+"<br/>");
						controlOutput.append("Available Threads: "+(threadPool.getThreadPool().size())+"<br/>");
						controlOutput.append("<a href=\"http://localhost:"+threadPool.getPortNumber()+"/shutdown\">SHUTDOWN</a><br/>");
						controlOutput.append("<br/>All Worker Threads: <br/>");
						for (WorkerThread worker : threadPool.getListOfThreads()){
							if(worker.getState().equals(Thread.State.RUNNABLE)){
								controlOutput.append(worker.threadId+" "+worker.requestHttp.getFilePath()+"<br/>");
							}else{
								controlOutput.append(worker.threadId+" "+worker.getState()+"<br/>");
							}
							
						}
						controlOutput.append("</body></html>");
						contentOutput = controlOutput.toString();
						requestHttpMessages.put("Date", HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
						requestHttpMessages.put("Content-Length",""+contentOutput.length());
						requestHttpMessages.put("Content-type", "text/html; charset=utf-8");
						requestHttpMessages.put("Connection", "Close");
						responseHttp = new ResponseMessage(contentOutput, "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
						if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
							outtoClient.write(responseHttp.giveHttpResponseWithHeaders().getBytes());
						}
						else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
							outtoClient.write(responseHttp.giveHttpResponse().getBytes());
						}
						else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
							outtoClient.write(HTTPHandler.get405StatusMessage().giveHttpResponse().getBytes());
						}
						else{
							outtoClient.write(HTTPHandler.get400StatusMessage().giveHttpResponse().getBytes());
						}
						
					}else if(requestHttp.getFilePath().equalsIgnoreCase("/shutdown")){
						threadPool.setRunningStatus(false);
						for (WorkerThread worker : threadPool.getListOfThreads()){
							worker.interrupt();
						}
						Shutdown sd = new Shutdown();
						sd.start();
						//spawn a new thread
						requestHttpMessages.put("Date", HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
						requestHttpMessages.put("Content-Length",""+contentOutput.length());
						requestHttpMessages.put("Content-type", "text/html; charset=utf-8");
						requestHttpMessages.put("Connection", "Close");
						responseHttp = new ResponseMessage(contentOutput, "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
						if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
							outtoClient.write(responseHttp.giveHttpResponseWithHeaders().getBytes());
						}
						else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
							outtoClient.write(responseHttp.giveHttpResponse().getBytes());
						}
						else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
							outtoClient.write(HTTPHandler.get405StatusMessage().giveHttpResponse().getBytes());
						}
						else{
							outtoClient.write(HTTPHandler.get400StatusMessage().giveHttpResponse().getBytes());
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
			
			
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("Dequeuing not works from the blocking queue");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Outputstream did not work");
		}
	}
		ThreadPool.count++;
	}
	

	private RequestData reqParser(BufferedReader inputData) {
		// TODO Auto-generated method stub
		StringBuilder readInputData = new StringBuilder();
		String input = "";
		try {
			while (((input = inputData.readLine())!=null && !input.equals(""))){
				readInputData.append(input + "\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Nothing to read from the BufferedReader");
		}
		RequestData reqHTTP = null;
		if(readInputData!=null){
			reqHTTP = new RequestData(readInputData.toString());
			
		}
		return reqHTTP;
	}

	
}
