package edu.upenn.cis455.webserver;

import java.net.Socket;

public class WorkerThread extends Thread{
	
	private RequestData requestHttp;
	private ResponseMessage responseHttp;
	private int threadId;
	private String rootDirectory;
	private ThreadPool threadPool;
	private BlockingQueue bq;
	private Socket mySock;
	

	public WorkerThread(int threadId, String rootDirectory, ThreadPool threadPool, BlockingQueue bq) {
		// TODO Auto-generated constructor stub
		this.threadId = threadId;
		this.rootDirectory = rootDirectory;
		this.threadPool = threadPool;
		this.bq = bq;
		this.mySock = new Socket();
	}

	
}
