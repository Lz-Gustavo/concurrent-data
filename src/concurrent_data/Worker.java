package concurrent_data;

import java.util.concurrent.*;
import java.util.HashMap;
import java.util.Random;

public class Worker implements Runnable{
	
	private HashMap Config;
	
	private final Object DataStruct;
	private final Random rand;
		
	private int num_read = 0;
	private int num_write = 0;
		
	public Worker(Object n_struct) {
		
		rand = new Random();
		
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
	}
	
	@Override
	public void run() {
		// dispatch random operation using config file param.
		System.out.println("Worker Running!");
		
		try {
			int n_ops = Integer.parseInt(Config.get("OPS:").toString());
			for (int i = 0; i < n_ops; i++) {

				int random = rand.nextInt(100);

				if (random < Integer.parseInt(Config.get("READ(%):").toString())) {
					// nextInt(MAX_VALUE) + MIN_VALUE

					int ini_pos = rand.nextInt(Integer.parseInt(Config.get("R_MAX_POS:").toString()))
								+ Integer.parseInt(Config.get("R_MIN_POS:").toString());

					int fin_pos = rand.nextInt(Integer.parseInt(Config.get("R_MAX_POS:").toString()))
								+ Integer.parseInt(Config.get("R_MIN_POS:").toString());

					while (fin_pos < ini_pos) {
						ini_pos = rand.nextInt(Integer.parseInt(Config.get("R_MAX_POS:").toString()))
								+ Integer.parseInt(Config.get("R_MIN_POS:").toString());

						fin_pos = rand.nextInt(Integer.parseInt(Config.get("R_MAX_POS:").toString()))
								+ Integer.parseInt(Config.get("R_MIN_POS:").toString());
					}

					Read(ini_pos, fin_pos);
				}
				else {
					int pos = rand.nextInt(Integer.parseInt(Config.get("W_MAX_POS:").toString()))
								+ Integer.parseInt(Config.get("W_MIN_POS:").toString());

					Write("conteudo", pos);
				}
				
				Thread.sleep(Integer.parseInt(Config.get("T_TIME(msec):").toString()));
			}
		}
		catch (InterruptedException e) {
			
			System.out.println("Exception: " +e);
		}
	}
	
	public void Read(int pos, int f_pos) {
		//TODO read operation on the specific structure
		num_read++;
	}
	
	public void Write(String x, int pos) {
		//TODO write operation on the specific structure
		num_write++;
	}
	
	public void getStatus() {
		
		System.out.println("");
		System.out.println("Number of read operations: " +num_read);
		System.out.println("Number of write operations: " +num_write);
	}
}
