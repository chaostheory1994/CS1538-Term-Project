/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jmsch
 */
public class ThermalCentrafuge extends Machine {

    public ThermalCentrafuge() {
        super();
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.EU;
    }

    @Override
    public double getPowerUsage() {
        return 960.0;
    }

    @Override
    public double getWorkTime() {
        return 25.0; // 5 seconds to run
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
            if (b.state != BlockState.CRUSHED && b.state != BlockState.PURIFIED) {
                this.insertOutput(b);

                return new Event(EventType.MACH_FINISHED, time);
            } else {
                // This means its something we can process.
                // Lets do so.
                // We can assume 1 ore block per block structure.
                // Process it.
                b.state = BlockState.DUST;
                double amount = b.amount;
                double multiply = (1.0 / 9.0);

                // Move to output
                this.insertOutput(b);

                Block temp;
                // Generate Extra Stuff
                switch (b.type) {
                    case IRON:
                        temp = new Block(BlockType.GOLD);
                        temp.amount = multiply;
                        temp.state = BlockState.DUST;
                        this.insertOutput(temp);
                        break;
                    case GOLD:
                        temp = new Block(BlockType.SILVER);
                        temp.amount = multiply;
                        temp.state = BlockState.DUST;
                        this.insertOutput(temp);
                        break;
                    case TIN:
                        temp = new Block(BlockType.IRON);
                        temp.amount = multiply;
                        temp.state = BlockState.DUST;
                        this.insertOutput(temp);
                        break;
                    case SILVER:
                        if (b.state != BlockState.CRUSHED) {
                            temp = new Block(BlockType.SILVER);
                            temp.amount = multiply;
                            temp.state = BlockState.DUST;
                            this.insertOutput(temp);
                        }

                        break;
                }

                return new Event(EventType.MACH_FINISHED, time + (this.getWorkTime() * amount));

            }

        } else {
            return null;
        }
    }
}
