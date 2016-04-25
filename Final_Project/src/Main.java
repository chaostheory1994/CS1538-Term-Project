
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
import java.util.EnumMap;

public class Main {

    PriorityQueue<Event> pq;
    double runtime; // Runtime of the simulation. In seconds.
    final double REFRESH_TIME = 10.0; // Refresh displayed stats every 10
    // seconds of events.
    final double PING_TIME = 1; // Will ping all the machine needs in 1 second
    // intervals.
    final double QUARRY_POWER_USAGE = 446.0;
    final double QUARRY_MINE_SPEED = 5.2;
    final double[] BLOCK_RATE = {(0.03245714), (0.02659776), (0.03218587), (0.03379991), (0.02185059)};
    final int LAYER_SIZE = 9;
    int currLayer;
    boolean quarryRunning;
    int generatorsRunningEU, generatorsRunningRF;
    ArrayList<Path> paths;
    Generator mainRF;
    Generator mainEU;
    int extraCoal, coalReserve, coalUsed;
    Random rnd;
    JFrame frame;
    JLabel label;
    EnumMap<BlockType, Double> barOutput;
    EnumMap<BlockType, Double> otherOutput;
    EnumMap<BlockType, Integer> blocksFound;
    double EUGenerated, RFGenerated;
    double extraEU, extraRF;
    static double prePowT = 0;

    public static void main(String[] args) {
        // Create and Start new simulation.
        // Creates a quarry of size 64x64.
        // The quarry will dig from lvl 50 to lvl 9.
        new Main(64, 64, "paths.txt").start();
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
        EUGenerated = 0.0;
        RFGenerated = 0.0;
        extraEU = 0.0;
        extraRF = 0.0;
        // Only 1 EU generator
        mainEU = new EUGenerator();
        rnd = new Random();
        // Create new Event Queue
        pq = new PriorityQueue<>();
        // Track output statistics
        barOutput = new EnumMap<>(BlockType.class);
        otherOutput = new EnumMap<>(BlockType.class);
        blocksFound = new EnumMap<>(BlockType.class);
        for (BlockType tempType : BlockType.values()) {
            barOutput.put(tempType, 0.0);
            otherOutput.put(tempType, 0.0);
            blocksFound.put(tempType, 0);
        }
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
                                    machines.add(new RedstoneFurnace());
                                } else if (temp[i] == '3') {
                                    machines.add(new ElectricFurnace());
                                } else if (temp[i] == '4') {
                                    machines.add(new InductionFurnace());
                                }
                                break;
                            // Was our next input a machine?
                            case 'm':
                                // Which machine did we add?
                                i++;
                                if (temp[i] == '1') {
                                    machines.add(new SAGMill());
                                } else if (temp[i] == '2') {
                                    machines.add(new Pulverizer());
                                } else if (temp[i] == '3') {
                                    machines.add(new Macerator());
                                } else if (temp[i] == '4') {
                                    machines.add(new OreWashing());
                                } else if (temp[i] == '5') {
                                    machines.add(new ThermalCentrafuge());
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
                        mainRF = new StirlingEngine();
                    } else if (temp[1] == '3') {
                        mainRF = new SteamDynamo();
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
        frame.setSize(500, 525);
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
        pq.add(new Event(EventType.QUARRY_END, x * z * (50 - 4) / QUARRY_MINE_SPEED));
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
    private void calcPower(double t, PowerType pt) {
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

        // This is for statistic tracking. This will ensure the stats know that
        // a generator was turned off.
        if (pt != null) {
            if (pt == PowerType.EU) {
                EUGenerated += (generatorsRunningEU + 1) * mainEU.getPowerGen() * (t - prePowT);
                RFGenerated += (generatorsRunningRF) * mainRF.getPowerGen() * (t - prePowT);
                extraRF += (genRF - totalRF) * (t - prePowT);
                extraEU += (genEU + mainEU.getPowerGen() - totalEU) * (t - prePowT);
            } else {
                EUGenerated += (generatorsRunningEU) * mainEU.getPowerGen() * (t - prePowT);
                RFGenerated += (generatorsRunningRF + 1) * mainRF.getPowerGen() * (t - prePowT);
                extraRF += (genRF + mainRF.getPowerGen() - totalRF) * (t - prePowT);
                extraEU += (genEU - totalEU) * (t - prePowT);
            }
        } else {
            EUGenerated += generatorsRunningEU * mainEU.getPowerGen() * (t - prePowT);
            RFGenerated += generatorsRunningRF * mainRF.getPowerGen() * (t - prePowT);
            extraRF += (genRF - totalRF) * (t - prePowT);
            extraEU += (genEU - totalEU) * (t - prePowT);
        }

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

        prePowT = t;
    }
    /*
     * Displays simulation details on the screen.
     */

    public void dispInfo(double t) {
        StringBuilder output = new StringBuilder();
        output.append("<html>");
        output.append("<label>Current Time: </label>").append(t).append("<br>");
        output.append("<label>Quarry Running?: </label>").append(quarryRunning).append("<br>");
        output.append("<label>Current Quarry Layer: </layer>").append(currLayer).append("<br>");
        output.append("<label>EU Generated: </laberl>").append(EUGenerated).append("<br>");
        output.append("<label>RF Generated: </label>").append(RFGenerated).append("<br>");
        output.append("<label>Extra EU Generated: </laberl>").append(extraEU).append(" <label>Avg: </label>").append(extraEU / t).append("<br>");
        output.append("<label>Extra RF Generated: </label>").append(extraRF).append(" <label>Avg: </label>").append(extraRF / t).append("<br>");
        output.append("<label>Extra Coal Used: </label>").append(extraCoal).append("<br>");
        output.append("<label>Reserved Coal: </label>").append(coalReserve).append("<br>");
        output.append("<label>Reserved Coal Used: </labe>").append(coalUsed - extraCoal).append("<br>");
        for (BlockType temp : BlockType.values()) {
            if (temp == BlockType.GOLD || temp == BlockType.IRON || temp == BlockType.SILVER || temp == BlockType.TIN) {
                output.append("<label>").append(temp).append(" Ore Found: </label>").append(blocksFound.get(temp)).append("<br>");
                output.append("<label>").append(temp).append(" Bars Made: </label>").append(barOutput.get(temp)).append("<br>");
                output.append("<label>Unprocessed ").append(temp).append(": </label>").append(otherOutput.get(temp)).append("<br>");
            } else {
                output.append("<label>").append(temp).append(" Ore Found: </label>").append(blocksFound.get(temp)).append("<br>");
                output.append("<label>").append(temp).append(": </label>").append(barOutput.get(temp)).append("<br>");
            }
        }
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
        currLayer = 0;
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
                    calcPower(e.t, (PowerType) e.param1);
                    break;
                case BLOCK_ARRIVED:
                    // This means the quarry dug up a block from the ground and we
                    // should deal with it.
                    // Put the block in its place and move on.
                    bestChoice = null;
                    Block newBlock = Block.genBlock((int) e.param1);
                    currLayer = (int)e.param1;
                    blocksFound.put(newBlock.type, blocksFound.get(newBlock.type) + 1);

                    // Is it a non processable block?
                    if (newBlock.type == BlockType.DIAMOND || newBlock.type == BlockType.LAPIZ || newBlock.type == BlockType.REDSTONE) {
                        barOutput.put(newBlock.type, barOutput.get(newBlock.type) + newBlock.amount);
                    } else if (newBlock.type == BlockType.COAL) {
                        barOutput.put(newBlock.type, barOutput.get(newBlock.type) + newBlock.amount);
                        coalReserve += newBlock.amount;
                    } else {
                        
                        for (Path p : paths) {
                            if (bestChoice == null && p.isBlockAllowed(newBlock.type)) {
                                bestChoice = p;
                            } 
                            else if (bestChoice != null){
                                if (p.isBlockAllowed(newBlock.type) && p.getInputSize() < bestChoice.getInputSize()) {
                                    bestChoice = p;
                                }
                            }
                        }
                        
                        if(bestChoice == null){
                            // We have no path for this ore. just break
                            break;
                        }

                        // With the best path possible for this block. Add to it.
                        newEvent = null;
                        if (bestChoice != null) {
                            newEvent = bestChoice.insertBlock(newBlock, e.t);
                        } else {
                            System.out.println("Could not place block " + (BlockType) e.param1);
                        }

                        // Add to event if it exists.
                        if (newEvent != null) {
                            pq.add(newEvent);
                        }
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
                    // Finally we should do a broad checking of outputs and see if any of the paths have finished materials.
                    for (Path p : paths) {
                        while (p.getOutputSize() > 0) {
                            Block b = p.getNextOutput();
                            if (b.state == BlockState.BAR) {
                                barOutput.put(b.type, barOutput.get(b.type) + b.amount);
                            } else {
                                otherOutput.put(b.type, otherOutput.get(b.type) + b.amount);
                            }
                        }
                    }
                    break;
                case PING:
                    // This is an annual check for energy usage and turning on
                    // generators if needed.
                    // Will also need to add the next ping event if needed.
                    isWorking = false;
                    // Calc power for next second.
                    calcPower(e.t, null);
                    // Update Power Statistics.
                    EUGenerated += generatorsRunningEU * mainEU.getPowerGen();
                    RFGenerated += generatorsRunningRF * mainRF.getPowerGen();
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
