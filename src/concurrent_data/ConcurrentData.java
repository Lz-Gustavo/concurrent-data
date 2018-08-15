package concurrent_data;

import java.io.*;
import java.nio.file.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;

public class ConcurrentData {
	//vector is an obselete class because it doesnt implemment a sync. for 
	//a whole sequence of operations, making the lock manage too costly
	
	private static ArrayList<Worker> workers;
	private static Thread workers_t[];
	
	//IDEA: use an unique object pointer to reference a single datastruct, 
	//choosen from config param.
	private static Object DataStruct;
	
	public static HashMap ConfigParam(File config) {
		// extract info from config file
		HashMap info = new HashMap();

		try {
			Scanner scn = new Scanner(config);

			while(scn.hasNextLine()) {
				info.put(scn.next(), scn.next());
			}
		}
		catch (FileNotFoundException e) {
			System.out.println("Exception: " +e);
		}
		
		return info;
	}
	
	public static void FillStruct(HashMap config) {
		//populates data structure with spaces
		
		if (DataStruct instanceof CopyOnWriteArrayList) {
			
			for (int i = 0; i < Integer.parseInt(config.get("TAM:").toString()); i++) {
				((CopyOnWriteArrayList) DataStruct).add(" ");
			}
		}
		else if (DataStruct instanceof ConcurrentMap) {
			
			for (int i = 0; i < Integer.parseInt(config.get("TAM:").toString()); i++) {
				((ConcurrentMap) DataStruct).put(i, " ");
			}
		}
		else if (DataStruct instanceof ArrayList) {
			
			synchronized(DataStruct) {
				for (int i = 0; i < Integer.parseInt(config.get("TAM:").toString()); i++) {
					((ArrayList) DataStruct).add(" ");
				}
			}
		}
	}
	
	public static void GenerateWorkers(HashMap config_param) {
		// instantiate worker objects and thread each one
		
		if (config_param.get("DATA:").equals("0")) {
			DataStruct = new CopyOnWriteArrayList();
		}
		else if (config_param.get("DATA:").equals("1")) {
			DataStruct = new ConcurrentHashMap();
		}
		else {
			DataStruct = new ArrayList();
		}
		
		int i, number_w = Integer.parseInt(config_param.get("WORKERS:").toString());
		FillStruct(config_param);
		
		for (i = 0; i < number_w; i++) {
			
			if (config_param.get("LOG:").toString().equals("1"))
				System.out.println("worker created.");
			
			workers.add(new Worker(DataStruct));
			workers.get(i).LoadConfig(config_param);
		}

		try {
			workers_t = new Thread[number_w];
			
			for (i = 0; i < number_w; i++) {
				workers_t[i] = new Thread(workers.get(i));
				workers_t[i].start();
			}
			
			for (i = 0; i < number_w; i++) {
				workers_t[i].join();
			}
		}
		catch (InterruptedException e) {
			System.out.println("Exception: " +e);
		}
	}
	
	public static void main(String[] args) {
		
		File config_file = new File("/home/lzgustavo/NetBeansProjects/concurrent-data/test/config.txt");
		HashMap data = ConfigParam(config_file);
		
		workers = new ArrayList<>();
		GenerateWorkers(data);
		
		if (data.get("LOG:").toString().equals("1")) {
			
			System.out.println("Data Vector: ");
			for (Object key : data.keySet()) {
				System.out.println(key + " " + data.get(key));
			}

			for (int i = 0; i < workers.size(); i++) {
				workers.get(i).getStatus();

				Path file = Paths.get("/home/lzgustavo/NetBeansProjects/concurrent-data/test/log-worker"+i+".txt");
				try {
					// Create the empty file with default permissions, etc.
					Files.createFile(file);
					
					ArrayList log_ops = workers.get(i).getLog();
					ArrayList latency = workers.get(i).getLatency();
					
					String aux_buff = new String();
					
					for (int j = 0; j < log_ops.size(); j++) {
						
						aux_buff += log_ops.get(j) + " - " + latency.get(j) + "\n";
					}
					
					byte[] buffer = aux_buff.getBytes();
					Files.write(file, buffer);
				}
				catch (FileAlreadyExistsException x) {
					System.err.format("file named %s" +" already exists %s%n", file, x);
				}
				catch (IOException x) {	
					System.err.format("createFile error: %s%n", x);
				}
			}
			
			//all workers operate on the same data structure reference
			workers.get(0).Show();
		}
	}
}
