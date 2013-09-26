import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * This class represents a solution to a sokoban map, i.e.
 * a list of moves of the player that will place all boxes 
 * on the goals.
 * @author Jonas Sköld
 */
public class Solution {
	private final Deque<Deque<Move>> path = new LinkedList<>();
	
	public void prepend (Deque<Move> moves) {
		path.addFirst (moves);
	}
	
	// toString is the char representation of the move sequence
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<Deque<Move>> outer = path.descendingIterator(); outer.hasNext();) {
			Deque<Move> moveBatch = outer.next();
			for (Iterator<Move> inner = moveBatch.descendingIterator(); inner.hasNext();)
				sb.append(inner.next().inverse().toChar());
		}
		return sb.toString();
	}
}
