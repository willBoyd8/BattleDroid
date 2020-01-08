package c3po.utility;

import battlecode.common.Direction;
import battlecode.common.RobotType;

public class Constants {

    public static Direction[] DIRECTIONS = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};
    static RobotType[] SPAWNED_BY_MINER = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};
    public static int WALL_SLOPE_RATE = 3;

}
