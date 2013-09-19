import java.awt.Point;
import java.io.*;
import java.util.*;

public class Sokoban {
	
	private static final char PLAYER = '@';
	private static final String PLAYER_ON_GOAL = "+";
	private static final String NO_PATH = "no path";
	private static final Point ON_GOAL = new Point (-1, -1);

	/**
	 * Solves a Sokoban board without moving any boxes. Fuck yeah!
	 */
	public static void main(String[] args) throws IOException {
		List<String> board = readBoard();
		
		Point player = findPlayer (board);
		if (player.equals(ON_GOAL)) {
			System.out.println();
			return; // print a newline and return
		}
		
		String path = findPath (player, board);
		System.out.println(path);
	}

	private static List<String> readBoard() throws IOException {
		List<String> board = new ArrayList<String>();

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String line;
		while ((line = br.readLine()) != null)
			board.add(line);
			
		return board;
	}

	private static Point findPlayer (List<String> board) {
		for (int rowIndex = 0; rowIndex < board.size(); rowIndex++) {
			String row = board.get(rowIndex);
			if (row.contains(PLAYER_ON_GOAL))
				return ON_GOAL;
			
			int playerColumn = row.indexOf(PLAYER);
			if (playerColumn != -1)
				return new Point(playerColumn, rowIndex);
		}
		return null; // should not happen on proper input
	}

	private static String findPath(Point player, List<String> board) {
		Queue<Point> queue = new LinkedList<>();
		queue.add (player);
		Point[][] paths = findPathsMatrix (board);
		
		Point p = null;
		boolean found = false;
		while (!queue.isEmpty()) {
			p = queue.poll();
			char object = board.get(p.y).charAt(p.x);
			
			if (object == '.') { // we found goal!
				found = true;
				break;
			}
			
			// handle all adjacencies
			handleLocation(p.x, p.y-1, board, paths, p, queue); // UP
			handleLocation(p.x+1, p.y, board, paths, p, queue); // RIGHT
			handleLocation(p.x, p.y+1, board, paths, p, queue); // DOWN
			handleLocation(p.x-1, p.y, board, paths, p, queue); // LEFT
		}
		if (!found)
			return NO_PATH;
		// p is now the goal point
		StringBuilder path = new StringBuilder();
		while (!p.equals(player)) {
			Point from = paths[p.x][p.y];
			path.append (findStep (from, p));
			p = from; // next piece of the path
		}
		return path.reverse().toString();
	}

	private static void handleLocation (int x, int y, List<String> board,
										Point[][] paths, Point from,
										Queue<Point> queue) {
		switch (board.get(y).charAt(x)) {
		case '.': // goal
		case ' ': // free space
			if (paths[x][y] == null) { // this node is not visited
				paths[x][y] = from;
				queue.add (new Point (x, y));
			}
		}
	}
	
	private static char findStep(Point from, Point to) {
		if (from.x == to.x && from.y - to.y == 1) // to is above from 
			return 'U';
		else if (from.x - to.x == -1 && from.y == to.y) // to is right of from
			return 'R';
		else if (from.x == to.x && from.y - to.y == -1) // to is below from 
			return 'D';
		else // to is left of from
			return 'L';
	}
	
	private static Point[][] findPathsMatrix (List<String> board) {
		int height = board.size();
		int width = 0;
		for (String line : board)
			width = Math.max(width, line.length());
		return new Point[width][height];
	}
}