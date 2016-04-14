/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jmsch
 */
public class Stirling implements Generator {

    @Override
    public PowerType getPowerType() {
        return PowerType.RF; // Makes RF
    }

    @Override
    public double getPowerGen() {
        return 400.0; // 400 RF/s
    }

    @Override
    public double getGenTime() {
        return 40.0; // 40 Second burn time
    }
    
}
