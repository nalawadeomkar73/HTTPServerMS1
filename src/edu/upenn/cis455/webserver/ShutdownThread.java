package edu.upenn.cis455.webserver;

import java.io.IOException;

public class ShutdownThread extends Thread{
	
	private int maxNoOfThreads;

	public ShutdownThread(ThreadPool threadPool) {
		
		this.maxNoOfThreads = threadPool.getNumberOfThreads();
	}

	public void run(){
		
		boolean cond=true;
		while(cond){
			
		if(ThreadPool.count==50)
			break;
		else
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				
			}
		}
		
		
		try {
			
			
			
			throw new Exception("done");
		
			
		
			
		} catch (Exception e) {
			System.out.println("FInally DOne");
			try {
				HttpServer.server.close();
				HttpServer.timer.cancel();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

}
