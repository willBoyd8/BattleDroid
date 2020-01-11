package droideka.utility;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class DebugHelper {
    public static void setIndicatorDot(MapLocation loc, int red, int green, int blue, RobotController rc){
        if(Constants.DEBUGGER_ENABLE){
            rc.setIndicatorDot(loc, red, green, blue);
        }
    }

    public static void setIndicatorLine(MapLocation startLoc, MapLocation endLoc, int red, int green, int blue, RobotController rc) {
        if(Constants.DEBUGGER_ENABLE) {
            rc.setIndicatorLine(startLoc, endLoc, red, green, blue);
        }
    }
}
