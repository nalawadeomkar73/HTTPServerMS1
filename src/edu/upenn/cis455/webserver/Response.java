package edu.upenn.cis455.webserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author tjgreen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Response implements HttpServletResponse {

	private Socket sock;
	private ResponseMessage responseHttp;
	private Request req;
	private int bSize;
	private StringWriter s;
	private int errorStatusCode;
	private String charEncoding;
	private boolean isCommit = false;
	private Locale locale;
	private ArrayList<Cookie> cookieList = new ArrayList<Cookie>();

	public Response(ResponseMessage responseHttp, Request req) {
		// TODO Auto-generated constructor stub
		this.responseHttp = responseHttp;
		this.req = req;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
	 */
	public void addCookie(Cookie c) {
		// TODO Auto-generated method stub
		StringBuilder cp = new StringBuilder();
		cp.append(c.getName()+"=");
		cp.append(c.getValue()+"; ");
		cp.append("Max-Age"+c.getMaxAge());
		cookieList.add(c);
		if(responseHttp.getHeaderMap().containsKey("set-cookie")){
			responseHttp.getHeaderMap().get("set-cookie").add(cp.toString());
		}else{
			ArrayList<String> headerValues = new ArrayList<String>();
			headerValues.add(cp.toString());
			responseHttp.getHeaderMap().put("set-cookie", headerValues);
		}
		
		
		
		//if(responseHttp)
		

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
	 */
	public boolean containsHeader(String header) {
		// TODO Auto-generated method stub
		return responseHttp.getHeaderMap().containsKey(header);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	public String encodeURL(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
	 */
	public String encodeRedirectURL(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 */
	public String encodeUrl(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
	 */
	public String encodeRedirectUrl(String arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
	 */
	public void sendError(int statusCode, String statusMessage) throws IOException {
		// TODO Auto-generated method stub
		if(!isCommitted()){
			String contentOutput = "<html><body>"+statusCode+statusMessage+"</body></html>";
			setBufferSize(contentOutput.length());
			s = new StringWriter(bSize);
			setStatus(statusCode);
			responseHttp.setErrorStatus(statusMessage);
			setContentLength(contentOutput.length());
			setContentType("text/html");
			
			//Pending to write to print writer
			flushBuffer();
			
		}else{
			throw new IllegalStateException("In send Error with status code and status message");
		}
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 */
	public void sendError(int statusCode) throws IOException {
		// TODO Auto-generated method stub
		if(!isCommitted()){
			String contentOutput = "<html><body>"+statusCode+"</body></html>";
			setBufferSize(contentOutput.length());
			s = new StringWriter(bSize);
			setStatus(statusCode);
			//responseHttp.setErrorStatus(statusMessage);
			setContentLength(contentOutput.length());
			setContentType("text/html");
			
			//Pending to write to print writer
			flushBuffer();
			
		}else{
			throw new IllegalStateException("In send Error with status code");
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	public void sendRedirect(String arg0) throws IOException {
		System.out.println("[DEBUG] redirect to " + arg0 + " requested");
		System.out.println("[DEBUG] stack trace: ");
		Exception e = new Exception();
		StackTraceElement[] frames = e.getStackTrace();
		for (int i = 0; i < frames.length; i++) {
			System.out.print("[DEBUG]   ");
			System.out.println(frames[i].toString());
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
	 */
	public void setDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub
		ArrayList<String> headerValues = new ArrayList<String>();
		headerValues.add(HTTPHandler.dateFormat().format(new Date(arg1)));
		responseHttp.getHeaderMap().put(arg0, headerValues);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
	 */
	public void addDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub
		if(responseHttp.getHeaderMap().containsKey(arg0)){
			responseHttp.getHeaderMap().get(arg0).add(HTTPHandler.dateFormat().format(new Date(arg1)));
		}else{
			ArrayList<String> headerValues = new ArrayList<String>();
			headerValues.add(HTTPHandler.dateFormat().format(new Date(arg1)));
			responseHttp.getHeaderMap().put(arg0, headerValues);
		}

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
	 */
	public void setHeader(String arg0, String arg1) {
		// TODO Auto-generated method stub
		ArrayList<String> headerValues = new ArrayList<String>();
		headerValues.add(arg1);
		responseHttp.getHeaderMap().put(arg0, headerValues);

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
	 */
	public void addHeader(String arg0, String arg1) {
		if(responseHttp.getHeaderMap().containsKey(arg0)){
			responseHttp.getHeaderMap().get(arg0).add(arg1);
		}else{
			ArrayList<String> headerValues = new ArrayList<String>();
			headerValues.add(arg1);
			responseHttp.getHeaderMap().put(arg0, headerValues);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
	 */
	public void setIntHeader(String arg0, int arg1) {
		ArrayList<String> headerValues = new ArrayList<String>();
		headerValues.add(String.valueOf(arg1));
		responseHttp.getHeaderMap().put(arg0, headerValues);

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
	 */
	public void addIntHeader(String arg0, int arg1) {
		if(responseHttp.getHeaderMap().containsKey(arg0)){
			responseHttp.getHeaderMap().get(arg0).add(String.valueOf(arg1));
		}else{
			ArrayList<String> headerValues = new ArrayList<String>();
			headerValues.add(String.valueOf(arg1));
			responseHttp.getHeaderMap().put(arg0, headerValues);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 */
	public void setStatus(int statusCode) {
		// TODO Auto-generated method stub
		errorStatusCode = statusCode;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
	 */
	public void setStatus(int arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		if(charEncoding!=null){
			return charEncoding;
		}else{
			return "ISO-8859-1";
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getContentType()
	 */
	public String getContentType() {
		// TODO Auto-generated method stub
		if(responseHttp.getHeaderMap().containsKey("content-type")){
			return responseHttp.getHeaderMap().get("content-type").get(0);
		}else{
			return null;
		}
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getOutputStream()
	 */
	public ServletOutputStream getOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getWriter()
	 */
	public PrintWriter getWriter() throws IOException {
		return new PrintWriter(System.out, true);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String encoding) {
		// TODO Auto-generated method stub
		charEncoding = encoding;

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 */
	public void setContentLength(int len) {
		// TODO Auto-generated method stub
		ArrayList<String> headerValues = new ArrayList<String>();
		headerValues.add(String.valueOf(len));
		responseHttp.getHeaderMap().put("content-length", headerValues);

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 */
	public void setContentType(String contentValue) {
		// TODO Auto-generated method stub
		ArrayList<String> headerValues = new ArrayList<String>();
		headerValues.add(contentValue);
		responseHttp.getHeaderMap().put("content-type", headerValues);
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	public void setBufferSize(int bufSize) {
		// TODO Auto-generated method stub
		bSize = bufSize;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	public int getBufferSize() {
		// TODO Auto-generated method stub
		return bSize;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#resetBuffer()
	 */
	public void resetBuffer() {
		// TODO Auto-generated method stub
		if(!isCommitted()){
			s.getBuffer().setLength(0);
		}else{
			throw new IllegalStateException("Reset the buffer");
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#isCommitted()
	 */
	public boolean isCommitted() {
		// TODO Auto-generated method stub
		return isCommit ;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#reset()
	 */
	public void reset() {
		if(!isCommitted()){
			responseHttp.clearAll();
		}else{
			throw new IllegalStateException("Reset the buffer");
		}

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale lcl) {
		// TODO Auto-generated method stub
		if(!isCommitted() && charEncoding==null){
			locale = lcl;
		}
		

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getLocale()
	 */
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return locale;
	}

	public void setSocket(Socket mySock) {
		// TODO Auto-generated method stub
		this.sock = mySock;
	}

}
