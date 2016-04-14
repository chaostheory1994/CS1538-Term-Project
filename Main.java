
/*
 * Main Program
 * Does all the simulation stuff.
 * 
 */
// Imports
import java.util.PriorityQueue;
import java.util.ArrayList;

public class Main {
	PriorityQueue<Event> pq;
	double runtime; // Runtime of the simulation. In seconds.
	final double REFRESH_TIME = 10.0; // Refresh displayed stats every 10
										// seconds of events.
	final double PING_TIME = 1; // Will ping all the machine needs in 1 second
								// intervals.
	final double QUARRY_POWER_USAGE = 446.0;
	boolean quarryRunning;
	int generatorsRunningEU, generatorsRunningRF;
	ArrayList<Path> paths;
	Generator mainRF;
	Generator mainEU;

	public static void main(String[] args) {
		// Create and Start new simulation.
		// Creates a quarry of size 64x64.
		// The quarry will dig from lvl 50 to lvl 9.
		new Main(64, 64).start();
	}

	/*
	 * Initialize simulation. Setup initial variables
	 */
	public Main(int sizeX, int sizeZ) {
		runtime = 0; // Initialize runtime;
		// Create new Event Queue
		pq = new PriorityQueue<Event>();
		// Generate the blocks that we will be getting from teh quarry ahead of
		// time.
		// Adds those events to the priority queue.
		GenBlocks(sizeX, sizeZ);

		// Generates events for other things that we will predicts now.
		GenEvents();

		// Initialize path structures
		paths = new ArrayList<Path>();

		// We will generate paths here. Create new path, add machines, profit.

	}

	/*
	 * Generates the incoming blocks throughout the simulation.
	 * 
	 */
	private void GenBlocks(int x, int z) {
		int currX, currY, currZ;
		currX = 0;
		currZ = 0;
		currY = 50; // This was the starting point for layer 1;

		pq.add(new Event(EventType.BLOCK_ARRIVED, 1, BlockType.IRON));
		pq.add(new Event(EventType.BLOCK_ARRIVED, 3, BlockType.SILVER));
		pq.add(new Event(EventType.BLOCK_ARRIVED, 2, BlockType.GOLD));

		// Inform the simulation that the quarry has finished.
		pq.add(new Event(EventType.QUARRY_END, 1000000));
	}

	/*
	 * Generates other predictable events;
	 */
	private void GenEvents() {
		pq.add(new Event(EventType.PING, 0)); // Adds initial machine ping.
		pq.add(new Event(EventType.REFRESH, 0)); // Adds initial screen refresh.
	}

	/*
	 * Calculates the power needed by all the machines and then turns on
	 * generators as needed.
	 */
	private void calcPower(double t) {
		// We will have to go through all the paths and check for their power
		// usage.
		double totalRF = 0.0;
		double totalEU = 0.0;
		for (Path p : paths) {
			totalRF += p.getRFPower();
			totalEU += p.getEUPower();
		}

		// Now that we have our power usages add in quarry.
		totalRF += QUARRY_POWER_USAGE;

		// Turn on generators to generate enough power.
		// This information will be filled based on decisions.
		double genRF = generatorsRunningRF * mainRF.getPowerGen(t);
		double genEU = generatorsRunningEU * mainEU.getPowerGen(t);

		// Are we generating enough energy? (RF)
		while (totalRF > genRF) {
			generatorsRunningRF++;
			genRF = generatorsRunningRF * mainRF.getPowerGen(t);
			// Add to coal
		}
		// Are we generating enough energy? (EU)
		while (totalEU > genEU) {
			generatorsRunningEU++;
			genEU = generatorsRunningEU * mainEU.getPowerGen(t);
			// Add to coal
		}
	}

	/*
	 * Begin the simulation
	 */
	public void start() {
		while (!pq.isEmpty()) {
			// We will remove the event and then run code based on what has just
			// happened.
			Event e = pq.remove();

			// Which event occured and do appropriate action
			switch (e.type) {
			case GEN_FINISHED:
				// This event will occur when a generator has finished operating
				// on its
				// current piece of coal. We should recalculate if we need that
				// generator or not.
				// We may just turn the generator off until next ping.
				if ((PowerType) e.param2 == PowerType.EU)
					generatorsRunningEU--;
				else
					generatorsRunningRF--;
				calcPower((double) e.param1);
				break;
			case BLOCK_ARRIVED:
				// This means the quarry dug up a block from the ground and we
				// should deal with it.
				// Put the block in its place and move on.
				

				break;
			case MACH_FINISHED:
				// This means a machine somewhere in the system has finished its
				// process.
				// Let the path it is contained in know of this and continue the
				// simulation.
				Event ret = paths.get((int) e.param1).passEvent(e);
				break;
			case PING:
				// This is an annual check for energy usage and turning on
				// generators if needed.
				// Will also need to add the next ping event if needed.
				calcPower((double) e.param1);
				break;
			case REFRESH:
				// This is hear to update the screen's data.
				// Will also need to add next refresh event if needed.
				pq.add(new Event(EventType.REFRESH, e.t));
				break;
			case QUARRY_END:
				// This tells the simulation has finished with quarry stuff and
				// that we should
				// Mark the quarry as running.
				quarryRunning = false;
				break;
			default:
				System.out.println("Something went wrong. An unknown event occured /shrug");
				break;
			}
		}
	}

}
