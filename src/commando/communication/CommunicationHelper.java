package commando.communication;

import battlecode.common.MapLocation;

public class CommunicationHelper {
    public static int convertLocationToMessage(MapLocation loc){
        return (loc.x * 64) + loc.y;
    }

    public static MapLocation convertMessageToLocation(int message){
        return new MapLocation(message/64, message % 64);
    }
}
