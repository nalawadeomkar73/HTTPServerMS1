package edu.upenn.cis455.webserver;

import java.util.ArrayList;
import java.util.List;

public class ThreadPool {
	
	private boolean isRunning;
	private int portNumber;
	private String rootDirectory;
	private int noOfThreads;
	private ArrayList<WorkerThread> threadPool;
	private ArrayList<WorkerThread> listOfThreads;

	public ThreadPool(int portNumber, String rootDirectory, int noOfThreads){
		super();
		this.portNumber = portNumber;
		this.rootDirectory = rootDirectory;
		this.noOfThreads = noOfThreads;
		this.threadPool = new ArrayList<WorkerThread>();
		this.listOfThreads = new ArrayList<WorkerThread>();
		this.isRunning = true;
	}
	
	public void executeThreadPool() {
		
		// TODO Auto-generated method stub
		for(int i=0;i<noOfThreads;i++){
			threadPool.add(new WorkerThread(i,rootDirectory,this));
			listOfThreads.add(threadPool.get(i));
		}
		for(WorkerThread thread: threadPool){
			thread.start();
		}
		
	}
	
	public int getPortNumber(){
		return portNumber;
	}
	
	public String getDirectory(){
		return rootDirectory;	
	}
	
	public synchronized int getNumberOfThreads(){
		return noOfThreads;
	}
	
	public synchronized void setNumberOfThreads(int noOfThreads){
		this.noOfThreads = noOfThreads;
	}
	
	public synchronized boolean checkThreadPoolRunning(){
		return isRunning;
	}
	
	public synchronized void setThreadPoolRunning(boolean isRunning){
		this.isRunning = isRunning;
	}
	
	public synchronized ArrayList<WorkerThread> getThreadPool(){
		return threadPool;
	}
	
	public synchronized void setThreadPool(ArrayList<WorkerThread> threadPool){
		this.threadPool = threadPool;
	}
	
	public synchronized ArrayList<WorkerThread> getListOfThreads(){
		return listOfThreads;
	}
}
