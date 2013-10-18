import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * This interface represents a solution to a sokoban map, i.e.
 * a list of moves of the player that will place all boxes 
 * on the goals.
 */
public interface Solution {
	public void prepend (Deque<Move> moves);
	public void append(Deque<Move> moves);
	public Solution getForwardSolution();
	public Deque<Move> asDeque();
	public Deque<Deque<Move>> getPath();
}
