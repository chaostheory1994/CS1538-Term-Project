/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jmsch
 */
public class InductionFurnace extends Machine{
    Block secBlock;
    public InductionFurnace(){
        super();
        secBlock = null;
    }
    @Override
    public PowerType getPowerType() {
        return PowerType.EU;
    }

    @Override
    public double getPowerUsage() {
        // This machine is interesting. It always toake power to keep running,
        // But when something is being smelted, it takes more energy.
        if(isWorking()) return 320.0; // 400 rf/s
        else return 20.0;
    }

    @Override
    public double getWorkTime() {
        return 4.0;
    }

    @Override
    public boolean isWorking() {
        return this.getOutputSize() != 0;
    }

    @Override
    public Event proc(double time) {
        // The induction smelter will smelt 2 blocks at a time. So we have to update both of these.
        // If we have an empty output and an empty 2nd block, we 
        if(getInputSize() > 0 && getOutputSize() == 0 && secBlock == null){
            // The way the program works, it is impossible for more than one input to be there when the other
            // 2 spots for blocks to go are empty. So we just need to move the input into output with proccessing.
            Block b = this.removeInput();
            
            b.state = BlockState.BAR;
            
            this.insertOutput(b);
            
            return new Event(EventType.MACH_FINISHED, time + (this.getWorkTime() * b.amount));
        }
        else if(getInputSize() > 0 && getOutputSize() != 0 && secBlock == null){
            // This means there is input, but the output slot is filled.
            // move input into block slot.
            secBlock = this.removeInput();
            
            secBlock.state = BlockState.BAR;
            
            return new Event(EventType.MACH_FINISHED, time + (this.getWorkTime() * secBlock.amount));
        }
        else if(getOutputSize() == 0 && secBlock != null){
            // This means the output was taken but secBlock still has something stored.
            // We just need to move it over.
            this.insertOutput(secBlock);
            // Now we must check if there is an input to fill secBlock.
            if(this.getInputSize() > 0){
                secBlock = this.removeInput();
                secBlock.state = BlockState.BAR;
                return new Event(EventType.MACH_FINISHED, time + (this.getWorkTime() * secBlock.amount));
            }
            else{
                secBlock = null;
                return null;
            }
        }
        else return null;
    }
}
