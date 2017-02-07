package edu.upenn.cis455.webserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class ResponseMessage{
	private byte[] errorOutput;
	private String errorCode;
	private String errorStatus;
	private Map<String,String> headerErrorMessages;
	private ByteArrayOutputStream byteOutput=null;
	private static final String commProtocol = "HTTP";
	//private static final String version_1 = "1.0";
	//private static final String version_2 = "1.1";
	
	public ResponseMessage(byte[] errorOutput, String errorCode, String errorStatus, Map<String, String> headerErrorMessages) {
		// TODO Auto-generated constructor stub
		this.errorOutput = errorOutput;
		this.errorCode = errorCode;
		this.errorStatus = errorStatus;
		this.headerErrorMessages = headerErrorMessages;
		this.byteOutput=new ByteArrayOutputStream();
	}
	
	public byte[] giveHttpResponse(String version){
		
		try
		{
			StringBuilder responseMsg = new StringBuilder();
		responseMsg.append(commProtocol+"/"+version+" "+errorCode+" "+errorStatus+"\r\n");
		if(headerErrorMessages!=null){
			for(Map.Entry<String, String> entry : headerErrorMessages.entrySet()){
				responseMsg.append(entry.getKey()+":"+entry.getValue()+"\r\n");
			}
		}
		responseMsg.append("\r\n");
		
			byteOutput.write(responseMsg.toString().getBytes());
			if(errorOutput!=null){
				byteOutput.write(errorOutput);
			}
			
			
		} 
		
		//responseMsg.append("\r\n");
		
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return byteOutput.toByteArray();
	}

	public byte[] giveHttpResponseWithHeaders(String version) {
		// TODO Auto-generated method stub
		try
		{
			StringBuilder responseMsg = new StringBuilder();
		responseMsg.append(commProtocol+"/"+version+" "+errorCode+" "+errorStatus+"\r\n");
		if(headerErrorMessages!=null){
			for(Map.Entry<String, String> entry : headerErrorMessages.entrySet()){
				responseMsg.append(entry.getKey()+":"+entry.getValue()+"\r\n");
			}
		}
		responseMsg.append("\r\n");
		
			byteOutput.write(responseMsg.toString().getBytes());
		}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return byteOutput.toByteArray();
	}
	
	
	
	
}
