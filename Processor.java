/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jmsch
 */
import java.util.ArrayList;

public class Processor {

    ArrayList<Block> input;
    ArrayList<Block> output;

    public Processor() {
        input = new ArrayList<>();
        output = new ArrayList<>();
    }

    public void insertBlock(Block b) {
        input.add(b);
    }

    public Block getNextOutput() {
        return output.remove(0);
    }

    public int getInputSize() {
        return input.size();
    }

    public int getOutputSize() {
        return output.size();
    }

    protected Block removeInput() {
        return input.remove(0);
    }

    protected void insertOutput(Block b) {
        output.add(b);
    }

}
