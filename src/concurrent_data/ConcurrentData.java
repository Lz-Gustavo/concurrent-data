package concurrent_data;

import java.util.*;
import java.io.*;

public class ConcurrentData {
	//vector is an obselete class, but desired in this kind of simulation, 
	//since it doesnt implemment a sync. for a whole sequence of operations
	
	public static Vector ConfigParam(File config) {
		// extract info from config file
		Vector info = new Vector();

		try {
			Scanner scn = new Scanner(config);

			while(scn.hasNextLine()) {
				scn.next();
				info.add(scn.next());
			}
			return info;
		}
		catch (Exception e) {
			System.out.println("Exception: " +e);
			return info;
		}	
	}
	
	public static void GenerateWorkers(Vector config_param) {
		// instantiate worker objects and thread each one
	}
	
	public static void main(String[] args) {
		
		File config_file = new File("/home/lzgustavo/NetBeansProjects/concurrent-data/test/config.txt");
		Vector data = ConfigParam(config_file);
		
		System.out.println("Data Vector: ");
		for (int i = 0; i < data.size(); i++) 
			System.out.println(data.get(i));
		
		//GenerateWorkers(data);
	}
}
