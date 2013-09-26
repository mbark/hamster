import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;


/**
 * This class represents a solution to a sokoban map, i.e.
 * a list of moves of the player that will place all boxes 
 * on the goals.
 * @author Jonas Sköld
 */
public class Solution {
	private final List<Deque<Move>> path;

	public Solution() {
		path = new ArrayList<>();
	}
	
	public void append (Deque<Move> moves) {
		path.add(moves);
	}
	
	// toString is the char representation of the move sequence
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Deque<Move> moves : path) {
			Iterator<Move> iterator = moves.descendingIterator();
			while(iterator.hasNext()) {
				sb.append(iterator.next().inverse().toChar());
			}
		}
		return sb.toString();
	}
}
