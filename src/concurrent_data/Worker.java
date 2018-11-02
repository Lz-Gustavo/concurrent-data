package concurrent_data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class Worker implements Runnable{
	
	private HashMap Config;
	
	private final Object DataStruct;
	private int DataS;
	private final Random rand;
	
	private Iterator<String> iterator;
		
	private int num_read = 0;
	private int num_write = 0;
	private int num_remove = 0;
	
	private ArrayList<String> log_operations;
	private ArrayList<Long> latency;
	
	private byte[] write_value;
	
	private Semaphore mutex;
		
	public Worker(Object n_struct) {
		
		rand = new Random();
		latency = new ArrayList();
		log_operations = new ArrayList();
		mutex = new Semaphore(1);
		
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
		DataS = Integer.parseInt(Config.get("DATA:").toString());
		int DataLen = Integer.parseInt(Config.get("LEN:").toString());

		write_value = new byte[DataLen];
		for (int i = 0; i < DataLen; i++) {
			write_value[i] = '-';
		}
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

						Write(write_value, rand_pos);
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
							Write(write_value, rand_pos);
							long end_time = System.nanoTime();
							latency.add((end_time - start_time));
						}
						else
							Write(write_value, rand_pos);
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
		
		try {
			switch (DataS) {
				case 0:
					iterator = ((CopyOnWriteArrayList) DataStruct).iterator();

					//idk if its possible to lock a specific memory region, because
					//when iterator() is called it snapshots the entire data structure
					//thats why the two implementations of array list used in this example
					//use the iterator declaration as the only snapshot capture alternative,
					//but have different latency in run-time overhead since CoW capture is
					//much more costly.
					break;
				case 1:
					((ConcurrentMap) DataStruct).get(pos);
					break;
				case 3:
					((ArrayList) DataStruct).get(pos);
					break;
				case 4:
					synchronized(DataStruct) {
						((ArrayList) DataStruct).get(pos);
					}	break;
				case 5:
					mutex.acquire();
					((ArrayList) DataStruct).get(pos);
					mutex.release();
					break;
				default:
					break;
			}
			++num_read;
		}
		catch (Exception e) {
			System.out.println("ReadOpExcep: "+e);
		}
	}
	
	public void Write(byte[] x, int pos) throws InterruptedException {
		//write operation on the specific structure
		
		try {
			switch (DataS) {
				case 0:
					((CopyOnWriteArrayList) DataStruct).set(pos, x);
					break;
				case 1:
					((ConcurrentMap) DataStruct).put(pos, x);
					break;
				case 3:
					((ArrayList) DataStruct).set(pos, x);
					break;
				case 4:
					synchronized(DataStruct) {
						((ArrayList) DataStruct).set(pos, x);
					}	break;
				case 5:
					mutex.acquire();
					((ArrayList) DataStruct).set(pos, x);
					mutex.release();
					break;
				default:
					break;
			}

			++num_write;
		}
		catch (Exception e) {
			System.out.println("WriteOpExcep: "+e);
		}
	}
	
	public void Remove(int pos) {
		
		try {
			switch (DataS) {
				case 0:
					((CopyOnWriteArrayList) DataStruct).set(pos, " ");
					break;
				case 1:
					((ConcurrentMap) DataStruct).remove(pos);
					break;
				case 3:
					((ArrayList) DataStruct).set(pos, " ");
					break;
				case 4:
					synchronized(DataStruct) {
						((ArrayList) DataStruct).set(pos, " ");
					}	break;
				case 5:
					mutex.acquire();
					((ArrayList) DataStruct).set(pos, " ");
					mutex.release();
					break;
				default:
					break;
			}
			++num_remove;
		}
		catch (Exception e) {
			System.out.println("RemoveOpExcep: "+e);
		}
	}
	
	public void getStatus() {
		
		System.out.println("");
		System.out.println("Number of read operations: " +num_read);
		System.out.println("Number of write operations: " +num_write);
		System.out.println("Number of remove operations: "+num_remove);
	}
	
	public void Show() {
		
		if (DataStruct instanceof CopyOnWriteArrayList) {
			
			try {
				System.out.println("\n=====CoW Array Structure=====");
				for (int i = 0; i < ((CopyOnWriteArrayList) DataStruct).size(); i++) {
					System.out.println("["+i+"] " + Arrays.toString(serialize(((CopyOnWriteArrayList) DataStruct).get(i))));
				}

				System.out.println();
			}
			catch (Exception e) {

				System.out.println("Exception: "+e);
			}
			
		}
		else if (DataStruct instanceof ConcurrentMap) {

			try {
				System.out.println("\n=====Concurrent Map Structure=====");
				for (Object key : ((ConcurrentMap) DataStruct).keySet()) {
					System.out.println("["+key+"] " + Arrays.toString(serialize(((ConcurrentMap) DataStruct).get(key))));
				}
			}
			catch (Exception e) {

				System.out.println("Exception: "+e);
			}
		}
		else if (DataStruct instanceof ArrayList) {
			
			try {
				synchronized (DataStruct) {

					System.out.println("\n=====ArrayList Structure=====");
					for (int i = 0; i < ((ArrayList) DataStruct).size(); i++) {
						System.out.println("["+i+"] " + Arrays.toString(serialize(((ArrayList) DataStruct).get(i))));
					}
					System.out.println();
				}
			}
			catch (Exception e) {

				System.out.println("Exception: "+e);
			}
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
	
	public byte[] serialize(Object obj) throws IOException {
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out;
		byte[] Bytes = null;
			
		try {
			out = new ObjectOutputStream(bos);   
			out.writeObject(obj);
			out.flush();
			Bytes = bos.toByteArray();
		}
		catch (Exception e) {
			
			System.out.println("Exception: "+e);
		}
		finally {	
			bos.close();
		}
		
		return Bytes;
	}
}