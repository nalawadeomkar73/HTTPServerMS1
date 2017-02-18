package edu.upenn.cis455.webserver;

import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * @author Omkar Nalawade
 */
class Session implements HttpSession {
	
	private Date creationTime;
	private Date lastAccessedTime;
	private String sessionID;
	private int maxInactiveInterval = -1;
	
	
	
	public Session(){
		creationTime = new Date();
		sessionID = UUID.randomUUID().toString();
		lastAccessedTime = new Date();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getCreationTime()
	 */
	public long getCreationTime() {
		// TODO Auto-generated method stub
		if(!isValid()){
			throw new IllegalStateException("METHOD CALLED ON INVALIDATED SESSION");
		}
		return creationTime.getTime();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getId()
	 */
	public String getId() {
		// TODO Auto-generated method stub
		if(!isValid()){
			throw new IllegalStateException("METHOD CALLED ON INVALIDATED SESSION");
		}
		return sessionID;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getLastAccessedTime()
	 */
	public long getLastAccessedTime() {
		// TODO Auto-generated method stub
		if(!isValid()){
			throw new IllegalStateException("METHOD CALLED ON INVALIDATED SESSION");
		}
		return lastAccessedTime.getTime();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getServletContext()
	 */
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#setMaxInactiveInterval(int)
	 */
	public void setMaxInactiveInterval(int interval) {
		// TODO Auto-generated method stub
		if(interval<0){
			maxInactiveInterval = (int) Double.POSITIVE_INFINITY;
		}
		else{
			maxInactiveInterval  = interval;
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getMaxInactiveInterval()
	 */
	public int getMaxInactiveInterval() {
		// TODO Auto-generated method stub
		return maxInactiveInterval;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getSessionContext()
	 */
	public HttpSessionContext getSessionContext() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String arg0) {
		// TODO Auto-generated method stub
		if(isValid()){
			if(m_props.containsKey(arg0)){
				return m_props.get(arg0);
			}else{
				return null;
			}
		}else{
			throw new IllegalStateException("METHOD CALLED ON INVALIDATED SESSION");
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getValue(java.lang.String)
	 */
	public Object getValue(String arg0) {
		// TODO Auto-generated method stub
		return m_props.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getAttributeNames()
	 */
	public Enumeration getAttributeNames() {
		// TODO Auto-generated method stub
		if(isValid()){
			return m_props.keys();
		}else{
			throw new IllegalStateException("METHOD CALLED ON INVALIDATED SESSION");
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#getValueNames()
	 */
	public String[] getValueNames() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String arg0, Object arg1) {
		if(isValid()){
			if(arg1 == null){
				removeAttribute(arg0);
			}else{
				m_props.put(arg0, arg1);
			}
		}else{
			throw new IllegalStateException("METHOD CALLED ON INVALIDATED SESSION");
		}
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#putValue(java.lang.String, java.lang.Object)
	 */
	public void putValue(String arg0, Object arg1) {
		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String arg0) {
		if(isValid()){
			if(arg0!=null && m_props.containsKey(arg0)){
				m_props.remove(arg0);
			}
		}else{
			throw new IllegalStateException("METHOD CALLED ON INVALIDATED SESSION");
		}
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#removeValue(java.lang.String)
	 */
	public void removeValue(String arg0) {
		m_props.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#invalidate()
	 */
	public void invalidate() {
		m_valid = false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSession#isNew()
	 */
	public boolean isNew() {
		// TODO Auto-generated method stub
	
		if(getLastAccessedTime()>getCreationTime())
			return false;
		else
			return true;
		
	}

	boolean isValid() {
		return m_valid;
	}
	
	private Properties m_props = new Properties();
	private boolean m_valid = true;
}
