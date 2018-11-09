package concurrent_data;

import java.util.concurrent.*;

public class TestHeap {

	public CopyOnWriteArrayList<byte[]> DataStruct;
	
	public TestHeap() {
		DataStruct = new CopyOnWriteArrayList<byte[]>();
	}
	
	public static void main(String args[]) {
	
		
		TestHeap t = new TestHeap();
		
		System.out.println("testing heap....");
		
		try {
			for (int i = 0; i < 1000000; i++) {
				t.DataStruct.add(new byte[512]);
				Thread.sleep(1);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Test finished");
	}
}
