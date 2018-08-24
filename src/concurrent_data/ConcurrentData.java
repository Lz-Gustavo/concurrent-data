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
	
	/*public ConcurrentData() {
		// RAII
		
		try {
			File config_file = new File("/home/lzgustavo/NetBeansProjects/concurrent-data/test/config.txt");
			
			file_remove = Paths.get("/home/lzgustavo/NetBeansProjects/concurrent-data/test/log-remove.txt");
			file_read = Paths.get("/home/lzgustavo/NetBeansProjects/concurrent-data/test/log-read.txt");
			file_write = Paths.get("/home/lzgustavo/NetBeansProjects/concurrent-data/test/log-write.txt");

			Files.createFile(file_remove);
			Files.createFile(file_read);
			Files.createFile(file_write);
		}
		catch (FileAlreadyExistsException x) {
			System.err.format("file named already exists %s%n", x);
		}
		catch (IOException x) {	
			System.err.format("createFile error: %s%n", x);
		}
		catch (Exception e) {
			System.err.format("Exception caught: %s%n", e);
		}
	}*/
	
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
			System.err.format("Exception: %s%n", e);
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
		}

		Path file_remove = Paths.get("/home/lzgustavo/NetBeansProjects/concurrent-data/test/log-remove.txt");
		Path file_read = Paths.get("/home/lzgustavo/NetBeansProjects/concurrent-data/test/log-read.txt");
		Path file_write = Paths.get("/home/lzgustavo/NetBeansProjects/concurrent-data/test/log-write.txt");

		try {

			// create the empty file with default permissions, etc.
			Files.createFile(file_remove);
			Files.createFile(file_read);
			Files.createFile(file_write);

			for (int i = 0; i < workers.size(); i++) {
				workers.get(i).getStatus();

				ArrayList log_ops = workers.get(i).getLog();
				ArrayList latency = workers.get(i).getLatency();

				String aux_remove = new String();
				String aux_read = new String();
				String aux_write = new String();

				for (int j = 0; j < log_ops.size(); j++) {

					if (log_ops.get(j).toString().contains("Remove"))
						aux_remove += "worker" + i + " - " + log_ops.get(j) + " - " + latency.get(j) + "\n";
					else if (log_ops.get(j).toString().contains("Read"))
						aux_read += "worker" + i + " - " + log_ops.get(j) + " - " + latency.get(j) + "\n";
					else
						aux_write += "worker" + i + " - " + log_ops.get(j) + " - " + latency.get(j) + "\n";
				}

				Files.write(file_remove, aux_remove.getBytes(), StandardOpenOption.APPEND);
				Files.write(file_read, aux_read.getBytes(), StandardOpenOption.APPEND);
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

		//all workers operate on the same data structure reference
		workers.get(0).Show();

		//lock execution till any key input
		Scanner scan = new Scanner(System.in);
		String msg;
		do {
			msg = scan.nextLine();
		} while (!msg.equals(""));
			
		
	}
}
