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
	
	@Override public Deque<Move> asDeque() {
		Deque<Move> asDeque = new LinkedList<>();
		for (Deque<Move> moveBatch : path) {
			for (Move move : moveBatch) {
				asDeque.addLast(move);
			}
		}
		return asDeque;
	}
}
