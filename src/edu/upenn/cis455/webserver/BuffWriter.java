package edu.upenn.cis455.webserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class BuffWriter extends PrintWriter{

	private Response response;

	public BuffWriter(Writer s, boolean b, Response response) {
		// TODO Auto-generated constructor stub
		super(s,b);
		this.response = response;
	}
	
	public void flush(){
		try {
			response.flushBuffer();
		} catch (IOException e) {
			
		}
	}
	

}
