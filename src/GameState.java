import java.util.Deque;
import java.util.List;


public interface GameState {
	
	public static final char FREE_SPACE = ' ';
	public static final char WALL = '#';
	
	boolean isDone();
	List<GameState> getNextBoxStates ();
	Deque<Move> getMovesToHere ();
	int getDistanceToGoal();

}
