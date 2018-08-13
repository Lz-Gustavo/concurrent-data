package concurrent_data;

import java.util.*;
import java.io.*;
import java.util.concurrent.*;

public class ConcurrentData {
	//vector is an obselete class because it doesnt implemment a sync. for 
	//a whole sequence of operations, making the lock manage too costly
	
	private static ArrayList<Worker> workers;
	private static Thread workers_t[];
	
	//IDEA: use an unique object pointer to reference a single datastruct, 
	//choosen from config param.
	private static Object DataStruct;	
	
//	private BlockingQueue Queue = null;
//	private ConcurrentMap Map = null;
//	private CopyOnWriteArrayList List = null;
	
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
		
		if (DataStruct instanceof CopyOnWriteArrayList) {
			
			for (int i = 0; i < Integer.parseInt(config.get("TAM:").toString()); i++) {
				((CopyOnWriteArrayList) DataStruct).add(" ");
			}
		}
	}
	
	public static void GenerateWorkers(HashMap config_param) {
		// instantiate worker objects and thread each one
		
//		if (config_param.get("DATA:").equals("0")) {
//			DataStruct = new BlockingQueue();
//		}
//		else if (config_param.get("DATA:").equals("1")) {
//			DataStruct = new ConcurrentMap();
//		}
		if (config_param.get("DATA:").equals("2")) {
			DataStruct = new CopyOnWriteArrayList();
		}
		
		int i;
		int number_w = Integer.parseInt(config_param.get("WORKERS:").toString());
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
		
		System.out.println("Data Vector: ");
		for (Object key : data.keySet()) {
			System.out.println(key + " " + data.get(key));
		}
		
		workers = new ArrayList<>();
		GenerateWorkers(data);
		
		if (data.get("LOG:").toString().equals("1")) {
			for (int i = 0; i < workers.size(); i++)
				workers.get(i).getStatus();
			
			//all workers operate on the same data structure reference
			workers.get(0).Show();
		}
	}
}
