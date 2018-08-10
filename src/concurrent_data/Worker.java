package concurrent_data;

import java.util.concurrent.*;
import java.util.HashMap;

public class Worker implements Runnable{
	
	private HashMap Config;
	
	private final Object DataStruct; 
		
	private int num_read = 0;
	private int num_write = 0;
		
	public Worker(Object n_struct) {
		
		if (n_struct instanceof CopyOnWriteArrayList)
			DataStruct = (CopyOnWriteArrayList) n_struct;

		else if (n_struct instanceof ConcurrentMap)
			DataStruct = (ConcurrentMap) n_struct;
		
		else if (n_struct instanceof BlockingQueue)
			DataStruct = (BlockingQueue) n_struct;
		
		else
			DataStruct = null;
	}
	
	public void LoadConfig(HashMap config) {
		Config = config;
		
		CopyOnWriteArrayList test;
	}
	
	@Override
	public void run() {
		// dispatch random operation using config file param.
		System.out.println("Worker Running!");
		
	}
	
	public void Read(int pos, int f_pos, int data_struct) {
		// read operation on the specific structure
		num_read++;
	}
	
	public void Write(String x, int pos, int data_struct) {
		// write operation on the specific structure
		num_write++;
	}
}
