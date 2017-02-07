package edu.upenn.cis455.webserver;

import java.util.HashMap;
import java.util.Map;

public class RequestData {

	private boolean isCorrect;
	private String methodName;
	private String filePath;
	private String protocolName;
	private String versionNumber;
	private Map<String, String> parserMap;

	public RequestData(String parseRequest) {
		isCorrect = true;
		parserMap = new HashMap<String,String>();
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
				parserMap.put(contentKey,contentValue);
				for(String key:parserMap.keySet())
				{
					System.out.println(key);
				}
			}
		}
		
		if(versionNumber.equalsIgnoreCase("1.1")&& !parserMap.containsKey("host")){
			System.out.println("The HTTP request with version number "+versionNumber+" but is without Host defined for "+parseRequest);
			isCorrect = false;
		}
	}
	
	@Override
	public String toString() {
		return "HttpRequest [method=" + methodName + ", protocolName=" + protocolName+ ", version=" + versionNumber + ", filePath=" + filePath+ ", parserMap=" + parserMap + ", isCorrect=" + isCorrect+ "]";
	}
	
	

	public boolean isCorrectMessage() {
		return isCorrect;
	}

	public Map<String,String> getParserMap() {
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
	

}

