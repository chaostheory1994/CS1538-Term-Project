/*
 * This defines the "Machines" of the system.
 */

public abstract class Machine extends Processor {

    public Machine() {
        super();
    }

    public abstract PowerType getPowerType();

    public abstract double getPowerUsage();

    public abstract double getWorkTime();

    public abstract boolean isWorking();

    public abstract Event proc(double time);
}
