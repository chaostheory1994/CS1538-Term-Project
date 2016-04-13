/*
 * This defines the "Machines" of the system.
 */

public interface Machine {
	public PowerType getPowerType();
	public double getPowerUsage(float t);
	public boolean insertBlock(Block b);
	public Block getNextOutput();
	public int getInputSize();
	public int getOutputSize();
	public double getWorkTime();
	public boolean isWorking();
}
