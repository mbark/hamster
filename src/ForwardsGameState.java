import java.util.ArrayList;
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
		List<GameState> nextStates = new ArrayList<>();

		List<BoxMove> possibleBoxMoves = new ArrayList<>();
		for (Box box : boxes) {
			List<Move> possibleMoves = getPossibleMoves(box);
			for (Move move : possibleMoves)
				possibleBoxMoves.add (new BoxMove(box, move));
		}
		// TODO lol
		return nextStates;
	}

	@Override public int getDistanceToGoal() {
		// TODO Auto-generated method stub
		return 0;
	}

	private List<Move> getPossibleMoves (Movable<?> m) {
		List<Move> possibleMoves = new ArrayList<>();
		Location oneUp = m.getLocation().move(Move.UP);
		Location oneRight = m.getLocation().move(Move.RIGHT);
		Location oneDown = m.getLocation().move(Move.DOWN);
		Location oneLeft = m.getLocation().move(Move.LEFT);
		if (isFreeForPlayer(oneUp, oneDown)) {
			possibleMoves.add(Move.UP);
			possibleMoves.add(Move.DOWN);
		}
		if (isFreeForPlayer(oneRight, oneLeft)) {
			possibleMoves.add(Move.RIGHT);
			possibleMoves.add(Move.LEFT);
		}
		return possibleMoves;
	}
	
	public static BackwardsGameState calculateBoard(List<String> boardStrings) {
		return null;
	}
}
