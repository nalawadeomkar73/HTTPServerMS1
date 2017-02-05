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
