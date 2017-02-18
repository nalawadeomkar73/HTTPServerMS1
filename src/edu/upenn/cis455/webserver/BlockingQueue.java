package edu.upenn.cis455.webserver;


import java.net.Socket;
import java.util.ArrayList;

public class BlockingQueue {
	
	private ArrayList<Socket> blockingQueue;
	
	public BlockingQueue(){
		blockingQueue = new ArrayList<Socket>();
	}
	
	public synchronized void enqueue(Socket socket)throws InterruptedException{
		this.blockingQueue.add(socket);
		this.notifyAll();
	}
	
	public synchronized Socket dequeue()throws InterruptedException{
		while(this.blockingQueue.size()==0){
			wait();
		}
		return this.blockingQueue.remove(0);
	}
	
	public int checkSize() {
		return blockingQueue.size();
	}
	
}
