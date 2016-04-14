
import java.util.ArrayList;

public class Path {

    int id;
    ArrayList<Machine> machines;
    ArrayList<Block> output;
    BlockType[] allowed;
    /*
     * Constructor
     */

    public Path(int id, BlockType[] t) {
        this.id = id;
        machines = new ArrayList<>();
        output = new ArrayList<>();
        allowed = t;
    }

    /*
     * Allow the main program to tell the path of a current event.
     * Also allows for the return of a new event that must be added to the Queue
     */
    public Event[] passEvent(Event e) {
        // A machine has finished.
        ArrayList<Event> ret = new ArrayList<>();
        int index = (int) e.param2;
        while (machines.get(index).getOutputSize() != 0) {
            if (machines.size() == (index + 1)) {
                // The current item has finished going through the system.
                // Add to output
                output.add(machines.get(index).getNextOutput());
            } else {
                // There is another machine in the path. Move the block along.
                // IS that next machine already working?
                machines.get(index + 1).insertBlock(machines.get(index).getNextOutput());
            }
        }
        Event temp;
        if (machines.size() > index + 1) {
            // Ask the next machine if anything new is happening?
            temp = machines.get(index + 1).proc(e.t);

            if (temp != null) {
                temp.param1 = getID();
                temp.param2 = index + 1;
                ret.add(temp);
            }
        }
        // Ask the current machine if anything new is happening?
        temp = machines.get(index).proc(e.t);

        if (temp != null) {
            temp.param1 = getID();
            temp.param2 = index;
            ret.add(temp);
        }

        // Return the events.
        return ret.toArray(new Event[ret.size()]);
    }

    /*
     * Adds a machine to the path.
     */
    public void addMachine(Machine m) {
        machines.add(m);
    }

    /*
     * Allows the main program to get the id of this current path.
     * Used mainly to keep track of events and assertions.
     */
    public int getID() {
        return id;
    }

    /*
     * Allows the main program to insert a block into this path.
     */
    public Event insertBlock(Block b, double t) {
        machines.get(0).insertBlock(b);
        Event ret = machines.get(0).proc(t);
        if (ret != null) {
            ret.param1 = getID();
            ret.param2 = 0;
        }
        return ret;
    }

    /*
     * Returns the size of the input of the first machine
     */
    public int getInputSize() {
        return machines.get(0).getInputSize();
    }

    /*
     * Allows the main program to get the size of the output.
     */
    public int getOutputSize() {
        return output.size();
    }

    /*
     * Allows the main program to get the next item waiting to be outputed.
     */
    public Block getNextOutput() {
        return output.get(0);
    }

    /*
     * This function is needed for seeing if the current state of the simulation is in its
     * finished state or if it is still doing things of interest.
     * This function will prevent the simulation from running endlessly.
     * It will check if any machine is running in its domain.
     */
    public boolean isWorking() {
        for (Machine m : machines) {
            if (m.isWorking()) {
                return true;
            }
        }
        return false;
    }

    /*
     * This method will return the amount of power needed by all the machines.
     * It will only count machines that currently need power.
     */
    public double getRFPower() {
        double totalPower = 0.0;
        for (Machine m : machines) {
            if (m.isWorking() && m.getPowerType() == PowerType.RF) {
                totalPower += m.getPowerUsage();
            }
        }
        return totalPower;
    }

    public double getEUPower() {
        double totalPower = 0.0;
        for (Machine m : machines) {
            if (m.isWorking() && m.getPowerType() == PowerType.EU) {
                totalPower += m.getPowerUsage();
            }
        }
        return totalPower;
    }

    /*
     * Returns true if this path will allow this type of block in.
     */
    public boolean isBlockAllowed(BlockType b) {
        for (int i = 0; i < allowed.length; i++) {
            if (allowed[i] == b) {
                return true;
            }
        }
        return false;
    }
}
