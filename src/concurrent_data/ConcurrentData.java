package concurrent_data;

import java.util.*;
import java.io.*;

public class ConcurrentData {
	//vector is an obselete class because it doesnt implemment a sync. for 
	//a whole sequence of operations, making the lock manage too costly
	
	public static HashMap ConfigParam(File config) {
		// extract info from config file
		HashMap info = new HashMap();

		try {
			Scanner scn = new Scanner(config);

			while(scn.hasNextLine()) {
				info.put(scn.next(), scn.next());
			}
			return info;
		}
		catch (Exception e) {
			System.out.println("Exception: " +e);
			return info;
		}	
	}
	
	public static void GenerateWorkers(HashMap config_param) {
		// instantiate worker objects and thread each one
	}
	
	public static void main(String[] args) {
		
		File config_file = new File("/home/lzgustavo/NetBeansProjects/concurrent-data/test/config.txt");
		HashMap data = ConfigParam(config_file);
		
		System.out.println("Data Vector: ");
		for (Object key : data.keySet()) {
			System.out.println(key + " " + data.get(key));
		}
		
		//GenerateWorkers(data);
	}
}
