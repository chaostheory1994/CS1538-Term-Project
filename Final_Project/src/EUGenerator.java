/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jmsch
 */
public class EUGenerator implements Generator {

    @Override
    public PowerType getPowerType() {
        return PowerType.EU;
    }

    @Override
    public double getPowerGen() {
        return 200.0;
    }

    @Override
    public double getGenTime() {
        return 20.0;
    }
    
}
