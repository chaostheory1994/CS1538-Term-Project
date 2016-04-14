
/*
 * Main Program
 * Does all the simulation stuff.
 * 
 */
// Imports
import java.awt.BorderLayout;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Random;
import java.io.*;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Main {

    PriorityQueue<Event> pq;
    double runtime; // Runtime of the simulation. In seconds.
    final double REFRESH_TIME = 10.0; // Refresh displayed stats every 10
    // seconds of events.
    final double PING_TIME = 1; // Will ping all the machine needs in 1 second
    // intervals.
    final double QUARRY_POWER_USAGE = 446.0;
    final double QUARRY_MINE_SPEED = 5.2;
    final double[] BLOCK_RATE = {0.4, 0.5, 0.6, 0.6, 0.6};
    final int LAYER_SIZE = 9;
    boolean quarryRunning;
    int generatorsRunningEU, generatorsRunningRF;
    ArrayList<Path> paths;
    Generator mainRF;
    Generator mainEU;
    int extraCoal, coalReserve, coalUsed;
    Random rnd;
    JFrame frame;
    JLabel label;

    public static void main(String[] args) {
        // Create and Start new simulation.
        // Creates a quarry of size 64x64.
        // The quarry will dig from lvl 50 to lvl 9.
        new Main(64, 64, "C:\\Users\\jmsch\\Documents\\NetBeansProjects\\CS1538-Term-Project\\CS1538-Term-Project\\src\\paths.txt").start();
    }

    /* 
     * Generates geometric distribution 
     */
    public int genGeometric(double p) {
        int ret = 0;
        while (rnd.nextDouble() > p) {
            ret++;
        }
        return ret;
    }

    /*
     * Initialize simulation. Setup initial variables
     */
    public Main(int sizeX, int sizeZ, String pathFile) {
        runtime = 0; // Initialize runtime;
        // We have used 0 extra coal and have 0 coal in reserve.
        extraCoal = 0;
        coalReserve = 0;
        coalUsed = 0;
        // Only 1 EU generator
        mainEU = new EUGenerator();
        rnd = new Random();
        // Create new Event Queue
        pq = new PriorityQueue<>();
        // Generate the blocks that we will be getting from teh quarry ahead of
        // time.
        // Adds those events to the priority queue.
        GenBlocks(sizeX, sizeZ);

        // Generates events for other things that we will predicts now.
        GenEvents();

        // Initialize path structures
        paths = new ArrayList<>();
        ArrayList<BlockType> filter = new ArrayList<>();
        ArrayList<Machine> machines = new ArrayList<>();
        String pathString;
        int id = 0;
        try {
            BufferedReader inputStream = new BufferedReader(new FileReader(pathFile));

            while ((pathString = inputStream.readLine()) != null) {
                // Clear our lists
                filter.clear();
                machines.clear();
                // A new path on a new line.
                char temp[] = pathString.toCharArray();
                if (temp[0] == 'p') {
                    for (int i = 1; i < temp.length; i++) {
                        switch (temp[i]) {
                            // Did we add a filter to the path?
                            case 'i':
                                filter.add(BlockType.IRON);
                                break;
                            case 't':
                                filter.add(BlockType.TIN);
                                break;
                            case 'g':
                                filter.add(BlockType.GOLD);
                                break;
                            case 's':
                                filter.add(BlockType.SILVER);
                                break;
                            // Did we add a furnace?
                            case 'f':
                                // Which furnace did we add?
                                i++;
                                if (temp[i] == '1') {
                                    machines.add(new AlloySmelter());
                                } else if (temp[i] == '2') {

                                } else if (temp[i] == '3') {

                                } else if (temp[i] == '4') {

                                }
                                break;
                            // Was our next input a machine?
                            case 'm':
                                // Which machine did we add?
                                i++;
                                if (temp[i] == '1') {

                                } else if (temp[i] == '2') {

                                } else if (temp[i] == '3') {

                                } else if (temp[i] == '4') {

                                } else if (temp[i] == '5') {

                                }
                                break;
                            default:
                                System.out.println("Unknown option in input file: " + temp[i]);
                                break;
                        }
                    }

                    // With the line read through, setup the new path.
                    paths.add(new Path(id, filter.toArray(new BlockType[filter.size()])));
                    // Add machines into the path.
                    for (Machine m : machines) {
                        paths.get(id).addMachine(m);
                    }
                    // With this path setup. iterate id.
                    id++;
                } else if (temp[0] == 'g') {
                    // This creates a generator to add to the simulation.
                    if (temp[1] == '1') {
                        mainRF = new Stirling();
                    } else if (temp[1] == '2') {

                    } else if (temp[1] == '3') {

                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Could not open file");
            System.out.println(e.toString());
            System.exit(0);
        }
        // Setup output window
        frame = new JFrame("Simulation Output");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        label = new JLabel();
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setSize(300, 200);
        frame.setVisible(true);
    }

    /*
     * Generates the incoming blocks throughout the simulation.
     * 
     */
    private void GenBlocks(int x, int z) {
        // Loop through the area given by parameters.
        // y will start at 50.
        // it will end at 5.
        for (int currY = 50; currY > 5; currY--) {
            for (int currZ = 1; currZ <= z; currZ++) {
                for (int currX = 1; currX <= x; currX++) {
                    // Temporary generation rates.
                    if (4 - ((currY - 6) / LAYER_SIZE) == -1) {
                        System.out.println();
                    }
                    int genNextBlockLoc = genGeometric(BLOCK_RATE[4 - ((currY - 6) / LAYER_SIZE)]);
                    // Update forloop values.
                    currX += genNextBlockLoc;
                    if (currX > x) {
                        currX -= x;
                        currZ++;
                    }
                    if (currZ > z) {
                        currZ -= z;
                        currY--;
                    }
                    if (currY <= 5) {
                        // This means we went though all the blocks, lets quit the loop.
                        currZ = z;
                        currX = x;
                        break;
                    }
                    // This means that the block we generated is on the correct
                    // Create a new event.
                    pq.add(new Event(EventType.BLOCK_ARRIVED, (double) (currX + ((currZ - 1) * x) + ((50 - currY) * x * z)) / QUARRY_MINE_SPEED, (50 - currY) / LAYER_SIZE));
                }
            }
        }

        // Inform the simulation that the quarry has finished.
        pq.add(new Event(EventType.QUARRY_END, x * z * (50 - 4) * QUARRY_MINE_SPEED));
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
        double genRF = generatorsRunningRF * mainRF.getPowerGen();
        double genEU = generatorsRunningEU * mainEU.getPowerGen();

        // Are we generating enough energy? (RF)
        while (totalRF > genRF) {
            generatorsRunningRF++;
            genRF = generatorsRunningRF * mainRF.getPowerGen();
            pq.add(new Event(EventType.GEN_FINISHED, t + mainRF.getGenTime(), PowerType.RF));
            // Add to coal
            if (coalReserve > 0) {
                coalReserve--;
            } else {
                extraCoal++;
            }

            coalUsed++;
        }
        // Are we generating enough energy? (EU)
        while (totalEU > genEU) {
            generatorsRunningEU++;
            genEU = generatorsRunningEU * mainEU.getPowerGen();
            // Add to coal
            if (coalReserve > 0) {
                coalReserve--;
            } else {
                extraCoal++;
            }

            coalUsed++;
        }
    }
    /*
     * Displays simulation details on the screen.
     */

    public void dispInfo(double t) {
        StringBuilder output = new StringBuilder();
        output.append("<html>");
        output.append("<label>Current Time: </label>").append(t).append("<br>");
        output.append("<label>Extra Coal Used: </label>").append(extraCoal).append("<br>");
        output.append("</html>");
        label.setText(output.toString());
    }

    /*
     * Begin the simulation
     */
    public void start() {
        // Store the previous t value.
        double preT = 0.0;
        quarryRunning = true;
        while (!pq.isEmpty()) {
            // We will remove the event and then run code based on what has just
            // happened.
            Event e = pq.poll();

            // Possible variables that are needed for teh switch statement
            Event[] ret;
            Event newEvent;
            Path bestChoice;
            boolean isWorking;

            // Which event occured and do appropriate action
            switch (e.type) {
                case GEN_FINISHED:
                    // This event will occur when a generator has finished operating
                    // on its
                    // current piece of coal. We should recalculate if we need that
                    // generator or not.
                    // We may just turn the generator off until next ping.
                    if ((PowerType) e.param1 == PowerType.EU) {
                        generatorsRunningEU--;
                    } else {
                        generatorsRunningRF--;
                    }
                    calcPower(e.t);
                    break;
                case BLOCK_ARRIVED:
                    // This means the quarry dug up a block from the ground and we
                    // should deal with it.
                    // Put the block in its place and move on.
                    bestChoice = null;

                    for (Path p : paths) {
                        if (bestChoice == null) {
                            bestChoice = p;
                        } else {
                            if (bestChoice.isBlockAllowed((BlockType) e.param1) && p.getInputSize() < bestChoice.getInputSize()) {
                                bestChoice = p;
                            }
                        }
                    }

                    // With the best path possible for this block. Add to it.
                    newEvent = null;
                    if (bestChoice != null) {
                        newEvent = bestChoice.insertBlock(Block.genBlock((int) e.param1), e.t);
                    } else {
                        System.out.println("Could not place block " + (BlockType) e.param1);
                    }

                    // Add to event if it exists.
                    if (newEvent != null) {
                        pq.add(newEvent);
                    }
                    break;
                case MACH_FINISHED:
                    // This means a machine somewhere in the system has finished its
                    // process.
                    // Let the path it is contained in know of this and continue the
                    // simulation.
                    ret = paths.get((int) e.param1).passEvent(e);
                    // Add returned events to the queue;
                    for (int i = 0; i < ret.length; i++) {
                        pq.add(ret[i]);
                    }
                    break;
                case PING:
                    // This is an annual check for energy usage and turning on
                    // generators if needed.
                    // Will also need to add the next ping event if needed.
                    isWorking = false;
                    // Calc power for next second.
                    calcPower(e.t);
                    // Create next PING event
                    for (Path p : paths) {
                        if (p.isWorking()) {
                            isWorking = true;
                            break;
                        }
                    }
                    isWorking = isWorking | quarryRunning;
                    if (isWorking) {
                        pq.add(new Event(EventType.PING, e.t + PING_TIME));
                    }
                    break;
                case REFRESH:
                    // This is hear to update the screen's data.
                    // Will also need to add next refresh event if needed.
                    dispInfo(e.t);
                    isWorking = false;
                    // Create next REFRESH event
                    for (Path p : paths) {
                        if (p.isWorking()) {
                            isWorking = true;
                            break;
                        }
                    }
                    isWorking = isWorking | quarryRunning;
                    if (isWorking) {
                        pq.add(new Event(EventType.REFRESH, e.t + REFRESH_TIME));
                    }
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
            preT = e.t;
        }
    }

}
