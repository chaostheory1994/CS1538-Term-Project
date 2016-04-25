/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jmsch
 */
public class SteamDynamo implements Generator{

    @Override
    public PowerType getPowerType() {
        return PowerType.RF;
    }

    @Override
    public double getPowerGen() {
        return 1600.0;
    }

    @Override
    public double getGenTime() {
        return 30.0;
    }
    
}
