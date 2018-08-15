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
	private int num_remove = 0;
		
	public Worker(Object n_struct) {
		
		rand = new Random();
		
		if (n_struct instanceof CopyOnWriteArrayList)
			DataStruct = (CopyOnWriteArrayList) n_struct;
		
		else if (n_struct instanceof ConcurrentMap)
			DataStruct = (ConcurrentMap) n_struct;
		
		else if (n_struct instanceof ArrayList)
			DataStruct = (ArrayList) n_struct;
		
		else
			DataStruct = null;
	}
	
	public void LoadConfig(HashMap config) {
		Config = config;
	}
	
	@Override
	public void run() {
		// dispatch random operation using config file param.
		if (Config.get("LOG:").toString().equals("1"))
			System.out.println("Worker Running!");
		
		// TODO check latency between operations dispatch-finish executing
		// and heap memory in-use by each simulation
		
		try {
			int read_perc = Integer.parseInt(Config.get("READ(%):").toString());
			int write_perc = Integer.parseInt(Config.get("WRITE(%):").toString());
			int remove_perc = Integer.parseInt(Config.get("REMOVE(%):").toString());
			
			if ((read_perc + write_perc + remove_perc) > 100) {
				System.out.println("Percentage values out of range.");
				return;
			}
			
			int ini_pos, fin_pos, random, pos;
			int n_ops = Integer.parseInt(Config.get("OPS:").toString());
			
			for (int i = 0; i < n_ops; i++) {

				random = rand.nextInt(100);

				if  (random < read_perc) {
					// nextInt(MAX_VALUE) + MIN_VALUE
					
					do {
						ini_pos = rand.nextInt(Integer.parseInt(Config.get("R_MAX_POS:").toString()) + 1)
								+ Integer.parseInt(Config.get("R_MIN_POS:").toString());

						fin_pos = rand.nextInt(Integer.parseInt(Config.get("R_MAX_POS:").toString()) + 1)
								+ Integer.parseInt(Config.get("R_MIN_POS:").toString());
					
					} while (fin_pos < ini_pos);

					Read(ini_pos, fin_pos);
				}
				else if (random < (write_perc + read_perc)) {
					pos = rand.nextInt(Integer.parseInt(Config.get("W_MAX_POS:").toString()) + 1)
								+ Integer.parseInt(Config.get("W_MIN_POS:").toString());

					Write("conteudo", pos);
				}
				else {
					pos = rand.nextInt(Integer.parseInt(Config.get("TAM:").toString()) - 1);
					Remove(pos);
				}
				
				Thread.sleep(Integer.parseInt(Config.get("T_TIME(msec):").toString()));
			}
		}
		catch (InterruptedException e) {
			
			System.out.println("Write Exception: " +e);
			Thread.currentThread().interrupt();
		}
	}
	
	public void Read(int pos, int f_pos) {
		
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

		++num_read;
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
		
		++num_write;
	}
	
	public void Remove(int pos) {
		
		if (DataStruct instanceof CopyOnWriteArrayList) {
			((CopyOnWriteArrayList) DataStruct).set(pos, " ");
		}
		else if (DataStruct instanceof ConcurrentMap) {
			((ConcurrentMap) DataStruct).remove(pos);
			
		}
		else if (DataStruct instanceof ArrayList) {
			
			synchronized(DataStruct) {
				((ArrayList) DataStruct).set(pos, " ");
			}
		}
		
		++num_remove;
	}
	
	public void getStatus() {
		
		System.out.println("");
		System.out.println("Number of read operations: " +num_read);
		System.out.println("Number of write operations: " +num_write);
		System.out.println("Number of remove operations: "+num_remove);
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
			
			synchronized (DataStruct) {
				
				System.out.println("\n=====ArrayList Structure=====");
				for (int i = 0; i < ((ArrayList) DataStruct).size(); i++) {
					System.out.println("["+i+"] " +((ArrayList) DataStruct).get(i));
				}
				System.out.println();
			}
		}
	}
}