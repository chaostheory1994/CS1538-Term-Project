/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jmsch
 */
public class StirlingEngine implements Generator{

    @Override
    public PowerType getPowerType() {
        return PowerType.RF;
    }

    @Override
    public double getPowerGen() {
        return 200.0; // 200 rf/s
    }

    @Override
    public double getGenTime() {
        return 80.0; //runs for 80 seconds
    }
    
}
