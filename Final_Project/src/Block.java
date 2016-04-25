import java.util.Random;

public class Block {

    BlockType type;
    BlockState state;
    double amount;
    double time_entered;
    static Random rnd = new Random();

    /*
     * Returns a new block when we have reached an important block in the Quarry.
     * Passes in the current layer we are in to be able to adjust the chances based on layer.
     */
    public static Block genBlock(int layer) {
        double hold = rnd.nextDouble();
        switch(layer){
            case 0:
                if(hold<0.437944){
                    return new Block(BlockType.COAL);
                }
                else if(hold<0.635186){
                    return new Block(BlockType.IRON);
                }
                else{
                    return new Block(BlockType.TIN);
                }
            case 1:
                if(hold<0.4405915){
                    return new Block(BlockType.COAL);
                }
                else if(hold<0.6568077){
                    return new Block(BlockType.IRON);
                }
                else{
                    return new Block(BlockType.TIN);
                }
            case 2:
                if(hold<0.3632533){
                    return new Block(BlockType.COAL);
                }
                else if(hold<0.3914876){
                    return new Block(BlockType.GOLD);
                }
                else if(hold<0.5655289){
                    return new Block(BlockType.IRON);
                }
                else if(hold<0.5992415){
                    return new Block(BlockType.LAPIZ);
                }
                else{
                    return new Block(BlockType.TIN);
                }
            case 3:
                if(hold<0.3162119){
                    return new Block(BlockType.COAL);
                }
                else if(hold<0.35353132){
                    return new Block(BlockType.GOLD);
                }
                else if(hold<0.54735152){
                    return new Block(BlockType.IRON);
                }
                else if(hold<0.56139646){
                    return new Block(BlockType.LAPIZ);
                }
                else if(hold<0.97873196){
                    return new Block(BlockType.SILVER);
                }
                else if(hold<0.98154095){
                    return new Block(BlockType.DIAMOND);
                }
                else{
                    return new Block(BlockType.REDSTONE);
                }
            case 4:
                if(hold<0.2513966){
                    return new Block(BlockType.COAL);
                }
                else if(hold<0.27995029){
                    return new Block(BlockType.DIAMOND);
                }
                else if(hold<0.30167593){
                    return new Block(BlockType.GOLD);
                }
                else if(hold<0.45996273){
                    return new Block(BlockType.IRON);
                }
                else if(hold<0.49658595){
                    return new Block(BlockType.LAPIZ);
                }
                else if(hold<0.72004965){
                    return new Block(BlockType.REDSTONE);
                }
                else{
                    return new Block(BlockType.SILVER);
                }
            default:
                return null;
        }
    }

    /*
     * This will allow the main program to request the chance a specific block will clump.
     * It will then continue to make more of the same block based on that chance.
     * Also affected by layer.
     */
    public static double getClumpChance(BlockType bt, int layer) {
        return 0.5; // TODO: get Clump Chances for each block.
    }

    /*
     * Constructor
     * Creates block based on block type.
     */
    public Block(BlockType bt) {
        type = bt;
        if (type == BlockType.DIAMOND || type == BlockType.COAL) {
			// When you mine coal/diamond, you get 1 diamond/coal.
            // Thus there is no processing needed.
            state = BlockState.BAR;
            amount = 1;
        } else if (type == BlockType.REDSTONE) {
			// Redstone is a bit different than diamond and coal.
            // Redstone will drop 4-5 redstone when mined.
            // It is also finished when mined.
            state = BlockState.BAR;
            amount = rnd.nextInt(2) + 4;
        }else if(type == BlockType.LAPIZ){
            state = BlockState.BAR;
            amount = rnd.nextInt(6) + 4;
        } else {
            // If its any other block then its an ore than needs processed.
            state = BlockState.ORE;
            amount = 1;
        }
    }

    /*
     * Allows the main program to set the time the block entered the system.
     */
    public void setTimeEntered(double time) {
        time_entered = time;
    }

}
