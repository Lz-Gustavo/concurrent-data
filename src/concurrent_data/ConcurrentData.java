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
			System.err.format("Exception: %s%n", e);
		}
		
		return info;
	}
	
	public static void FillStruct(HashMap config) {
		//populates data structure with spaces, it can be syncronized for every test case of array
		//list since its not going to be executed currently with any other structure method
		
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
		else if (config_param.get("DATA:").equals("2")) {
			// TODO: implement here the new added datastruct
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
			
			if (Integer.parseInt(config_param.get("SUPERVISOR:").toString()) > 0) {
				
				Timer temp = new Timer(Integer.parseInt(config_param.get("SUPERVISOR:").toString()));
				Thread time = new Thread(temp);
				time.start();
				
				time.join();
				System.out.println("Worker finished!");
			
				for (i = 0; i < number_w; i++) {
					workers_t[i].interrupt();
				}
			}
			
			else {
	
				for (i = 0; i < number_w; i++) {
					workers_t[i].join();
				}
			}
		}
		catch (InterruptedException e) {
			System.err.format("Exception: %s%n", e);
		}
	}
	
	public static void main(String[] args) {
		
		File config_file = new File("/home/lzgustavo/NetBeansProjects/concurrent-data/test/config.txt");
		HashMap data = ConfigParam(config_file);
		
		if (args.length == 1)
			data.put("WORKERS:", args[0]);
		
		workers = new ArrayList<>();
		GenerateWorkers(data);
		
		if (data.get("LOG:").toString().equals("1")) {
			
			System.out.println("Data Vector: ");
			for (Object key : data.keySet()) {
				System.out.println(key + " " + data.get(key));
			}
		}
		
		Path file_remove = Paths.get("/home/lzgustavo/NetBeansProjects/concurrent-data/test/log-remove-"+data.get("WORKERS:").toString()+"t.txt");
		Path file_read = Paths.get("/home/lzgustavo/NetBeansProjects/concurrent-data/test/log-read-"+data.get("WORKERS:").toString()+"t.txt");
		Path file_write = Paths.get("/home/lzgustavo/NetBeansProjects/concurrent-data/test/log-write-"+data.get("WORKERS:").toString()+"t.txt");

		try {

			// create the empty file with default permissions, etc.
			if (!"0".equals(data.get("REMOVE(%):").toString())) {
				Files.deleteIfExists(file_remove);
				Files.createFile(file_remove);
			}
			
			if (!"0".equals(data.get("READ(%):").toString())) {
				Files.deleteIfExists(file_read);
				Files.createFile(file_read);
			}
			
			if (!"0".equals(data.get("WRITE(%):").toString())) {
				Files.deleteIfExists(file_write);
				Files.createFile(file_write);
			}

			for (int i = 0; i < workers.size(); i++) {
				
				if (data.get("LOG:").toString().equals("1"))
					workers.get(i).getStatus();
				
				ArrayList log_ops = workers.get(i).getLog();
				ArrayList latency = workers.get(i).getLatency();
				ArrayList num = workers.get(i).getNum();

				String aux_remove = new String();
				String aux_read = new String();
				String aux_write = new String();
				
				if (Integer.parseInt(data.get("SUPERVISOR:").toString()) > 0) {
					
					aux_read += num.get(0) + "\n";
					aux_remove += num.get(1) + "\n";
					aux_write += num.get(2) + "\n";
				}
				
				else {
					
					for (int j = 0; j < log_ops.size(); j++) {

						if (log_ops.get(j).toString().contains("Remove"))
							//aux_remove += "worker" + i + " - " + log_ops.get(j) + " - " + latency.get(j) + "\n";
							aux_remove += latency.get(j) + "\n";

						else if (log_ops.get(j).toString().contains("Read"))
							//aux_read += "worker" + i + " - " + log_ops.get(j) + " - " + latency.get(j) + "\n";
							aux_read += latency.get(j) + "\n";
						else
							//aux_write += "worker" + i + " - " + log_ops.get(j) + " - " + latency.get(j) + "\n";
							aux_write += latency.get(j) + "\n";
					}
				}
				
				if (!"0".equals(data.get("REMOVE(%):").toString()))
					Files.write(file_remove, aux_remove.getBytes(), StandardOpenOption.APPEND);
				
				if (!"0".equals(data.get("READ(%):").toString()))
					Files.write(file_read, aux_read.getBytes(), StandardOpenOption.APPEND);
				
				if (!"0".equals(data.get("WRITE(%):").toString()))
					Files.write(file_write, aux_write.getBytes(), StandardOpenOption.APPEND);
			}
		}
		catch (FileAlreadyExistsException x) {
			System.err.format("file named already exists %s%n", x);
		}
		catch (IOException x) {	
			System.err.format("createFile error: %s%n", x);
		}
		catch (Exception y) {
			System.err.format("Exception encountered: %s%n", y);
		}

		if (data.get("LOG:").toString().equals("1")) {
			
			//all workers operate on the same data structure reference
			workers.get(0).Show();
			
			Scanner scan = new Scanner(System.in);
			
			String msg;
			//lock execution till any key input
			do {
				msg = scan.nextLine();
			} while (!msg.equals(""));
		}
	}
}
