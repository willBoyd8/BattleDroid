package commando.utility;

import battlecode.common.Direction;
import battlecode.common.RobotType;

public class Constants {

    public static Direction[] DIRECTIONS = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};
    static RobotType[] SPAWNED_BY_MINER = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};
    public static int WALL_START_ROUND = 250;
    public static boolean DEBUGGER_ENABLE = false;
    public static int RAID_START_ROUND = 2000;
    public static int LANDSCAPERS_ON_WALL = 15;
    public static int MIN_REFINERY_SPREAD_DISTANCE = 20;
}
