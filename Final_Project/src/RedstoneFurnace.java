/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jmsch
 */
public class RedstoneFurnace extends Machine{
    
    public RedstoneFurnace(){
        super();
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.RF;
    }

    @Override
    public double getPowerUsage() {
        return 400.0; // 400 rf/s
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
        // If there is an input, then we must set it up to start working
        if (this.getInputSize() != 0 && this.getOutputSize() == 0) {
            // There are items waiting to be worked on.
            Block b = this.removeInput();
            // Process it.
            b.state = BlockState.BAR;
            // Move to output
            this.insertOutput(b);
            
            return new Event(EventType.MACH_FINISHED, time + (this.getWorkTime() * b.amount));
        }
        else return null;
    }
    
}
