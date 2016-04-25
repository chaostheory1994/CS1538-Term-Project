/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jmsch
 */
public class Pulverizer extends Machine {
    public Pulverizer() {
        super();
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.RF;
    }

    @Override
    public double getPowerUsage() {
        return 800.0;
    }

    @Override
    public double getWorkTime() {
        return 5.0; // 5 seconds to run
    }

    @Override
    public boolean isWorking() {
        return this.getOutputSize() != 0;
    }

    @Override
    public Event proc(double time) {
        // If there is an input, then we must set it up to start working
        if (this.getInputSize() != 0 && this.getOutputSize() == 0) {
            // This machine can only accept ore. If it gets something other than that.
            // Just move to output.
            // There are items waiting to be worked on.
            Block b = this.removeInput();
            if (b.state != BlockState.ORE) {
                this.insertOutput(b);

                return new Event(EventType.MACH_FINISHED, time);
            } else {
                // This means its something we can process.
                // Lets do so.
                // We can assume 1 ore block per block structure.
                // Process it.
                b.state = BlockState.PULVERIZED;
                // Mulitply ore.
                b.amount *= 2;
                // Move to output
                this.insertOutput(b);
                // Did the ore generate extra ore?
                switch(b.type){
                    case IRON:
                        if(this.genBernoulli(0.1)){
                            b = new Block(BlockType.TIN);
                            b.state = BlockState.PULVERIZED;
                            b.amount = 1;
                            this.insertOutput(b);
                        }
                        break;
                    case TIN:
                        if(this.genBernoulli(0.1)){
                            b = new Block(BlockType.IRON);
                            b.state = BlockState.PULVERIZED;
                            b.amount = 1;
                            this.insertOutput(b);
                        }
                        break;
                    case SILVER:
                        if(this.genBernoulli(0.1)){
                            b = new Block(BlockType.SILVER);
                            b.state = BlockState.PULVERIZED;
                            b.amount = 1;
                            this.insertOutput(b);
                        }
                        break;
                    default:
                        break;
                }

                return new Event(EventType.MACH_FINISHED, time + this.getWorkTime());

            }

        } else {
            return null;
        }
    }
}
