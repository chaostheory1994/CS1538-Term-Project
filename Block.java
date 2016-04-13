public class Block {
	BlockType type;
	BlockState state;
	double amount;
	double time_entered;
	
	/*
	 * Returns a new block when we have reached an important block in the Quarry.
	 * Passes in the current layer we are in to be able to adjust the chances based on layer.
	 */
	public static Block genBlock(int layer){
		return new Block(BlockType.IRON);// TODO: Get rates to generate blocks based on layer.
	}
	
	/*
	 * This will allow the main program to request the chance a specific block will clump.
	 * It will then continue to make more of the same block based on that chance.
	 * Also affected by layer.
	 */
	public static double getClumpChance(BlockType bt, int layer){
		return 0.5;// TODO: get Clump Chances for each block.
	}
	
	/*
	 * Constructor
	 * Creates block based on block type.
	 */
	public Block(BlockType bt){
		type = bt;
		if(type == BlockType.DIAMOND || type == BlockType.COAL){
			// When you mine coal/diamond, you get 1 diamond/coal.
			// Thus there is no processing needed.
			state = BlockState.BAR;
			amount = 1;
		}
		else if(type == BlockType.REDSTONE){
			// Redstone is a bit different than diamond and coal.
			// Redstone will drop 4-5 redstone when mined.
			// It is also finished when mined.
			state = BlockState.BAR;
			// TODO: get redstone drop rates.
		}
		else{
			// If its any other block then its an ore than needs processed.
			state = BlockState.ORE;
			amount = 1;
		}
	}
	
	/*
	 * Allows the main program to set the time the block entered the system.
	 */
	public void setTimeEntered(double time){
		time_entered = time;
	}
	
}
