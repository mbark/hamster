import java.util.Deque;
import java.util.LinkedList;


public abstract class AbstractSolution implements Solution {
	protected final Deque<Deque<Move>> path = new LinkedList<>();

	@Override public void prepend (Deque<Move> moves) {
		path.addFirst (moves);
	}

	@Override public void append(Deque<Move> moves)  {
		path.addLast(moves);
	}
}
