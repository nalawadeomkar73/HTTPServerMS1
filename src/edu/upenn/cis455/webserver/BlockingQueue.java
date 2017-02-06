package edu.upenn.cis455.webserver;

import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BlockingQueue {
	private List queue = new LinkedList();
	private ArrayList<Socket> blockingQueue;
	//private int limit = 100;
	
	public BlockingQueue(){
		//this.limit = limit;
		blockingQueue = new ArrayList<Socket>();
	}
	
	
	public synchronized void enqueue(Socket socket)throws InterruptedException{
		//while(this.queue.size() == this.limit){
			//wait();
		//}
		//if(this.queue.size()==0){
			//notifyAll();
		//}
		this.blockingQueue.add(socket);
		this.notifyAll();
	}
	
	public synchronized Socket dequeue()throws InterruptedException{
		while(this.blockingQueue.size()==0){
			wait();
		}
		/*if(this.queue.size()==this.limit
			notifyAll();
		}*/
		return this.blockingQueue.remove(0);
	}
	
	

	public int checkSize() {
		// TODO Auto-generated method stub
		return blockingQueue.size();
	}
	
}
