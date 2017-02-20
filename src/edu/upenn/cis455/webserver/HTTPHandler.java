package edu.upenn.cis455.webserver;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class HTTPHandler {
	private static SimpleDateFormat datePattern;
	private static Map<String,ArrayList<String>> headerErrorMessages;
	private static Map<String,String> httpResponseMessages;
	private static ResponseMessage get404Status;
	private static ResponseMessage get100Status;
	private static ResponseMessage get200Status;
	private static ResponseMessage get301Status;
	private static ResponseMessage get304Status;
	private static ResponseMessage get403Status;
	private static ResponseMessage get412Status;
	private static ResponseMessage get500Status;
	private static ResponseMessage get400Status;
	private static ResponseMessage get405Status;
	private static SimpleDateFormat datePattern1;
	private static SimpleDateFormat datePattern2;
	/*EEE, dd MMM yyyy HH:mm:ss",
	"EEEE, dd-MMM-yy HH:mm:ss", 
	"EEE MMM dd HH:mm:ss yyyy"};
*/
	static{
		datePattern = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		datePattern1 = new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss z");
		datePattern2 = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
		datePattern.setTimeZone(TimeZone.getTimeZone("GMT"));
		headerErrorMessages = new HashMap<String,ArrayList<String>>();
		ArrayList<String> headerValues;
		headerValues= new ArrayList<String>();
		headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
		headerErrorMessages.put("Date", headerValues);
		headerValues= new ArrayList<String>();
		headerValues.add("Close");
		headerErrorMessages.put("Connection", headerValues);
		httpResponseMessages = new HashMap<String,String>();
		httpResponseMessages.put("100", "CONTINUE");
		httpResponseMessages.put("200","OK");
		httpResponseMessages.put("301", "MOVED PERMANENTLY");
		httpResponseMessages.put("304", "NOT MODIFIED");
		httpResponseMessages.put("400", "BAD REQUEST");
		httpResponseMessages.put("403", "FORBIDDEN");
		
		httpResponseMessages.put("404", "NOT FOUND");
		httpResponseMessages.put("405", "METHOD NOT ALLOWED");
		httpResponseMessages.put("412", "PRECONDITION FAILED");
		httpResponseMessages.put("500", "SERVER ERROR");
		httpResponseMessages.put("POST","COMMAND IS NOT SUPPORTED");
		httpResponseMessages.put("PUT", "COMMAND IS NOT SUPPORTED");
		
		String errorOutput = "<html><body>"+"100"+" : "+httpResponseMessages.get("100")+"</body></html>\r\n";
		get100Status = new ResponseMessage(errorOutput.getBytes(),"100",httpResponseMessages.get("100"),headerErrorMessages);
		
		errorOutput = "<html><body>"+"200"+" : "+httpResponseMessages.get("200")+"</body></html>\r\n";
		headerValues= new ArrayList<String>();
		headerValues.add(""+errorOutput.length());
		headerErrorMessages.put("Content-Length", headerValues);
		get200Status = new ResponseMessage(errorOutput.getBytes(),"200",httpResponseMessages.get("200"),headerErrorMessages);

		errorOutput = "<html><body>"+"400"+" : "+httpResponseMessages.get("400")+"</body></html>\r\n";
		get400Status = new ResponseMessage(errorOutput.getBytes(),"400",httpResponseMessages.get("400"),headerErrorMessages);
		
		errorOutput = "<html><body>"+"404"+" : "+httpResponseMessages.get("404")+"</body></html>\r\n";
		get404Status = new ResponseMessage(errorOutput.getBytes(),"404",httpResponseMessages.get("404"),headerErrorMessages);
		
		errorOutput = "<html><body>"+"301"+" : "+httpResponseMessages.get("301")+"</body></html>\r\n";
		get301Status = new ResponseMessage(errorOutput.getBytes(),"301",httpResponseMessages.get("301"),headerErrorMessages);
		
		errorOutput = "<html><body>"+"304"+" : "+httpResponseMessages.get("304")+"</body></html>\r\n";
		get304Status = new ResponseMessage(errorOutput.getBytes(),"304",httpResponseMessages.get("304"),headerErrorMessages);
		
		errorOutput = "<html><body>"+"403"+" : "+httpResponseMessages.get("403")+"</body></html>\r\n";
		get403Status = new ResponseMessage(errorOutput.getBytes(),"403",httpResponseMessages.get("403"),headerErrorMessages);
		
		errorOutput = "<html><body>"+"412"+" : "+httpResponseMessages.get("412")+"</body></html>\r\n";
		get412Status = new ResponseMessage(errorOutput.getBytes(),"412",httpResponseMessages.get("412"),headerErrorMessages);
		
		errorOutput = "<html><body>"+"500"+" : "+httpResponseMessages.get("500")+"</body></html>\r\n";
		get500Status = new ResponseMessage(errorOutput.getBytes(),"500",httpResponseMessages.get("500"),headerErrorMessages);
		
		errorOutput = "<html><body>"+"405"+" : "+httpResponseMessages.get("405")+"</body></html>\r\n";
		headerValues = new ArrayList<String>();
		headerValues.add("HEAD, GET");
		headerErrorMessages.put("Accept", headerValues);
		get405Status = new ResponseMessage(errorOutput.getBytes(), "405", httpResponseMessages.get("405"), headerErrorMessages);
		
	}
	
	public static SimpleDateFormat dateFormat(){
		datePattern.setTimeZone(TimeZone.getTimeZone("GMT"));
		return datePattern;
	}
	
	public static SimpleDateFormat dateFormat1(){
		datePattern1.setTimeZone(TimeZone.getTimeZone("GMT"));
		return datePattern1;
	}
	
	public static SimpleDateFormat dateFormat2(){
		//datePattern2.setTimeZone(TimeZone.getTimeZone("GMT"));
		return datePattern2;
	}
	
	public static ResponseMessage get100StatusMessage(){
		//headerErrorMessages.put("Date", HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
		return get100Status;
	}
	
	public static ResponseMessage get200StatusMessage(){
		ArrayList<String> headerValues = new ArrayList<String>();
		headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
		headerErrorMessages.put("Date",headerValues);
		return get200Status;
	}
	
	public static ResponseMessage get301StatusMessage(){
		ArrayList<String> headerValues = new ArrayList<String>();
		headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
		headerErrorMessages.put("Date",headerValues);
		return get301Status;
	}
	
	public static ResponseMessage get304StatusMessage(){
		ArrayList<String> headerValues = new ArrayList<String>();
		headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
		headerErrorMessages.put("Date",headerValues);
		return get304Status;
	}
	
	public static ResponseMessage get403StatusMessage(){
		ArrayList<String> headerValues = new ArrayList<String>();
		headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
		headerErrorMessages.put("Date",headerValues);
		return get403Status;
	}
	
	public static ResponseMessage get404StatusMessage(){
		ArrayList<String> headerValues = new ArrayList<String>();
		headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
		headerErrorMessages.put("Date",headerValues);
		return get404Status;
	}
	
	public static ResponseMessage get412StatusMessage(){
		ArrayList<String> headerValues = new ArrayList<String>();
		headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
		headerErrorMessages.put("Date",headerValues);
		return get412Status;
	}
	
	public static ResponseMessage get500StatusMessage(){
		ArrayList<String> headerValues = new ArrayList<String>();
		headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
		headerErrorMessages.put("Date",headerValues);
		return get500Status;
	}
	
	public static ResponseMessage get400StatusMessage(){
		ArrayList<String> headerValues = new ArrayList<String>();
		headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
		headerErrorMessages.put("Date",headerValues);
		return get400Status;
	}

	public static Map<String,String> getHttpResponseMessages() {
		// TODO Auto-generated method stub
		return httpResponseMessages;
	}
	
	public static ResponseMessage get405StatusMessage(){
		ArrayList<String> headerValues = new ArrayList<String>();
		headerValues.add(HTTPHandler.dateFormat().format(new GregorianCalendar().getTime()));
		headerErrorMessages.put("Date",headerValues);
		return get405Status;
	}
	

}


/*
HTTP applications have historically allowed three different formats
for the representation of date/time stamps:

   Sun, 06 Nov 1994 08:49:37 GMT  ; RFC 822, updated by RFC 1123
   Sunday, 06-Nov-94 08:49:37 GMT ; RFC 850, obsoleted by RFC 1036
   Sun Nov  6 08:49:37 1994       ; ANSI C's asctime() format
*/
