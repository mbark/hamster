import java.util.RandomAccess;


public class Penis implements RandomAccess {
	
	public void performStuff () {
		for (int i = 0; i < 1000; i++) {
			new Thread(new Runnable() {
				@Override public void run() {
					runka ();
				}
			});
		}
	}

	private void runka() {
		return; // return. dirr.
	}
}
