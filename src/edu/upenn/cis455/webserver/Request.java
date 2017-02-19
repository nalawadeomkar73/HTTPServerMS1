package edu.upenn.cis455.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


class Request implements HttpServletRequest {

	
	
	//private Socket sock;
	
	Request() {
	}
	
	Request(Session session) {
		m_session = session;
	}
	
	Request(RequestData req){
		this.req =req;
	}
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getAuthType()
	 */
	public String getAuthType() {
		// TODO Auto-generated method stub
		return BASIC_AUTH;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getCookies()
	 */
	public Cookie[] getCookies() {
		// TODO Auto-generated method stub
		Cookie[] allCookies = cookiesFromHeaders.toArray(new Cookie[cookiesFromHeaders.size()]);
		return allCookies;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
	 */
	public long getDateHeader(String dateHeader) {
		// TODO Auto-generated method stub
		if(req.getParserMap().containsKey(dateHeader)){
			String givenDate = req.getParserMap().get(dateHeader).get(0);
		
			Calendar cd = new GregorianCalendar();
			Date d1 = null;
			String formats[] = {"EEE, dd MMM yyyy HH:mm:ss z","EEEE, dd-MMM-yy HH:mm:ss z","EEE MMM dd HH:mm:ss yyyy"};
			SimpleDateFormat sd = null;
			try{
				sd = new SimpleDateFormat(formats[0]);
				sd.setTimeZone(TimeZone.getTimeZone("GMT"));
				d1 = sd.parse(givenDate);
			
			}
			catch(Exception e){
			
			}
			if(d1!=null){
				cd.setTime(d1);
				return cd.getTimeInMillis();
			}
		
			try{
				sd = new SimpleDateFormat(formats[1]);
				sd.setTimeZone(TimeZone.getTimeZone("GMT"));
				d1 = sd.parse(givenDate);
			
			}
			catch(Exception e){
			
			}
			if(d1!=null){
				cd.setTime(d1);
				return cd.getTimeInMillis();
			}
			try{
				sd = new SimpleDateFormat(formats[2]);
				sd.setTimeZone(TimeZone.getTimeZone("GMT"));
				d1 = sd.parse(givenDate);
			
			}
			catch(Exception e){
			
			}
			if(d1!=null){
				cd.setTime(d1);
				return cd.getTimeInMillis();
			}else{
				throw new IllegalArgumentException("Date does not match");
			}
			
		}else{
			return -1;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 */
	public String getHeader(String arg0) {
		// TODO Auto-generated method stub
		if(req.getParserMap().containsKey(arg0)){
			return req.getParserMap().get(arg0).get(0);
		}else{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
	 */
	public Enumeration getHeaders(String arg0) {
		// TODO Auto-generated method stub
		if(req.getParserMap().containsKey(arg0)){
			return Collections.enumeration(req.getParserMap().get(arg0));
		}else{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
	 */
	public Enumeration getHeaderNames() {
		// TODO Auto-generated method stub
		if(req.getParserMap()==null){
			return null;
		}else{
			if(req.getParserMap().isEmpty()){
				return Collections.emptyEnumeration();
			}else{
				return Collections.enumeration(req.getParserMap().keySet());
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
	 */
	public int getIntHeader(String arg0) {
		// TODO Auto-generated method stub
		if(req.getParserMap().containsKey(arg0)){
			return Integer.parseInt(req.getParserMap().get(arg0).get(0));
		}else{
			return -1;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getMethod()
	 */
	public String getMethod() {
		return req.getMethodName();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathInfo()
	 */
	public String getPathInfo() {
		// TODO Auto-generated method stub
		if(req.getDetails().isEmpty())
			return null;
		else
			return req.getDetails();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
	 */
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getContextPath()
	 */
	public String getContextPath() {
		// TODO Auto-generated method stub
		return "";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getQueryString()
	 */
	public String getQueryString() {
		// TODO Auto-generated method stub
		return req.getUrlBody();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
	 */
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	public boolean isUserInRole(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
	 */
	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		for(Cookie c : cookiesFromHeaders){
			if(c.getName().equalsIgnoreCase("JSESSIONID"))
			{
				return c.getValue();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	public String getRequestURI() {
		// TODO Auto-generated method stub
		if(req.getFilePath().contains("?")){
			return req.getFilePath().split("\\?")[0];
		}else{
			return req.getFilePath();
		}
		//return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURL()ET
	 */
	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		StringBuffer requestURL = new StringBuffer();
		requestURL.append("http://localhost:"+HttpServer.getPortNumber()+getRequestURI());
		return requestURL;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getServletPath()
	 */
	public String getServletPath() {
		// TODO Auto-generated method stub
		return req.getfinalPath();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	public HttpSession getSession(boolean arg0) {
		if (arg0) {
			if (! hasSession()) {
				m_session = new Session();
				synchronized(HttpServer.getSessionMap())
				{
					HttpServer.getSessionMap().put(m_session.getId(), m_session);
				}
			}
		} else {
			if (! hasSession()) {
				m_session = null;
			}
		}
		return m_session;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession()
	 */
	public HttpSession getSession() {
		return getSession(true);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
	 */
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		String sId = getRequestedSessionId();
		synchronized(HttpServer.getSessionMap())
		{
			if(sId != null){
				if(HttpServer.getSessionMap().containsKey(sId)){
					if(HttpServer.getSessionMap().get(sId).isValid()){
						return true;
					}else{
						return false;
					}
				}else return false;
				
			}else return false;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
	 */
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		String sId = getRequestedSessionId();
		if(sId!=null){
			return true;
		}else{
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String arg0) {
		// TODO Auto-generated method stub
		return m_props.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttributeNames()
	 */
	public Enumeration getAttributeNames() {
		// TODO Auto-generated method stub
		return m_props.keys();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		return encoding;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String charEncoding)
			throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		this.encoding = charEncoding;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentLength()
	 */
	public int getContentLength() {
		// TODO Auto-generated method stub
		if(req.getParserMap().containsKey("content-length")){
			return Integer.parseInt(req.getParserMap().get("content-length").get(0));
		}else{
			return -1;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentType()
	 */
	public String getContentType() {
		// TODO Auto-generated method stub
		if(req.getParserMap().containsKey("content-type")){
			return req.getParserMap().get("content-key").get(0);
		}else{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getInputStream()
	 */
	public ServletInputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 */
	public String getParameter(String arg0) {
		if(propertiesMap.containsKey(arg0)){
			return propertiesMap.get(arg0).get(0);
		}else{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 */
	public Enumeration getParameterNames() {
		return Collections.enumeration(propertiesMap.keySet());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String arg0) {
		// TODO Auto-generated method stub
		if(propertiesMap.containsKey(arg0)){
			String[] allParameters;
			allParameters =  propertiesMap.get(arg0).toArray(new String[propertiesMap.get(arg0).size()]);
			return allParameters;
		}else{
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	public Map getParameterMap() {
		// TODO Auto-generated method stub
		return propertiesMap;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getProtocol()
	 */
	public String getProtocol() {
		// TODO Auto-generated method stub
		return req.getProtocolName()+"/"+req.getVersionNumber();
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getScheme()
	 */
	public String getScheme() {
		// TODO Auto-generated method stub
		return "http";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerName()
	 */
	public String getServerName() {
		// TODO Auto-generated method stub
		if(req.getParserMap().containsKey("host")){
			String hostValue = req.getParserMap().get("host").get(0);
			if(hostValue.contains(":")){
				return hostValue.split(":")[0];
			}else{
				return hostValue;
			}
		}else{
			return HttpServer.server.getInetAddress().getHostAddress();
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerPort()
	 */
	public int getServerPort() {
		// TODO Auto-generated method stub
		if(req.getParserMap().containsKey("host")){
			String hostValue = req.getParserMap().get("host").get(0);
			if(hostValue.contains(":")){
				return Integer.parseInt(hostValue.split(":")[1]);
			}else{
				return HttpServer.server.getLocalPort();
			}
		}else{
			return HttpServer.server.getLocalPort();
		}
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getReader()
	 */
	public BufferedReader getReader() throws IOException {
		// TODO Auto-generated method stub
		BufferedReader reader = new BufferedReader(new StringReader(req.getContentBody()));
		return reader;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteAddr()
	 */
	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		return sock.getRemoteSocketAddress().toString();
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteHost()
	 */
	public String getRemoteHost() {
		// TODO Auto-generated method stub
		return sock.getInetAddress().getHostName();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String arg0, Object arg1) {
		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String arg0) {
		// TODO Auto-generated method stub
		m_props.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocale()
	 */
	public Locale getLocale() {
		// TODO Auto-generated method stub
		return locale;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocales()
	 */
	public Enumeration getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#isSecure()
	 */
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 */
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemotePort()
	 */
	public int getRemotePort() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalName()
	 */
	public String getLocalName() {
		// TODO Auto-generated method stub
		return HttpServer.server.getInetAddress().getHostName();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalAddr()
	 */
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return HttpServer.server.getInetAddress().getHostAddress();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalPort()
	 */
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return HttpServer.server.getLocalPort();
	}

	void setMethod(String method) {
		m_method = method;
	}
	
	void setParameter(String key, String value) {
		if(propertiesMap.containsKey(key))
		{
			propertiesMap.get(key).add(value);
		}
		else
		{
			ArrayList<String> valueList = new ArrayList<String>();
			valueList.add(value);
			propertiesMap.put(key, valueList);
		}
	}
	
	void clearParameters() {
		m_params.clear();
	}
	
	boolean hasSession() {
		return ((m_session != null) && m_session.isValid());
	}
		
    public void setLocale(Locale locale){
    	this.locale = locale;
    }
	private Properties m_params = new Properties();
	private Properties m_props = new Properties();
	private Session m_session = null;
	private String m_method;
	private RequestData req;
	private HashMap<String, ArrayList<String>> propertiesMap;
	private ArrayList<Cookie> cookiesFromHeaders;
	private Socket sock;
	private String encoding = "ISO-8859-1";
	private Locale locale;
	public void setparameters(HashMap<String, ArrayList<String>> propertiesMap) {
		// TODO Auto-generated method stub
		this.propertiesMap = propertiesMap;
	}

	public void setCookies(ArrayList<Cookie> cookiesFromHeaders) {
		// TODO Auto-generated method stub
		this.cookiesFromHeaders = cookiesFromHeaders;
	}

	public void saveSession(Session session) {
		// TODO Auto-generated method stub
		this.m_session = session;
	}

	public void setSocket(Socket mySock) {
		// TODO Auto-generated method stub
		this.sock = mySock;
	}
}
