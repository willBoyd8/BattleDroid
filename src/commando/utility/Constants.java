package commando.utility;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotType;

public class Constants {

    public static Direction[] DIRECTIONS = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};
    static RobotType[] SPAWNED_BY_MINER = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};
    public static int WALL_START_ROUND = 250;
    public static boolean DEBUGGER_ENABLE = true;
    public static int RAID_START_ROUND = 2000;
    public static int LANDSCAPERS_ON_WALL = 15;
    public static int MIN_REFINERY_SPREAD_DISTANCE = 100;
    public static int MAP_EDGE_PROXIMITY_THRESHOLD = 8;
    public static int PATROL_RADIUS = 5;
    public static int LATICE_HEIGHT = 11;
    public static int WALL_SAFTEY_BARRIER = 0;
    public static int MESSAGE_KEY = 31415926;
    public static int MAX_SOUP_TO_CARRY = RobotType.MINER.soupLimit;
    public static int MIN_LATICE_BUILDING_ELEVATION = -150;
}
