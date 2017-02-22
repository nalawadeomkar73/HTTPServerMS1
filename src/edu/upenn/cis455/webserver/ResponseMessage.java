package edu.upenn.cis455.webserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ResponseMessage{
	private byte[] errorOutput;
	private String errorCode;
	private String errorStatus;
	private Map<String,ArrayList<String>> headerErrorMessages;
	private ByteArrayOutputStream byteOutput=null;
	private String commProtocol = "HTTP";
	StringBuilder responseMsg=null;
	private String versionNumber;
	private StringWriter setWrite;
	//private static final String version_1 = "1.0";
	//private static final String version_2 = "1.1";
	
	
	public ResponseMessage(byte[] errorOutput, String errorCode, String errorStatus, Map<String, ArrayList<String>> headerErrorMessages) {
		
		this.errorOutput = errorOutput;
		this.errorCode = errorCode;
		this.errorStatus = errorStatus;
		this.headerErrorMessages = headerErrorMessages;
		this.byteOutput=new ByteArrayOutputStream();
		this.responseMsg = new StringBuilder();
	}
	
	public ResponseMessage(String commProtocol, String errorCode,
			String errorStatus) {
		this.commProtocol = commProtocol;
		this.errorCode = errorCode;
		this.errorStatus = errorStatus;
		this.headerErrorMessages = new HashMap<String,ArrayList<String>>();
		this.byteOutput=new ByteArrayOutputStream();
		this.responseMsg = new StringBuilder();
	}

	public ResponseMessage() {
		
		this.errorCode = "";
		this.errorStatus = "";
		this.headerErrorMessages = new HashMap<String,ArrayList<String>>();
		this.byteOutput=new ByteArrayOutputStream();
		this.responseMsg = new StringBuilder();
	}

	public byte[] giveHttpResponse(String version){
		
		try
		{
			responseMsg.delete(0, responseMsg.length());
			byteOutput.reset();
		    responseMsg.append(commProtocol+"/"+version+" "+errorCode+" "+errorStatus+"\r\n");
		
		if(headerErrorMessages!=null){
			for(Map.Entry<String, ArrayList<String>> entry : headerErrorMessages.entrySet()){
				StringBuilder respMsg = new StringBuilder();
				for(int i=0;i<entry.getValue().size();i++){
					
					respMsg.append(entry.getValue().get(i));
					if(i<entry.getValue().size()-1){
						respMsg.append(", ");
					}
				}
				
				responseMsg.append(entry.getKey()+":"+entry.getValue().get(0)+"\r\n");
				
			}
		}
		responseMsg.append("\r\n");
		
		byteOutput.write(responseMsg.toString().getBytes());
		if(errorOutput!=null){
			byteOutput.write(errorOutput);
		}	
		} 
	
		catch (IOException e) {	
		}
		
		return byteOutput.toByteArray();
	}

	public byte[] giveHttpResponseWithHeaders(String version) {
		// TODO Auto-generated method stub
		try
		{
			responseMsg.delete(0, responseMsg.length());
			byteOutput.reset();
		responseMsg.append(commProtocol+"/"+version+" "+errorCode+" "+errorStatus+"\r\n");
		if(headerErrorMessages!=null){
			for(Map.Entry<String, ArrayList<String>> entry : headerErrorMessages.entrySet()){
				StringBuilder respMsg = new StringBuilder();
				for(int i=0;i<entry.getValue().size();i++){
					
					respMsg.append(entry.getValue().get(i));
					if(i<entry.getValue().size()-1){
						respMsg.append(", ");
					}
				}
				responseMsg.append(entry.getKey()+":"+entry.getValue().get(0)+"\r\n");
			}
		}
		responseMsg.append("\r\n");
	
		byteOutput.write(responseMsg.toString().getBytes());
		}catch (IOException e) {
			}
		
		return byteOutput.toByteArray();
	}
	
		public byte[] giveHttpResponseForServlets(String version){
		
		try
		{
			responseMsg.delete(0, responseMsg.length());
			byteOutput.reset();
		    responseMsg.append(commProtocol+"/"+version+" "+errorCode+" "+errorStatus+"\r\n");
		
		if(headerErrorMessages!=null){
			for(Map.Entry<String, ArrayList<String>> entry : headerErrorMessages.entrySet()){
				StringBuilder respMsg = new StringBuilder();
				for(int i=0;i<entry.getValue().size();i++){
					
					respMsg.append(entry.getValue().get(i));
					if(i<entry.getValue().size()-1){
						respMsg.append(", ");
					}
				}
				responseMsg.append(entry.getKey()+":"+entry.getValue().get(0)+"\r\n");
			}
		}
		responseMsg.append("\r\n");
		errorOutput= (setWrite.getBuffer().toString().getBytes());
		byteOutput.write(responseMsg.toString().getBytes());
		if(errorOutput!=null){
			byteOutput.write(errorOutput);
		}	
		} 
	
		catch (IOException e) {	
		}
		return byteOutput.toByteArray();
	}
	
	
	
	
	
	
	public void clearAll() {
		// TODO Auto-generated method stub
		this.errorCode ="";
		this.errorStatus = "";
		StringBuilder sb = new StringBuilder("");
		this.responseMsg = sb;
		this.headerErrorMessages.clear();
		
		
	}
	
	public void setVersion(String versionNumber) {
		this.versionNumber = versionNumber;
		
	}
	
	public void setWriter(StringWriter s){
		this.setWrite = s;
	}
	
	public StringWriter getWriter(){
		return setWrite;
	}
	
	public String getVersion(){
		return versionNumber;
	}

	public Map<String,ArrayList<String>> getHeaderMap() {
		// TODO Auto-generated method stub
		return headerErrorMessages;
	}
	
	public void setErrorStatus(String response){
		this.errorStatus = response;
	}

	public void setErrorCode(String errorCode) {
		// TODO Auto-generated method stub
		this.errorCode = errorCode;
		
	}

	
	
	
	
}
