package concurrent_data;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class Worker implements Runnable{
	
	private HashMap Config;
	
	private final Object DataStruct;
	private final Random rand;
	
	private Iterator<String> iterator;
		
	private int num_read = 0;
	private int num_write = 0;
		
	public Worker(Object n_struct) {
		
		rand = new Random();
		
		if (n_struct instanceof CopyOnWriteArrayList)
			DataStruct = (CopyOnWriteArrayList) n_struct;
		
		else if (n_struct instanceof ConcurrentMap)
			DataStruct = (ConcurrentMap) n_struct;
		
		else if (n_struct instanceof ArrayList)
			DataStruct = (ArrayList) n_struct;
		
		else if (n_struct instanceof BlockingQueue)
			DataStruct = (BlockingQueue) n_struct;
		
		else
			DataStruct = null;
	}
	
	public void LoadConfig(HashMap config) {
		Config = config;
	}
	
	@Override
	public void run() {
		// dispatch random operation using config file param.
		System.out.println("Worker Running!");
		
		// TODO check latency between operations dispatch-finish executing
		// and heap memory in-use by each simulation
		
		try {
			int n_ops = Integer.parseInt(Config.get("OPS:").toString());
			for (int i = 0; i < n_ops; i++) {

				int random = rand.nextInt(100);

				if (random < Integer.parseInt(Config.get("READ(%):").toString())) {
					// nextInt(MAX_VALUE) + MIN_VALUE
					
					int ini_pos, fin_pos;
					
					do {
						ini_pos = rand.nextInt(Integer.parseInt(Config.get("R_MAX_POS:").toString()) + 1)
								+ Integer.parseInt(Config.get("R_MIN_POS:").toString());

						fin_pos = rand.nextInt(Integer.parseInt(Config.get("R_MAX_POS:").toString()) + 1)
								+ Integer.parseInt(Config.get("R_MIN_POS:").toString());
					
					} while (fin_pos < ini_pos);

					Read(ini_pos, fin_pos);
				}
				else {
					int pos = rand.nextInt(Integer.parseInt(Config.get("W_MAX_POS:").toString()) + 1)
								+ Integer.parseInt(Config.get("W_MIN_POS:").toString());

					Write("conteudo", pos);
				}
				
				Thread.sleep(Integer.parseInt(Config.get("T_TIME(msec):").toString()));
			}
		}
		catch (InterruptedException e) {
			
			System.out.println("Write Exception: " +e);
			//Thread.currentThread().interrupt();
		}
	}
	
	public void Read(int pos, int f_pos) {
		
		try {
			//creating an iterator creates an immutable snapshot of the data
			//in the list at the time iterator() was called
			if (DataStruct instanceof CopyOnWriteArrayList) {

				iterator = ((CopyOnWriteArrayList) DataStruct).iterator();
				//idk if its possible to lock a specific memory region, because
				//when iterator() is called it snapshots the entire data structure
			}
			else if (DataStruct instanceof ConcurrentMap) {
				
				((ConcurrentMap) DataStruct).get(pos);
			}
			else if (DataStruct instanceof ArrayList) {
				
				synchronized(DataStruct) {
					iterator = ((ArrayList) DataStruct).iterator();
				}
			}
			else if (DataStruct instanceof BlockingQueue) {
				//since WRITE just adds a single element, READS takes a single
				//one and blocks if not avaiable

				((BlockingQueue) DataStruct).take();
			}
		}
		catch (InterruptedException e) {
			
			System.out.println("Read Exception: " +e);
			//Thread.currentThread().interrupt();
		}
		
		num_read++;
	}
	
	public void Write(String x, int pos) throws InterruptedException {
		//write operation on the specific structure
		
		if (DataStruct instanceof CopyOnWriteArrayList) {
			((CopyOnWriteArrayList) DataStruct).set(pos, x);
		}
		else if (DataStruct instanceof ConcurrentMap) {
			((ConcurrentMap) DataStruct).put(pos, x);
		}
		else if (DataStruct instanceof ArrayList) {
			
			synchronized(DataStruct) {
				((ArrayList) DataStruct).set(pos, x);
			}
		}
		else if (DataStruct instanceof BlockingQueue) {
			((BlockingQueue) DataStruct).add(x);
		}
		
		num_write++;
	}
	
	public void getStatus() {
		
		System.out.println("");
		System.out.println("Number of read operations: " +num_read);
		System.out.println("Number of write operations: " +num_write);
	}
	
	public void Show() {
		
		if (DataStruct instanceof CopyOnWriteArrayList) {
			
			System.out.println("\n=====CoW Array Structure=====");
			for (int i = 0; i < ((CopyOnWriteArrayList) DataStruct).size(); i++) {
				System.out.println("["+i+"] " +((CopyOnWriteArrayList) DataStruct).get(i));
			}
			System.out.println();
		}
		else if (DataStruct instanceof ConcurrentMap) {
			
			System.out.println("\n=====Concurrent Map Structure=====");
			for (Object key : ((ConcurrentMap) DataStruct).keySet()) {
				System.out.println("["+key+"] " + ((ConcurrentMap) DataStruct).get(key));
			}
		}
		else if (DataStruct instanceof ArrayList) {
			
			System.out.println("\n=====ArrayList Structure=====");
			for (int i = 0; i < ((ArrayList) DataStruct).size(); i++) {
				System.out.println("["+i+"] " +((ArrayList) DataStruct).get(i));
			}
			System.out.println();
		}
		else if (DataStruct instanceof BlockingQueue) {
			//how to check state of the struct if reading takes out
			//head element?
		}
	}
}