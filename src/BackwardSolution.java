import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;


public class BackwardSolution extends AbstractSolution {
	
	@Override public Solution getForwardSolution() {
		Solution forward = new ForwardSolution();
		for (Iterator<Deque<Move>> outer = getPath().descendingIterator(); outer.hasNext();) {
			Deque<Move> moveBatch = outer.next();
			Deque<Move> forwardBatch = new LinkedList<>();
			for (Iterator<Move> inner = moveBatch.descendingIterator(); inner.hasNext();)
				forwardBatch.addLast(inner.next().inverse());
			forward.append(forwardBatch);
		}
		return forward;
	}

	// toString is the char representation of the move sequence
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<Deque<Move>> outer = getPath().descendingIterator(); outer.hasNext();) {
			Deque<Move> moveBatch = outer.next();
			for (Iterator<Move> inner = moveBatch.descendingIterator(); inner.hasNext();)
				sb.append(inner.next().inverse().toChar());
		}
		return sb.toString();
	}
}
