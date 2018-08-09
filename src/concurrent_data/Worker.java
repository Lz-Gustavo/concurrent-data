package concurrent_data;

import java.util.concurrent.*;
import java.util.Vector;

public class Worker {
	
	private final BlockingQueue Queue;
	private final ConcurrentMap Map;
	private final CopyOnWriteArrayList List;
	
	private int num_read = 0;
	private int num_write = 0;
	
	public Worker(BlockingQueue n_queue, ConcurrentMap n_map, CopyOnWriteArrayList n_list) {
		
		Queue = n_queue;
		Map = n_map;
		List = n_list;
	}
	
	public void Dispatch(Vector config) {
		// dispatch random operation using config file param.
	}
	
	public void Read(int pos, int f_pos, int data_struct) {
		// read operation on the specific structure
		num_read++;
	}
	
	public void Write(String x, int pos, int data_struct) {
		// write operation on the specific structure
		num_write++;
	}
}
