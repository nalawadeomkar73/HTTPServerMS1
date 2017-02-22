package edu.upenn.cis455.webserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;



public class RequestData {

	private boolean isCorrect;
	private String methodName;
	private String filePath;
	private String protocolName;
	private String versionNumber;
	private Map<String, ArrayList<String>> parserMap;
	private String contentBody;
	private String servletPath;
	private String urlBody;
	private String finalServletPath;
	private String details;

	public RequestData(String parseRequest) {
		isCorrect = true;
		parserMap = new HashMap<String,ArrayList<String>>();
		String[] parseRequestContent = parseRequest.split("\n");
		String[] parserContent = parseRequestContent[0].trim().split(" ");
		if(parserContent.length<3){
			isCorrect = false;
			return;
		}
		else if(parserContent[2].split("/").length<2){
			isCorrect = false;
			return;
		}
		else if(!parserContent[2].split("/")[0].trim().equalsIgnoreCase("HTTP")){
			isCorrect = false;
			return;
		}
		else if((!parserContent[2].split("/")[1].trim().equalsIgnoreCase("1.1") && !parserContent[2].split("/")[1].trim().equalsIgnoreCase("1.0"))){
			isCorrect = false;
			return;
		}
		else{
			methodName = parserContent[0].trim();
			filePath = parserContent[1].trim();
			
			if(filePath.equals(null)||filePath.equals("/favicon.ico")){
				return;
			}
			if(filePath.equals("/")){
				filePath = "";
			}
			protocolName = parserContent[2].split("/")[0].trim();
			versionNumber = parserContent[2].split("/")[1].trim();
		}
		
		for(int i =1;i<parseRequestContent.length;i++){
			if(parseRequestContent[i].split(":").length<2){
				System.out.println("Invalid request: "+parseRequest);
			}else{
				String contentKey = parseRequestContent[i].split(":")[0].trim().toLowerCase();
				String contentValue = parseRequestContent[i].trim().split(":")[1].trim();
				if(parseRequestContent[i].split(":").length>2){
					contentValue = parseRequestContent[i].substring(parseRequestContent[i].indexOf(":")+1).trim();
				}
				if(parserMap.containsKey(contentKey)){
					parserMap.get(contentKey).add(contentValue);
				}else{
					ArrayList<String> contentVal = new ArrayList<String>();
					contentVal.add(contentValue);
					parserMap.put(contentKey, contentVal);
				}
				
				
			}
		}
		
		if(versionNumber.equalsIgnoreCase("1.1")&& !parserMap.containsKey("host")){
			
			isCorrect = false;
		}
	}
	
	@Override
	public String toString() {
		return "HttpRequest [method=" + methodName + ", protocolName=" + protocolName+ ", version=" + versionNumber + ", filePath=" + filePath+ ", parserMap=" + parserMap + ", isCorrect=" + isCorrect+ "]";
	}
	
	public ArrayList<Cookie> getCookies() {
		// TODO Auto-generated method stub
		ArrayList<Cookie> totalCookies = new ArrayList<Cookie>();
		
		if(parserMap.containsKey("cookie")){
			for(String c: parserMap.get("cookie")){
				String[] values = c.split(";");
				for(String v:values){
					totalCookies.add(new Cookie(v.split("=")[0].trim(), v.split("=")[1].trim()));
				}
			}
		}
		return totalCookies;
	}

	public boolean isCorrectMessage() {
		return isCorrect;
	}

	public Map<String,ArrayList<String>> getParserMap() {
		return parserMap;
	}

	public String getFilePath() {
		return filePath;
	}
	
	
	public void setFilePath(String newFilePath) {
		this.filePath = newFilePath;
		
	}

	public String getMethodName() {
		return methodName;
	}

	public String getVersionNumber() {
		return versionNumber;
	}
	
	public  HashMap<String,ArrayList<String>> propertiesMap() {
		// TODO Auto-generated method stub
		HashMap<String,ArrayList<String>> methodProps = new HashMap<String,ArrayList<String>>();
		String finalContentBody = null;
		if(methodName.equals("POST")){
			if(parserMap.containsKey("content-length")){
				if(parserMap.containsKey("content-type")){
					if(parserMap.get("content-type").size()>0){
						if(parserMap.get("content-type").get(0).trim().startsWith("application/x-www-form-urlencoded")){
							finalContentBody = contentBody;
						}
					}
				}
			}
		}else if(methodName.equals("GET")){
			finalContentBody = urlBody;
			
		}
		if(!(finalContentBody==null)){
			for(String splitContent :finalContentBody.split("&")){
				if(splitContent.contains("=")){
					String contentKey = splitContent.split("=")[0].trim();
					String contentValue = splitContent.split("=")[1].trim();
					if(methodProps.containsKey(contentKey))
					{
						methodProps.get(contentKey).add(contentValue);
					}
					else
					{
						ArrayList<String> contentVal = new ArrayList<String>();
						contentVal.add(contentValue);
						methodProps.put(contentKey, contentVal);
					}
				}
			}
			
		}
		return methodProps;
	}
	
	
	public String getContentBody(){
		return contentBody;
	}
	
	public void setContentBody(String contentBody) {
		// TODO Auto-generated method stub
		this.contentBody = contentBody;
	}
	
	public String getServletPath(){
		return servletPath;
	}
	
	public void setServletPath(String servletPath){
		this.servletPath = servletPath;
	}
	
	public String getDetails(){
		return details;
	}

	public void parsePath() {
		// TODO Auto-generated method stub
		if(servletPath!=null){
			if(servletPath.contains(".*")){
				if(servletPath.startsWith(".*")){
					finalServletPath = "";
				}else{
					finalServletPath = servletPath.substring(0,servletPath.indexOf(".*"));
				}
			}else{
				finalServletPath =servletPath;
			}
			String urlPath = filePath.substring(finalServletPath.length());
			if(urlPath.contains("?")){
				details = urlPath.split("\\?")[0];
				urlBody =  urlPath.split("\\?")[1];
			}else{
				details = urlPath;
			}	
		}
	}

	public String getUrlBody() {
		// TODO Auto-generated method stub
		return urlBody;
	}

	public String getfinalPath() {
		// TODO Auto-generated method stub
		return finalServletPath;
	}
 
	
	public String getProtocolName(){
		return protocolName;
	}


	
	

}

