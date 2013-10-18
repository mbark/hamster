import java.util.Deque;
import java.util.LinkedList;


public abstract class AbstractSolution implements Solution {
	private final Deque<Deque<Move>> path = new LinkedList<>();

	@Override public void prepend (Deque<Move> moves) {
		getPath().addFirst (moves);
	}

	@Override public void append(Deque<Move> moves)  {
		getPath().addLast(moves);
	}
	
	@Override public Deque<Move> asDeque() {
		Deque<Move> asDeque = new LinkedList<>();
		for (Deque<Move> moveBatch : getPath()) {
			for (Move move : moveBatch) {
				asDeque.addLast(move);
			}
		}
		return asDeque;
	}

	@Override public Deque<Deque<Move>> getPath() {
		return path;
	}
}
