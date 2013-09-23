import java.util.ArrayList;
import java.util.List;


/**
 * This class represents a solution to a sokoban map, i.e.
 * a list of moves of the player that will place all boxes 
 * on the goals.
 * @author Jonas Sk�ld
 */
public class Solution {
	private final List<Move> path;

	public Solution() {
		path = new ArrayList<>();
	}
	
	public Solution(List<Move> path) {
		this.path = path;
	}
	
	public void append (Move move) {
		path.add(move);
	}
	
	// toString is the char representation of the move sequence
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Move move : path)
			sb.append(move.toChar ());
		return sb.toString();
	}
}
