import java.util.Deque;
import java.util.List;


public interface GameState {
	
	public static final char FREE_SPACE = ' ';
	public static final char WALL = '#';
	
	boolean isDone();
	List<GameState> getNextBoxStates ();
	Deque<Move> getMovesToHere ();
	int getDistanceToGoal();
	
	/**
	 * Simple class used to keep track of the move performed on a box.
	 */
	static class BoxMove {
		final Box box;
		final Move move;
		
		public BoxMove(Box box, Move move) {
			this.box = box;
			this.move = move;
		}
		
		@Override public String toString() {
			return box.toString() + " " + move.toString();
		}
		
		@Override public int hashCode() {
			return box.hashCode() + move.hashCode();
		}
		
		@Override public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof BoxMove))
				return false;
			BoxMove bm = (BoxMove) o;
			return box.equals(bm.box) && move.equals(bm.move);
		}
	}
}
