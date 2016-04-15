/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jmsch
 */
public class OreWashing extends Machine{
    public OreWashing() {
        super();
    }

    @Override
    public PowerType getPowerType() {
        return PowerType.EU;
    }

    @Override
    public double getPowerUsage() {
        return 320.0;
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
            if (b.state != BlockState.CRUSHED) {
                this.insertOutput(b);

                return new Event(EventType.MACH_FINISHED, time);
            } else {
                // This means its something we can process.
                // Lets do so.
                // We can assume 1 ore block per block structure.
                // Process it.
                b.state = BlockState.PURIFIED;
                double amount = b.amount;
                // Mulitply ore.
                b.amount *= (1.0 + (2.0/9.0));
                // Move to output
                this.insertOutput(b);

                return new Event(EventType.MACH_FINISHED, time + (this.getWorkTime() * amount));

            }

        } else {
            return null;
        }
    }
}
