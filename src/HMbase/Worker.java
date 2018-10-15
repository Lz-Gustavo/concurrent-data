package HMbase;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.HashMap;
import java.util.Random;

public class Worker implements Runnable {
	
	private HashMap Config;
	
	private final Object DataStruct;
	private final Random rand;
		
	private int num_read = 0;
	private int num_write = 0;
	private int num_remove = 0;
	
	private ArrayList<String> log_operations;
	private ArrayList<Long> latency;
		
	public Worker(Object n_struct) {
		
		rand = new Random();
		latency = new ArrayList();
		log_operations = new ArrayList();

		DataStruct = (ConcurrentMap) n_struct;
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
			int t_time = Integer.parseInt(Config.get("T_TIME(msec):").toString());
			
			if ((read_perc + write_perc + remove_perc) > 100) {
				throw new java.lang.RuntimeException("Percentage values out of range.");
			}
			
			int random, rand_pos;
			int total_ops = Integer.parseInt(Config.get("OPS:").toString());
			int n_ops = total_ops/Integer.parseInt(Config.get("WORKERS:").toString());
			
			//System.out.println("Execute "+n_ops+" operations.");
			
			if ((Integer.parseInt(Config.get("SUPERVISOR:").toString())) > 0) {
				
				for (;;) {

					random = rand.nextInt(100);

					if  (random < read_perc) {

						rand_pos = rand.nextInt(Integer.parseInt(Config.get("R_MAX_POS:").toString()) + 1)
									+ Integer.parseInt(Config.get("R_MIN_POS:").toString());

						Read(rand_pos);
					}
					else if (random < (write_perc + read_perc)) {
						rand_pos = rand.nextInt(Integer.parseInt(Config.get("W_MAX_POS:").toString()) + 1)
									+ Integer.parseInt(Config.get("W_MIN_POS:").toString());

						Write("conteudo", rand_pos);
					}
					else {
						rand_pos = rand.nextInt(Integer.parseInt(Config.get("TAM:").toString()) - 1);
						
						Remove(rand_pos);
					}

					if (t_time > 0)
						Thread.sleep(t_time);
				}
			}
			else {
				for (int i = 0; i < n_ops; i++) {

					random = rand.nextInt(100);

					if  (random < read_perc) {
						// nextInt(MAX_VALUE) + MIN_VALUE

						rand_pos = rand.nextInt(Integer.parseInt(Config.get("R_MAX_POS:").toString()) + 1)
									+ Integer.parseInt(Config.get("R_MIN_POS:").toString());

						if (i%100 == 0) {

							//since each worker has its own log, its not a critical region
							//so we dont need to lock access through a semaphore
							log_operations.add("Read "+rand_pos);
							long start_time = System.nanoTime();
							Read(rand_pos);
							long end_time = System.nanoTime();
							latency.add((end_time - start_time));
						}
						else
							Read(rand_pos);
					}
					else if (random < (write_perc + read_perc)) {
						rand_pos = rand.nextInt(Integer.parseInt(Config.get("W_MAX_POS:").toString()) + 1)
									+ Integer.parseInt(Config.get("W_MIN_POS:").toString());

						if (i%100 == 0) {

							log_operations.add("Write "+rand_pos);
							long start_time = System.nanoTime();
							Write("conteudo", rand_pos);
							long end_time = System.nanoTime();
							latency.add((end_time - start_time));
						}
						else
							Write("conteudo", rand_pos);
					}
					else {
						rand_pos = rand.nextInt(Integer.parseInt(Config.get("TAM:").toString()) - 1);
						if (i%100 == 0) {

							log_operations.add("Remove "+rand_pos);
							long start_time = System.nanoTime();
							Remove(rand_pos);
							long end_time = System.nanoTime();
							latency.add((end_time - start_time));
						}
						else
							Remove(rand_pos);
					}

					if (t_time > 0)
						Thread.sleep(t_time);
				}
			}
		}
		catch (InterruptedException e) {
			
			System.out.println("Write Exception: " +e);
			Thread.currentThread().interrupt();
		}
		catch (Exception e) {
			
			System.out.println("Exception: "+e);
		}
	}
	
	public void Read(int pos) {
		
		//creating an iterator creates an immutable snapshot of the data
		//in the list at the time iterator() was called

		((ConcurrentMap) DataStruct).get(pos);
		++num_read;
	}
	
	public void Write(String x, int pos) throws InterruptedException {
		//write operation on the specific structure
		
		((ConcurrentMap) DataStruct).put(pos, x);
		++num_write;
	}
	
	public void Remove(int pos) {
		
		((ConcurrentMap) DataStruct).remove(pos);
		++num_remove;
	}
	
	public void getStatus() {
		
		System.out.println("");
		System.out.println("Number of read operations: " +num_read);
		System.out.println("Number of write operations: " +num_write);
		System.out.println("Number of remove operations: "+num_remove);
	}
	
	public void Show() {
		
		System.out.println("\n=====Concurrent Map Structure=====");
		for (Object key : ((ConcurrentMap) DataStruct).keySet()) {
			System.out.println("["+key+"] " + ((ConcurrentMap) DataStruct).get(key));
		}
	}
	
	public ArrayList getLog() {
		return log_operations;
	}
	
	public ArrayList getLatency() {
		return latency;
	}
	
	public ArrayList getNum() {
		
		ArrayList info = new ArrayList();
		
		info.add(num_read);
		info.add(num_remove);
		info.add(num_write);
	
		return info;
	}
}