package mouse.units.mousedeliverydrone;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import mouse.base.MobileUnit;

import static mouse.utility.ActionHelper.tryMove;

public class MouseDeliveryDrone extends MobileUnit {
    boolean hasHelped;
    boolean hasMoved1;
    boolean hasMoved2;
    boolean hasDropped;
    public MouseDeliveryDrone(RobotController rc){
        super(rc);
        hasHelped = false;
        hasMoved1 = false;
        hasMoved2 = false;
        hasDropped = false;
    }

    public void turn() throws GameActionException {
        if(!hasHelped){
            RobotInfo robot = rc.senseRobotAtLocation(rc.getLocation().add(Direction.EAST));
            if(robot == null){
                return;
            } else {
                if(rc.isReady() && rc.canPickUpUnit(robot.ID)){
                    rc.pickUpUnit(robot.ID);
                    hasHelped = true;
                } else {
                    return;
                }
            }
        } else if(!hasMoved1){
            hasMoved1 = tryMove(Direction.SOUTH, rc);
        } else if(!hasMoved2){
            hasMoved2 = tryMove(Direction.SOUTH, rc);
        } else if(!hasDropped){
            if(rc.isReady() && rc.canDropUnit(Direction.NORTH)){
                rc.dropUnit(Direction.NORTH);
                hasDropped = true;
            }
            return;
        } else {
            tryMove(Direction.EAST, rc);
        }
    }
}
