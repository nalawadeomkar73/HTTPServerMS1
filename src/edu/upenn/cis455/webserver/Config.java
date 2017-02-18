package edu.upenn.cis455.webserver;

import javax.servlet.*;
import java.util.*;

/**
 * @author Omkar Nalawade
 */
class Config implements ServletConfig {
	private String name;
	private Context context;
	private HashMap<String,String> initParams;
	
	public Config(String name, Context context) {
		this.name = name;
		this.context = context;
		initParams = new HashMap<String,String>();
	}

	public String getInitParameter(String name) {
		return initParams.get(name);
	}
	
	public Enumeration getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	
	public ServletContext getServletContext() {
		return context;
	}
	
	public String getServletName() {
		return name;
	}

	void setInitParam(String name, String value) {
		initParams.put(name, value);
	}
}
