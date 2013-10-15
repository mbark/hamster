import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;


public class Main {
	
	private static final ExecutorService executor = Executors.newFixedThreadPool(2);
	
	@SuppressWarnings("unused")
	private static final PathFindingAlgorithm 	BFS = new BfsAlgorithm(),
												A_STAR = new AStarAlgorithm(null, null, null, null);
	
	public static final void main(String[] args) throws IOException {
		List<String> boardStrings = read();
		GameState gs = BackwardsGameState.calculateBoard(boardStrings);
		Solution solution = A_STAR.findPathToGoal(gs);
		System.out.println(solution);
	}
	
	public static final void main2(String... args) throws IOException, InterruptedException {
		List<String> boardStrings = read();
		CountDownLatch latch = new CountDownLatch(2);
		ConcurrentMap<BoxOnlyGameState, GameState> forwardVisited = new ConcurrentHashMap<>();
		ConcurrentMap<BoxOnlyGameState, GameState> backwardVisited = new ConcurrentHashMap<>();
		AtomicReference<BoxOnlyGameState> meetingPoint = new AtomicReference<>();
		final PathFindingAlgorithm forward =
				new AStarAlgorithm(forwardVisited, backwardVisited, latch, meetingPoint);
		final PathFindingAlgorithm backward =
				new AStarAlgorithm(backwardVisited, forwardVisited, latch, meetingPoint);
		final GameState start = ForwardsGameState.calculateBoard(boardStrings);
		final GameState goal = BackwardsGameState.calculateBoard(boardStrings);
		executor.submit(new Runnable() {
			@Override public void run() {
				forward.findPathToGoal(start);
			}
		});
		executor.submit(new Runnable() {
			@Override public void run() {
				backward.findPathToGoal(goal);
			}
		});
		latch.await();
		// TODO get the paths from forward and backward and print it
	}
	
	public static List<String> read() throws IOException {
		List<String> boardStrings  = new ArrayList<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String row;
		while((row = br.readLine()) != null) {
			boardStrings.add(row);
		}
		
		return boardStrings;
	}
}
