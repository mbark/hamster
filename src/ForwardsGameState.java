import java.util.Deque;
import java.util.List;
import java.util.Set;


public class ForwardsGameState extends AbstractGameState {

	ForwardsGameState (Board board, Player player, Set<Box> boxes) {
		super(board, player, boxes);
	}
	
	ForwardsGameState (Board board, Player player, Set<Box> boxes, Move lastMove) {
		super(board, player, boxes, lastMove);
	}

	ForwardsGameState (Board board, Player player, Set<Box> boxes, Deque<Move> movesToHere) {
		super(board, player, boxes, movesToHere);
	}

	@Override public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override public List<GameState> getNextBoxStates() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public int getDistanceToGoal() {
		// TODO Auto-generated method stub
		return 0;
	}

}
