package HMbase;

public class Timer implements Runnable {
	
	private final int time;
	
	public Timer(int interval) {
		time = interval;
	}
	
	@Override
	public void run() {
		
		try {
			Thread.sleep(time);
		}
		catch (InterruptedException e) {
			
			System.out.println("Write Exception: " +e);
			Thread.currentThread().interrupt();
		}
		catch (Exception e) {
			
			System.out.println("Exception: "+e);
		}
	}
}
