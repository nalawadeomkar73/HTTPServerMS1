package edu.upenn.cis455.webserver;

public class WorkerThread extends Thread{
	
	
	private int threadId;
	private String rootDirectory;
	private ThreadPool threadPool;

	public WorkerThread(int threadId, String rootDirectory, ThreadPool threadPool) {
		// TODO Auto-generated constructor stub
		this.threadId = threadId;
		this.rootDirectory = rootDirectory;
		this.threadPool = threadPool;
	}

	
}
