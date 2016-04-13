import java.util.ArrayList;

public class Path {
	int id;
	ArrayList<Machine> machines;
	/*
	 * Constructor
	 */
	public Path(int id){
		this.id = id;
		machines = new ArrayList<Machine>();
	}
	
	/*
	 * Allow the main program to tell the path of a current event.
	 * Also allows for the return of a new event that must be added to the Queue
	 */
	public Event passEvent(Event e){
		
		return new Event(EventType.GEN_FINISHED, 1.2);
	}
	
	/*
	 * Allows the main program to get the id of this current path.
	 * Used mainly to keep track of events and assertions.
	 */
	public int getID(){ return id; }
	
	/*
	 * Allows the main program to insert a block into this path.
	 */
	public void insertBlock(Block b){
		
	}
	
	/*
	 * Allows the main program to get the size of the output.
	 */
	public int getOutputSize(){
		return 0;
	}
	
	/*
	 * Allows the main program to get the next item waiting to be outputed.
	 */
	public Block getNextOutput(){
		return new Block(BlockType.IRON);
	}
	
	/*
	 * This function is needed for seeing if the current state of the simulation is in its
	 * finished state or if it is still doing things of interest.
	 * This function will prevent the simulation from running endlessly.
	 * It will check if any machine is running in its domain.
	 */
	public boolean isWorking(){
		return true;
	}
	
	/*
	 * This method will return the amount of power needed by all the machines.
	 * It will only count machines that currently need power.
	 */
	public double getRFPower(){
		return 0.0;
	}
	
	public double getEUPower(){
		return 0.0;
	}
}
