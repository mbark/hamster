import java.util.Deque;
import java.util.Iterator;


public class ForwardSolution extends AbstractSolution {

	@Override public Solution getForwardSolution() {
		return this;
	}

	// toString is the char representation of the move sequence
	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<Deque<Move>> outer = getPath().iterator(); outer.hasNext();) {
			Deque<Move> moveBatch = outer.next();
			for (Iterator<Move> inner = moveBatch.iterator(); inner.hasNext();)
				sb.append(inner.next().toChar());
		}
		return sb.toString();
	}
}
