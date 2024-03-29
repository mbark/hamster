import java.util.Deque;
import java.util.List;
import java.util.Set;


public interface GameState {
	
	public static final char FREE_SPACE = ' ';
	public static final char WALL = '#';
	public static final char PLAYER = '@';
	
	boolean isDone();
	List<GameState> getNextBoxStates ();
	Deque<Move> getMovesToHere ();
	int getDistanceToGoal();
	int difference (GameState gameState);
	int numObstacles();
	Set<Box> getBoxes();
	Location getPlayerLocation ();
	GameState getPlayerMoveGameState (Location l);
	List<GameState> tryGoalMacro();
	void markBoxAsFinished (Box box);
	
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
