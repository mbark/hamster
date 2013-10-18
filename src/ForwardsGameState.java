import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

public class ForwardsGameState extends AbstractGameState {
	
	public static final char GOAL = '.';
	public static final char PLAYER_ON_GOAL = '+';
	public static final char BOX = '$';
	public static final char BOX_ON_GOAL = '*';
	private static final int MAX_GOAL_AREA_SIZE = 35;
	
	private static final Set<Box> finishedBoxes = new HashSet<>();
	
	ForwardsGameState (Board board, Player player, Set<Box> boxes) {
		super(board, player, boxes);
	}
	
	ForwardsGameState (Board board, Player player, Set<Box> boxes, Move lastMove) {
		super(board, player, boxes, lastMove);
	}

	ForwardsGameState (Board board, Player player, Set<Box> boxes, Deque<Move> movesToHere) {
		super(board, player, boxes, movesToHere);
	}
	
	ForwardsGameState (Board board, Player player, Set<Box> boxes, Set<Box> movableBoxes, Deque<Move> movesToHere) {
		super(board, player, boxes, movableBoxes, movesToHere);
	}

	@Override public boolean isDone() {
		return boxesAreDone();
	}

	@Override public List<GameState> getNextBoxStates() {
		List<GameState> nextStates = new ArrayList<>();

		List<BoxMove> possibleBoxMoves = new ArrayList<>();
		for (Box box : boxes) {
			if (!finishedBoxes.contains(box)) {
				List<Move> possibleMoves = getPossibleMoves(box);
				for (Move move : possibleMoves)
					possibleBoxMoves.add (new BoxMove(box, move));
			}
		}
		
		// this generates BoxMoves mapped to the paths they would require
		// if you were to PULL them. we therefore need to treat them
		// "backwards" to obtain forwards GameStates
		Map<BoxMove, Deque<Move>> movePaths = findBackwardsMovePathsBFS(possibleBoxMoves);
		for (Entry<BoxMove, Deque<Move>> pathEntry : movePaths.entrySet()) {
			BoxMove boxMove = pathEntry.getKey();
			Deque<Move> moves = pathEntry.getValue();
			Box box = boxMove.box;
			Move realMove = boxMove.move.inverse(); // here is the double backwards part
			
			Player playerBeforeBoxMove = new Player(box.getLocation().move(boxMove.move));
			Player movedPlayer = playerBeforeBoxMove.move(realMove);
			Box movedBox = box.move(realMove);
			
			Set<Box> newBoxes = new HashSet<>(boxes);
			newBoxes.remove(box);
			moves.addLast (realMove);
			if (board.isStartOfTunnel(movedBox.getLocation(), realMove)) {
				Location start = movedBox.getLocation();
				List<Move> tunnelPath = start.getLinearPathTo(board.getEndOfTunnel(start, realMove));
				
				boolean boxInTunnel = false;
				Location positionInTunnel = start;
				for(Move tunnelMove : tunnelPath) {
					positionInTunnel = positionInTunnel.move(tunnelMove);
					if(boxes.contains(positionInTunnel)) {
						boxInTunnel = true;
						break;
					}
				}
				
				if(!boxInTunnel) {
					for (Move tunnelMove : tunnelPath) {
						movedPlayer = movedPlayer.move(tunnelMove);
						movedBox = movedBox.move(tunnelMove);
						moves.addLast (tunnelMove);
					}
				}
			}
			newBoxes.add(movedBox);
			ForwardsGameState state = new ForwardsGameState(board, movedPlayer, newBoxes, moves);
			if (!isDeadlockState(state, movedBox)) {
				nextStates.add (state);
			}
			
		}
		return nextStates;
	}
	
	private boolean isDeadlockState (ForwardsGameState state, Box movedBox) {
		Set<Box> visitedBoxes = new HashSet<>();
		return isDeadlockStateRecursive (state, movedBox, visitedBoxes);
	}

	private boolean isDeadlockStateRecursive(ForwardsGameState state, Box box,
			Set<Box> visitedBoxes) {
		visitedBoxes.add(box);
		if (board.isGoal(box.getLocation()))
			return false; // treat goals as non-deadlocks
		if (!state.getPossibleMoves(box).isEmpty())
			return false;
		
		List<Box> adjacentBoxes = getAdjacentBoxes(box, state.getBoxes(), visitedBoxes);
		for (Box adjacentBox : adjacentBoxes)
			if (!isDeadlockStateRecursive(state, adjacentBox, visitedBoxes))
				return false;
		
		return true;
	}
	
	private List<Box> getAdjacentBoxes (Box box, Set<Box> newBoxes, Set<Box> visitedBoxes) {
		List<Box> adjacentBoxes = new ArrayList<>();
		for (Move move : Move.values()) {
			Box adjacent = box.move(move);
			if (newBoxes.contains(adjacent) && !visitedBoxes.contains(adjacent))
				adjacentBoxes.add(adjacent);
		}
		return adjacentBoxes;
	}

	private List<Move> getPossibleMoves (Movable<?> m) {
		List<Move> possibleMoves = new ArrayList<>();
		Location oneUp = m.getLocation().move(Move.UP);
		Location oneRight = m.getLocation().move(Move.RIGHT);
		Location oneDown = m.getLocation().move(Move.DOWN);
		Location oneLeft = m.getLocation().move(Move.LEFT);
		if (isFreeForPlayer(oneUp, oneDown)) {
			if(!board.isDeadlockLocation(oneDown)) {
				possibleMoves.add(Move.UP);
			}
			if(!board.isDeadlockLocation(oneUp)) {
				possibleMoves.add(Move.DOWN);
			}
		}
		if (isFreeForPlayer(oneRight, oneLeft)) {
			if(!board.isDeadlockLocation(oneLeft)) {
				possibleMoves.add(Move.RIGHT);
			}
			if(!board.isDeadlockLocation(oneRight)) {
				possibleMoves.add(Move.LEFT);
			}
		}
		return possibleMoves;
	}
	
	public static ForwardsGameState calculateBoard(List<String> boardStrings) {
		int height = boardStrings.size();
		int width = 0;
		for (String s : boardStrings)
			width = Math.max(width, s.length());
		char[][] board = new char[height][width];
		return fillBoard(board, boardStrings);
	}

	private static ForwardsGameState fillBoard(char[][] board, List<String> boardStrings) {
		/*
		 * Fill the board using the board strings.
		 */
		Player player = new Player(new Location(-1, -1));
		Set<Box> boxes = new HashSet<Box>();
		Set<Goal> goals = new HashSet<Goal>();
		for (int row = 0; row < board.length; row++) {
			String rowString = boardStrings.get(row);
			for (int col = 0; col < board[row].length; col++) {
				char square = ' ';
				if (col < rowString.length())
					square = rowString.charAt(col);
				switch (square) {
				case FREE_SPACE:
				case WALL:
					board[row][col] = square;
					break;
				case PLAYER_ON_GOAL:
					board[row][col] = GOAL;
					goals.add(new Goal(new Location(col, row)));
					player = new Player(new Location(col, row));
					break;
				case PLAYER:
					board[row][col] = FREE_SPACE;
					player = new Player(new Location(col, row));
					break;
				case BOX_ON_GOAL:
					boxes.add(new Box(new Location(col, row)));
				case GOAL:
					board[row][col] = GOAL;
					goals.add(new Goal(new Location(col, row)));
					break;
				case BOX:
					board[row][col] = FREE_SPACE;
					boxes.add(new Box(new Location(col, row)));
					break;
				}
			}
		}
		
		board = fillUnreachableLocationsWithWalls(board, player);
		
		Board gameBoard = new Board(board, goals, GOAL);
		preprocess(gameBoard);
		
		return new ForwardsGameState(gameBoard, player, boxes);
	}
	
	private static char[][] fillUnreachableLocationsWithWalls(char[][] board, Player player) {
		Queue<Location> queue = new LinkedList<>();
		queue.add(player.getLocation());
		Set<Location> visited = new HashSet<>();
		visited.add(player.getLocation());
		
		while (!queue.isEmpty()) {
			Location location = queue.poll();
			
			for (Move move : Move.values()) {
				Location newLocation = location.move(move);
				if (visited.contains(newLocation) || getChar(board, newLocation) == WALL)
					continue;
				visited.add(newLocation);
				queue.add(newLocation);
			}
		}
		
		for(int row = 0; row<board.length; row++) {
			for(int col = 0; col<board[row].length; col++) {
				Location l = new Location(col, row);
				if(!visited.contains(l)) {
					board[row][col] = WALL;
				}
			}
		}
		
		return board;
	}
	
	private static Set<Location> preprocess(Board gameBoard) {
		Set<Location> deadlocks = new HashSet<>();
		Set<Location> entrances = new HashSet<>();
		
		char[][] board = gameBoard.getBoardMatrix();
		
		for(int row = 0; row<board.length; row++) {
			for(int col = 0; col<board[row].length; col++) {
				if(board[row][col] == GOAL || board[row][col] == BOX_ON_GOAL) {
					continue;
				}
				
				Location current = new Location(col, row);
				if(isEntrance(board, current)) {
					entrances.add(current);
				}
				if(isDeadlock(board, current)) {
					deadlocks.add(current);
				}
			}
		}
		
		List<GoalArea> goalAreas = findGoalAreas(gameBoard, entrances);
		gameBoard.setGoalAreas(goalAreas);
		
		findTunnels(entrances, gameBoard, goalAreas);
		
		gameBoard.setDeadlocks(deadlocks);
		
		
		return deadlocks;
	}
	
	private static boolean isDeadlock(char[][] board, Location current) {
		if(board[current.getRow()][current.getCol()] == WALL) {
			return false;
		}
		
		boolean isBlockedUp = isBlocked(board, current.move(Move.UP));
		boolean isBlockedDown = isBlocked(board, current.move(Move.DOWN));
		boolean isBlockedLeft = isBlocked(board, current.move(Move.LEFT));
		boolean isBlockedRight = isBlocked(board, current.move(Move.RIGHT));
		
		if(isBlockedUp || isBlockedDown) {
			return isBlockedLeft || isBlockedRight;
		}
		
		return false;
	}
	
	private static boolean isBlocked(char[][] board, Location loc) {
		int row = loc.getRow();
		int col = loc.getCol();
		
		if(row < 0 || row >= board.length) {
			return true;
		}
		if(col < 0 || col >= board[row].length) {
			return true;
		}
		
		return board[row][col] == WALL;
	}
	
	private static boolean isEntrance(char[][] board, Location loc) {
		if(getChar(board, loc) == WALL) {
			return false;
		}
		
		boolean upBlocked = isBlocked(board, loc.move(Move.UP));
		boolean downBlocked = isBlocked(board, loc.move(Move.DOWN));
		boolean leftBlocked = isBlocked(board, loc.move(Move.LEFT));
		boolean rightBlocked = isBlocked(board, loc.move(Move.RIGHT));
		
		boolean rowDirectionEntrace = upBlocked && downBlocked;
		boolean colDirectionEntrace = leftBlocked && rightBlocked;
		
		return rowDirectionEntrace != colDirectionEntrace;
	}
	
	private static void findTunnels(Set<Location> entrances, Board board, List<GoalArea> goalAreas) {
		TunnelFinder tf = new TunnelFinder(entrances, goalAreas);
		tf.addTunnels(board);
	}
	
	private static char getChar(char[][] board, Location loc) {
		return board[loc.getRow()][loc.getCol()];
	}
	
	private static List<GoalArea> findGoalAreas(Board board, Set<Location> entrances) {
		List<GoalArea> goalAreas = new ArrayList<>();
		for(Location entrance : entrances) {
			GoalArea goalArea = findGoalArea(entrance, board);
			if(goalArea == null) {
				continue;
			}
			
			boolean subsetOfOtherArea = false;
			Iterator<GoalArea> iterator = goalAreas.iterator();
			while(iterator.hasNext()) {
				GoalArea otherArea = iterator.next();
				if(otherArea.squaresInArea.contains(entrance)) {
					subsetOfOtherArea = true;
					break;
				}
				
				if(goalArea.squaresInArea.contains(otherArea.entrance)) {
					iterator.remove();
				}
			}
			
			if(subsetOfOtherArea) {
				continue;
			}
			goalAreas.add(goalArea);
		}
		
		return goalAreas;
	}
	
	private static GoalArea findGoalArea(Location entrance, Board board) {
		for(Move move : Move.values()) {
			GoalArea goalArea = findGoalAreaBFS(entrance, move, board, board.getGoals());
			if(goalArea != null) {
				return goalArea;
			}
		}
		
		return null;
	}
	
	private static GoalArea findGoalAreaBFS(Location entrance, Move direction, Board board, Set<Goal> goals) {
		char[][] boardMatrix = board.getBoardMatrix();
		Queue<Location> queue = new LinkedList<>();
		Set<Location> visited = new HashSet<>();
		
		Location start = entrance.move(direction);
		visited.add(start);
		for(Move move : Move.values()) {
			if(move == direction.inverse()) {
				continue;
			}
			
			Location newLocation = start.move(move);
			if(!isBlocked(boardMatrix, newLocation)) {
				queue.add(newLocation);
				visited.add(newLocation);
			}
		}

		Set<Goal> goalAreaGoals = new HashSet<>();
		while (!queue.isEmpty()) {
			Location location = queue.poll();
			Goal goal = new Goal(location);
			
			if(goals.contains(goal)) {
				goalAreaGoals.add(goal);
			}
			
			for (Move move : Move.values()) {
				Location newLocation = location.move(move);
				if(newLocation.equals(entrance)) {
					return null;
				}
				
				if (visited.contains(newLocation) || getChar(boardMatrix, newLocation) == WALL)
					continue;
				
				visited.add(newLocation);
				if(visited.size() > MAX_GOAL_AREA_SIZE) {
					return null;
				}
				
				queue.add(newLocation);
			}
		}
		if(goalAreaGoals.size() <= 1) {
			return null;
		}
		visited.add(entrance);
		
		return new GoalArea(board, entrance.move(direction.inverse()), entrance, goalAreaGoals, visited);
	}
	
	private static final class TunnelFinder {
		Set<Location> entrances;
		Set<Location> visited = new HashSet<>();
		List<GoalArea> goalAreas;
		
		private TunnelFinder(Set<Location> entrances, List<GoalArea> goalAreas) {
			this.entrances = entrances;
			this.goalAreas = goalAreas;
		}
		
		private Board addTunnels(Board board) {
			for(Location location : entrances) {
				if(visited.contains(location)) {
					continue;
				}
				
				boolean inGoalArea = false;
				for(GoalArea goalArea : goalAreas) {
					if(goalArea.squaresInArea.contains(location)) {
						inGoalArea = true;
						break;
					}
				}
				if(inGoalArea) {
					continue;
				}
				
				Location up = location.move(Move.UP);
				Location down = location.move(Move.DOWN);
				Location left = location.move(Move.LEFT);
				Location right = location.move(Move.RIGHT);
				
				if(entrances.contains(up) || entrances.contains(down)) {
					addTunnel(location, Move.UP, board);
				} else if(entrances.contains(right) || entrances.contains(left)) {
					addTunnel(location, Move.RIGHT, board);
				}
			}
			
			return board;
		}
		
		void addTunnel(Location current, Move move, Board board) {
			Move inverseMove = move.inverse();
			
			Location start = moveTillEnd(current, move);
			Location end = moveTillEnd(current, inverseMove);
			
			board.addTunnel(start, end, move.inverse());
			board.addTunnel(end, start, inverseMove.inverse());
		}
		
		Location moveTillEnd(Location start, Move direction) {
			visited.add(start);
			while(entrances.contains(start.move(direction))) {
				start = start.move(direction);
				visited.add(start);
			}
			return start;
		}
	}
	
	@Override public String toString() {
		char[][] matrix = board.getBoardMatrix ();
		for (Box box : boxes) {
			Location l = box.getLocation();
			char c = board.getCharForLocation(l);
			if (c == GOAL)
				matrix[l.getRow()][l.getCol()] = BOX_ON_GOAL;
			else
				matrix[l.getRow()][l.getCol()] = BOX;
		}
		if (player != null) {
			Location pl = player.getLocation();
			char c = board.getCharForLocation(pl);
			if (c == GOAL)
				matrix[pl.getRow()][pl.getCol()] = PLAYER_ON_GOAL;
			else
				matrix[pl.getRow()][pl.getCol()] = PLAYER;
		}
		
		StringBuilder sb = new StringBuilder();
		for (int row = 0; row < matrix.length; row++) {
			for (int col = 0; col < matrix[row].length; col++) {
				sb.append(matrix[row][col]);
			}
			sb.append('\n');
		}
		return sb.toString();
	}
	
	@Override public List<GameState> tryGoalMacro() {
		//Try to find a box that can be reached by the player and is located on a goal area entrance
		//If there is such a box, make the goal macro and check which state
		if (board.getGoalAreas() != null) {
			for (GoalArea goalArea : board.getGoalAreas()) {
				for (Box box : boxes) {
					if (box.getLocation().equals(goalArea.getEntrance())) {
						GameState dummy = getPlayerMoveGameState(goalArea.getInitialPlayerLocation());
						if (dummy != null) {
							Deque<Move> pathToEntrance = dummy.getMovesToHere();
							if (pathToEntrance != null)
								return goalArea.performMacro(this, box, pathToEntrance);
						}
					}
				}
			}
		}
		return null;
	}
	
	public static final class GoalArea {
		private Board board;
		private Set<Location> squaresInArea;
		private Location initialPlayerLocation;
		private Location entrance;
		private Set<Goal> goals;
		private List<Solution> solutionsToGoals;
		private List<List<Player>> playerPositionsInSolutions;
		private List<List<Box>> boxPositionsInSolutions;
		private int freeGoalsLeft;
		
		public GoalArea(Board board, Location playerLocation, Location entrance, 
				Set<Goal> goals, Set<Location> squaresInArea) {
			this.board = board;
			this.initialPlayerLocation = playerLocation;
			this.entrance = entrance;
			this.goals = goals;
			this.squaresInArea = squaresInArea;
			freeGoalsLeft = goals.size();
			playerPositionsInSolutions = new ArrayList<>();
			boxPositionsInSolutions = new ArrayList<>();
			calculateSolutions();
		}
		
		private void calculateSolutions() {
			solutionsToGoals = findSolutionToGoals(goals);
			for (Solution s : solutionsToGoals) {
				if (s != null) {
					List<Player> playerList = new ArrayList<>();
					List<Box> boxList = new ArrayList<>();
					Player player = new Player(initialPlayerLocation);
					Box newBox = new Box(entrance);
//					System.out.println(s);
					for (Deque<Move> path : s.getPath()) {
						for (Move m : path) {
							player = player.move(m);
						}
						playerList.add(player);
						if (!path.isEmpty())
							newBox = newBox.move(path.getLast());
						boxList.add(newBox);
					}
					playerPositionsInSolutions.add(playerList);
					boxPositionsInSolutions.add(boxList);
				}
			}
		}
		
		private List<Solution> findSolutionToGoals(Set<Goal> goalsLeft) {
			List<Solution> solutions = new ArrayList<>();
			for (Goal goal : goalsLeft) {
				Solution solutionToThisGoal = findGoalMacroSolution(goalsLeft, goal, entrance);
				if (solutionToThisGoal != null) {
					if (goalsLeft.size() == 1) {
						solutions.add(solutionToThisGoal);
						return solutions;
					}
					Set<Goal> restOfGoals = new HashSet<>(goalsLeft);
					restOfGoals.remove(goal);
					List<Solution> solutionsForRestOfGoals = findSolutionToGoals(restOfGoals);
					if (solutionsForRestOfGoals != null) {
						solutions.add(solutionToThisGoal);
						solutions.addAll(solutionsForRestOfGoals);
						return solutions;
					}
				}
			}
			return null;
		}

		private Solution findGoalMacroSolution(Set<Goal> goalsLeft, Goal goal, Location entrance) {
			char[][] dummyBoardMatrix = board.getBoardMatrix();
			for (Goal g : goals) {
				char type = GOAL;
				if (!g.equals(goal)) {
					type = FREE_SPACE;
					if (goalsLeft.contains(g))
						type = WALL;
				}
				dummyBoardMatrix[g.getLocation().getRow()][g.getLocation().getCol()] = type;
			}
//			for (char[] row : dummyBoardMatrix) {
//				StringBuilder sb = new StringBuilder();
//				for (char c : row) {
//					sb.append(c);
//				}
//				System.out.println(sb.toString());
//			}
			Set<Goal> thisGoal = new HashSet<>();
			thisGoal.add(goal);
			Board dummyBoard = new Board(dummyBoardMatrix, thisGoal, GOAL);
			Player dummyPlayer = new Player(initialPlayerLocation);
			Set<Box> dummyBox = new HashSet<>();
			dummyBox.add(new Box( new Location(entrance.getCol(), entrance.getRow())));
			GameState dummyGameState = new ForwardsGameState(dummyBoard, dummyPlayer, dummyBox);
			AStarAlgorithm pathFinder = new AStarAlgorithm(dummyGameState);
			while(pathFinder.nextStep());
			return pathFinder.getSolution();
		}
		
		public Location getInitialPlayerLocation() {
			return initialPlayerLocation;
		}

		public Location getEntrance() {
			return entrance;
		}
		
		public List<GameState> performMacro(GameState current, Box boxToMove, 
				Deque<Move> movesToInitialPosition) {
//			System.out.println("Call to perform");
//			int freeGoalsLeft = getFreeGoalsLeft(current);
			if (freeGoalsLeft > 0) {
				freeGoalsLeft--;
				List<GameState> path = new ArrayList<>();
				Solution solution = solutionsToGoals.get(freeGoalsLeft);
				Deque<Deque<Move>> solutionPath = solution.getPath();
//				System.out.println("Performing goal macro: " + solution.toString());
				int i = 0;
				Box newBox = boxToMove;
				for (Deque<Move> movesToHere : solutionPath) {
					if (i > 0) {
						if (i == 1) {
							movesToInitialPosition.addAll(movesToHere);
							movesToHere = movesToInitialPosition;
						}
						Player player = playerPositionsInSolutions.get(freeGoalsLeft).get(i);
						Set<Box> newBoxes = new HashSet<>(current.getBoxes());
						newBoxes.remove(boxToMove);
						newBox = boxPositionsInSolutions.get(freeGoalsLeft).get(i);
						newBoxes.add(newBox);
						path.add(new ForwardsGameState(board, player, newBoxes, movesToHere));
					}
					i++;
				}
				finishedBoxes.add(newBox);//TODO Does this work? Maybe non-static and copied from parent gamestate?
				return path;
			}
			return null;
		}
		
		private int getFreeGoalsLeft(GameState current) {
			//TODO //If non-static finished boxes -> goals.size - finished.size
			return 0;
		}
	}
}
