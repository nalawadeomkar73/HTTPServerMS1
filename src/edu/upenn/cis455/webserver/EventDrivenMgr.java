package edu.upenn.cis455.webserver;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import java.util.TimeZone;

public class EventDrivenMgr {

	private static String hrefEventPath;
	private static ResponseMessage responseHttp;
	@SuppressWarnings("null")
	public static byte[] getResponse(RequestData requestHttp,String rootDirectory, int portNumber) {
		byte[] responseMsg = null;
		responseHttp = null;
		if(requestHttp == null){
			
			responseMsg = (HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
		}
		
		else if(!requestHttp.isCorrectMessage()){
			
			
			responseMsg=(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
			
		}
		
		else{
			if(requestHttp.getFilePath().contains("http://localhost:"+portNumber))
			{
				
				requestHttp.setFilePath(requestHttp.getFilePath().substring(("http://localhost:"+portNumber).length()));
			}
			
			if((requestHttp.getParserMap().containsKey("expect"))&&(requestHttp.getVersionNumber().equals("1.1"))){
				if(requestHttp.getParserMap().get("expect").equals("100-continue")){
					responseMsg=(HTTPHandler.get100StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
				}
			}
			String absolutePath = rootDirectory+requestHttp.getFilePath();
			hrefEventPath = requestHttp.getFilePath();
			String finalPath = getEventRequiredPath(absolutePath);
			if(!finalPath.startsWith(rootDirectory)){
				responseMsg=(HTTPHandler.get403StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
			}
			
			else{
			
			requestHttp.setFilePath(finalPath);
			File fp = new File(requestHttp.getFilePath());
			String contentOutput ="";
			Map<String,String> requestHttpMessages = new HashMap<String,String>();
			if(fp.exists()){
				if(fp.canRead()==false){
					responseMsg=(HTTPHandler.get403StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
				}

				else if(fp.isFile()){
					try{
					FileInputStream inputStream = new FileInputStream(fp);
					byte[] bytesArray = new byte[(int) fp.length()];
					if(inputStream.read(bytesArray, 0, bytesArray.length)!=fp.length())
					{
						responseMsg=(HTTPHandler.get500StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
					}
					else{
						if(Files.probeContentType(fp.toPath())==null){
							responseMsg=(HTTPHandler.get404StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
						}
						else{
							if((requestHttp.getParserMap().containsKey("if-modified-since"))||(requestHttp.getParserMap().containsKey("if-unmodified-since"))){
								Calendar dateWhenModified = new GregorianCalendar();
								Calendar dateWhenFileModified = new GregorianCalendar();
								dateWhenFileModified.setTimeInMillis(fp.lastModified());
								
								
								if(requestHttp.getParserMap().containsKey("if-modified-since")){
									dateWhenModified =  parseEventDateMethod(requestHttp.getParserMap().get("if-modified-since"));
								}
								else{
									dateWhenModified =  parseEventDateMethod(requestHttp.getParserMap().get("if-unmodified-since"));
								}
								
								if(dateWhenModified!=null){
								
								//dateWhenModified.setTime(requestHttp.getParserMap().containsKey("if-modified-since")?HTTPHandler.dateFormat().parse(requestHttp.getParserMap().get("if-modified-since")):HTTPHandler.dateFormat().parse(requestHttp.getParserMap().get("if-unmodified-since")));

								if(requestHttp.getParserMap().containsKey("if-unmodified-since")){
									if(dateWhenFileModified.after(dateWhenModified)){
										responseMsg=(HTTPHandler.get412StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
									}
									else{
										contentOutput = new String(bytesArray);
										requestHttpMessages.put("Date", HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
										requestHttpMessages.put("Content-Length",""+contentOutput.length());
										requestHttpMessages.put("Content-type",Files.probeContentType(fp.toPath()) +"; charset=utf-8");
										requestHttpMessages.put("Connection", "Close");
										responseHttp  = new ResponseMessage(bytesArray, "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
										if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
											responseMsg=(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
										}
										else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
											responseMsg=(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
										}
										else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
											responseMsg=(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
										}
										else{
											responseMsg=(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
										}
									}	
								}
								if(requestHttp.getParserMap().containsKey("if-modified-since")){
									if(!dateWhenFileModified.after(dateWhenModified)){
									//requestHttpMessages.put("Content-type",Files.probeContentType(fp.toPath()) +"; charset=utf-8");
									//responseHttp = new ResponseMessage(contentOutput, "304", HTTPHandler.getHttpResponseMessages().get("304"), requestHttpMessages);
										//System.out.println(HTTPHandler.get304StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()).toString());
										responseMsg=(HTTPHandler.get304StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
									}
									else{
										contentOutput = new String(bytesArray);
										requestHttpMessages.put("Date", HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
										requestHttpMessages.put("Content-Length",""+contentOutput.length());
										requestHttpMessages.put("Content-type",Files.probeContentType(fp.toPath()) +"; charset=utf-8");
										requestHttpMessages.put("Connection", "Close");
										requestHttpMessages.put("Last-Modified",HTTPHandler.dateFormat().format(fp.lastModified()));
										responseHttp = new ResponseMessage(bytesArray, "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
										if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
											responseMsg=(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
										}
										else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
											responseMsg=(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
										}
										else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
											responseMsg=(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
										}
										else{
											responseMsg=(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
										}
									}
								}
								}else{
									contentOutput = new String(bytesArray);
									requestHttpMessages.put("Date", HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
									requestHttpMessages.put("Content-Length",""+contentOutput.length());
									requestHttpMessages.put("Content-type",Files.probeContentType(fp.toPath()) +"; charset=utf-8");
									requestHttpMessages.put("Connection", "Close");
									requestHttpMessages.put("Last-Modified",HTTPHandler.dateFormat().format(fp.lastModified()));
									responseHttp = new ResponseMessage(bytesArray, "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
									if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
										responseMsg=(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
									}
									else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
										responseMsg=(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
									}
									else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
										responseMsg=(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
									}
									else{
										responseMsg=(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
									}
									
								}
								
						}else{
							contentOutput = new String(bytesArray);
							requestHttpMessages.put("Date", HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
							requestHttpMessages.put("Content-Length",""+contentOutput.length());
							requestHttpMessages.put("Content-type",Files.probeContentType(fp.toPath()) +"; charset=utf-8");
							requestHttpMessages.put("Connection", "Close");
							requestHttpMessages.put("Last-Modified",HTTPHandler.dateFormat().format(fp.lastModified()));
							responseHttp = new ResponseMessage(bytesArray, "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
							if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
								responseMsg=(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
							}
							else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
								responseMsg=(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
							}
							else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
								responseMsg=(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
							}
							else{
								responseMsg=(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
							}
						}
					}
					
				}
					inputStream.close();
				}catch(Exception e){
					responseMsg=(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
				}
				}
				else if(fp.isDirectory()){
					if((requestHttp.getParserMap().containsKey("if-modified-since"))||(requestHttp.getParserMap().containsKey("if-unmodified-since"))){
						Calendar dateWhenModified = new GregorianCalendar();
						Calendar dateWhenFileModified = new GregorianCalendar();
						dateWhenFileModified.setTimeInMillis(fp.lastModified());
					//	dateWhenModified.setTime(requestHttp.getParserMap().containsKey("if-modified-since")?HTTPHandler.dateFormat().parse(requestHttp.getParserMap().get("if-modified-since")):HTTPHandler.dateFormat().parse(requestHttp.getParserMap().get("if-unmodified-since")));
						
						if(requestHttp.getParserMap().containsKey("if-modified-since")){
							dateWhenModified =  parseEventDateMethod(requestHttp.getParserMap().get("if-modified-since"));
						}
						else{
							dateWhenModified =  parseEventDateMethod(requestHttp.getParserMap().get("if-unmodified-since"));
						}
						
						
						if(dateWhenModified!=null){
						
						
						
						
						if(requestHttp.getParserMap().containsKey("if-unmodified-since")){
							if(dateWhenFileModified.after(dateWhenModified)){
							
								responseMsg=(HTTPHandler.get412StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
							}
							else{
								File[] allFiles = fp.listFiles();
								String htmlStart = "<html><body>";
								String htmlEnd = "</body></html>";
								
								StringBuilder filesInfo = new StringBuilder();
								filesInfo.append(htmlStart);
								filesInfo.append(hrefEventPath);
							
								filesInfo.append("<br/>");
								for(File file:allFiles){
									if(!file.getName().endsWith("~")){
										
										filesInfo.append("<a href=\"http://localhost:"+portNumber+hrefEventPath+"/"+file.getName()+"\">"+file.getName()+"</a><br/>");	
										
									}
									
								}
								filesInfo.append(htmlEnd);
								contentOutput = filesInfo.toString();
								requestHttpMessages.put("Date", HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
								requestHttpMessages.put("Content-Length",""+contentOutput.length());
								requestHttpMessages.put("Content-type", "text/html; charset=utf-8");
								requestHttpMessages.put("Connection", "Close");
								requestHttpMessages.put("Last-Modified",HTTPHandler.dateFormat().format(fp.lastModified()));
								responseHttp = new ResponseMessage(contentOutput.getBytes(), "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
								if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
									responseMsg=(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
								}
								else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
									responseMsg=(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
								}
								else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
									responseMsg=(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
								}
								else{
									responseMsg=(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
								}
							}	
						}
						
						if(requestHttp.getParserMap().containsKey("if-modified-since")){
							if(!dateWhenFileModified.after(dateWhenModified)){
							
								responseMsg=(HTTPHandler.get304StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
							}
							else{
								File[] allFiles = fp.listFiles();
								String htmlStart = "<html><body>";
								String htmlEnd = "</body></html>";
								
								StringBuilder filesInfo = new StringBuilder();
								filesInfo.append(htmlStart);
								filesInfo.append(hrefEventPath);
							
								filesInfo.append("<br/>");
								for(File file:allFiles){
									if(!file.getName().endsWith("~")){
										
										filesInfo.append("<a href=\"http://localhost:"+portNumber+hrefEventPath+"/"+file.getName()+"\">"+file.getName()+"</a><br/>");	
										
									}
									
								}
								filesInfo.append(htmlEnd);
								contentOutput = filesInfo.toString();
								requestHttpMessages.put("Date", HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
								requestHttpMessages.put("Content-Length",""+contentOutput.length());
								requestHttpMessages.put("Content-type", "text/html; charset=utf-8");
								requestHttpMessages.put("Connection", "Close");
								requestHttpMessages.put("Last-Modified",HTTPHandler.dateFormat().format(fp.lastModified()));
								responseHttp = new ResponseMessage(contentOutput.getBytes(), "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
								if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
									responseMsg=(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
								}
								else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
									responseMsg=(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
								}
								else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
									responseMsg=(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
								}
								else{
									responseMsg=(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
								}
							}
						}
						}else{
							File[] allFiles = fp.listFiles();
							String htmlStart = "<html><body>";
							String htmlEnd = "</body></html>";
							
							StringBuilder filesInfo = new StringBuilder();
							filesInfo.append(htmlStart);
							filesInfo.append(hrefEventPath);
						
							filesInfo.append("<br/>");
							for(File file:allFiles){
								if(!file.getName().endsWith("~")){
									
									filesInfo.append("<a href=\"http://localhost:"+portNumber+hrefEventPath+"/"+file.getName()+"\">"+file.getName()+"</a><br/>");	
									
								}
								
							}
							filesInfo.append(htmlEnd);
							contentOutput = filesInfo.toString();
							requestHttpMessages.put("Date", HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
							requestHttpMessages.put("Content-Length",""+contentOutput.length());
							requestHttpMessages.put("Content-type", "text/html; charset=utf-8");
							requestHttpMessages.put("Connection", "Close");
							requestHttpMessages.put("Last-Modified",HTTPHandler.dateFormat().format(fp.lastModified()));
							responseHttp = new ResponseMessage(contentOutput.getBytes(), "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
							if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
								responseMsg=(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
							}
							else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
								responseMsg=(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
							}
							else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
								responseMsg=(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
							}
							else{
								responseMsg=(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
							}
						}
						
						
					}
					else{
					File[] allFiles = fp.listFiles();
					String htmlStart = "<html><body>";
					String htmlEnd = "</body></html>";
					
					StringBuilder filesInfo = new StringBuilder();
					filesInfo.append(htmlStart);
					filesInfo.append(hrefEventPath);
				
					filesInfo.append("<br/>");
					for(File file:allFiles){
						if(!file.getName().endsWith("~")){
							
							filesInfo.append("<a href=\"http://localhost:"+portNumber+hrefEventPath+"/"+file.getName()+"\">"+file.getName()+"</a><br/>");	
							
						}
						
					}
					filesInfo.append(htmlEnd);
					contentOutput = filesInfo.toString();
					requestHttpMessages.put("Date", HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
					requestHttpMessages.put("Content-Length",""+contentOutput.length());
					requestHttpMessages.put("Content-type", "text/html; charset=utf-8");
					requestHttpMessages.put("Connection", "Close");
					requestHttpMessages.put("Last-Modified",HTTPHandler.dateFormat().format(fp.lastModified()));
					responseHttp = new ResponseMessage(contentOutput.getBytes(), "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
					if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
						responseMsg=(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
					}
					else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
						responseMsg=(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
					}
					else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
						responseMsg=(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
					}
					else{
						responseMsg=(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
					}
					}
				}else{
					responseMsg=(HTTPHandler.get404StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
				}
			}else{
				if(hrefEventPath.equalsIgnoreCase("/control")){
					StringBuilder controlOutput = new StringBuilder();
					
					controlOutput.append("</body></html>");
					contentOutput = controlOutput.toString();
					requestHttpMessages.put("Date", HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
					requestHttpMessages.put("Content-Length",""+contentOutput.length());
					requestHttpMessages.put("Content-type", "text/html; charset=utf-8");
					requestHttpMessages.put("Connection", "Close");
					responseHttp = new ResponseMessage(contentOutput.getBytes(), "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
					if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
						responseMsg=(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
					}
					else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
						responseMsg=(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
					}
					else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
						responseMsg=(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
					}
					else{
						responseMsg=(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
					}
					
				}else if(hrefEventPath.equalsIgnoreCase("/shutdown")){
					//threadPool.setRunningStatus(false);
					HttpServer.isNotShutdown = false;
					
					//spawn a new thread
					requestHttpMessages.put("Date", HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
					requestHttpMessages.put("Content-Length",""+contentOutput.length());
					requestHttpMessages.put("Content-type", "text/html; charset=utf-8");
					requestHttpMessages.put("Connection", "Close");
					responseHttp = new ResponseMessage(contentOutput.getBytes(), "200", HTTPHandler.getHttpResponseMessages().get("200"), requestHttpMessages);
					if(requestHttp.getMethodName().equalsIgnoreCase("HEAD")){
						responseMsg=(responseHttp.giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
					}
					else if(requestHttp.getMethodName().equalsIgnoreCase("GET")){
						responseMsg=(responseHttp.giveHttpResponse(requestHttp.getVersionNumber()));
					}
					else if((requestHttp.getMethodName().equalsIgnoreCase("POST"))||(requestHttp.getMethodName().equalsIgnoreCase("PUT"))||(requestHttp.getMethodName().equalsIgnoreCase("TRACE"))||(requestHttp.getMethodName().equalsIgnoreCase("DELETE"))){
						responseMsg=(HTTPHandler.get405StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
					}
					else{
						responseMsg=(HTTPHandler.get400StatusMessage().giveHttpResponseWithHeaders(requestHttp.getVersionNumber()));
					}
				}
				else{
					responseMsg=(HTTPHandler.get404StatusMessage().giveHttpResponse(requestHttp.getVersionNumber()));
				}
			}
		}	
	
	}
	return responseMsg;
}
	private static String getEventRequiredPath(String path) {
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
	private static Calendar parseEventDateMethod(String dateWhenFileModified) {
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
}


