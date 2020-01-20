package commando.utility;

import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotType;

public class Constants {

    public static Direction[] DIRECTIONS = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};
    static RobotType[] SPAWNED_BY_MINER = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    //The following integer arrays are for the purpose of checking tiles getting every visible tile using given game mechanics
    //The numbers in the arrays represent the line by line width of the vision area
    //These values were taken with only units that have a base vision radius squared of 24 in mind
    public static int[] SQUARED_RADIUS_20 = {5, 7, 9, 9, 9, 9, 9, 7, 5};    //Valid until greater than 381 pollution Standard Vision Radius
    public static int[] SQUARED_RADIUS_18 = {3, 7, 7, 9, 9, 9, 7, 7, 3};    //Valid until greater than 618 pollution
    public static int[] SQUARED_RADIUS_17 = {3, 5, 7, 9, 9, 9, 7, 5, 3};    //Valid until greater than 752 pollution
    public static int[] SQUARED_RADIUS_16 = {1, 5, 7, 7, 9, 7, 7, 5, 3};    //Valid until greater than 898 pollution
    public static int[] SQUARED_RADIUS_13 =    {5, 7, 7, 7, 7, 7, 5};       //Valid until greater than 1059 pollution
    public static int[] SQUARED_RADIUS_10 =    {3, 5, 7, 7, 7, 5, 3};       //Valid until greater than 2196 pollution
    public static int[] SQUARED_RADIUS_9  =    {1, 5, 5, 7, 5, 5, 1};       //Valid until greater than 2532 pollution
    public static int[] SQUARED_RADIUS_8  =       {5, 5, 5, 5, 5};          //Valid until greater than 2928 pollution
    public static int[] SQUARED_RADIUS_6  =       {3, 5, 5, 5, 3};          //Valid until greater than 4000 pollution Half Standard Vision Radius

    public static int WALL_START_ROUND = 250;
    public static boolean DEBUGGER_ENABLE = true;
    public static int RAID_START_ROUND = 2000;
    public static int LANDSCAPERS_ON_WALL = 15;
    public static int ASSIST_START_ROUND = 1000;

    public static int MIN_REFINERY_SPREAD_DISTANCE = 100;
    public static int MAP_EDGE_PROXIMITY_THRESHOLD = 8;
    public static int PATROL_RADIUS = 5;
    public static int DRONE_BARRIER_THRESHOLD = 8;
    public static int DRONE_LATICE_CHECK_RADIUS = 2;


    public static int LATICE_HEIGHT = 11;
    public static int WALL_SAFETY_BARRIER = 0;
    public static int MESSAGE_KEY = 31415926;
    public static int MAX_SOUP_TO_CARRY = RobotType.MINER.soupLimit;
}
