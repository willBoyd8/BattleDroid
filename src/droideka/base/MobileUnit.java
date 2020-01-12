package droideka.base;

import battlecode.common.RobotController;

public abstract class MobileUnit extends Unit {
    public MobileUnit(RobotController rc){
        super(rc);
    }

    public MobileUnit(MobileUnit unit){
        super(unit);
    }

}
