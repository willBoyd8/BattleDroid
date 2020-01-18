package commando.units.probedroid;

import battlecode.common.*;
import commando.base.MobileUnit;
import commando.communication.CommunicationHelper;
import commando.pathing.Simple;
import commando.units.laticelandscaper.LaticeLandscaper;
import commando.utility.*;


public class ProbeDroid extends MobileUnit {

    DroidList<MapLocation> enemyHQLocations;
    DroneState state;
    DroidList<MapLocation> wallLocations;
    int gridOffsetX, gridOffsetY;
    int homeQuad;
    DroidList<MapLocation> enemyHQBlacklist;
    DroidList<MapLocation> patrolPath;

    public ProbeDroid (RobotController rc) {
        super(rc);
        wallLocations = new DroidList<>();
        enemyHQBlacklist = new DroidList<>();
        patrolPath = new DroidList<>();
        state = DroneState.PATROL;
        homeQuad = 0;
        gridOffsetX = 0;
        gridOffsetY = 0;

    }

    enum DroneState {
        PATROL,
        INTERCEPT,
        HELP,
        DROPOFF,
        RETURNING,
        ROAMING,
    }


    public void onInitialization() throws GameActionException {
        catchup();

        if(hqLocation.x % 2 == 0){
            gridOffsetX = 1;
        }
        if(hqLocation.y % 2 == 0){
            gridOffsetY = 1;
        }
        int radius = Constants.PATROL_RADIUS;
        int neg_radius = Constants.PATROL_RADIUS * -1;
        homeQuad = Unsorted.getHQQuadrant(hqLocation, rc);

        //For the purpose of accounting for the edge cases of us being either close to or up against the map edge

        //case 1: HQ is not close to or against map edge
        //Path: Circle the HQ
        if (((hqLocation.x >= Constants.MAP_EDGE_PROXIMITY_THRESHOLD) || (hqLocation.x <= (rc.getMapWidth() - Constants.MAP_EDGE_PROXIMITY_THRESHOLD))) && ((hqLocation.y >= Constants.MAP_EDGE_PROXIMITY_THRESHOLD) && (hqLocation.y <= (rc.getMapHeight() - Constants.MAP_EDGE_PROXIMITY_THRESHOLD)))) {
            //No quadrant specific paths
            patrolPath.add(hqLocation.translate(radius, radius));
            patrolPath.add(hqLocation.translate(neg_radius, radius));
            patrolPath.add(hqLocation.translate(neg_radius, neg_radius));
            patrolPath.add(hqLocation.translate(radius, neg_radius));
        }

        //case 2: HQ is in a corner
        //Path: Semi-circle on the side of the HQ facing the center of the map
        //Endpoints: Depending on the corner, 1 - North/South; 2 - East/West
        else if (((hqLocation.x <= Constants.MAP_EDGE_PROXIMITY_THRESHOLD) || (hqLocation.x >= (rc.getMapWidth() - Constants.MAP_EDGE_PROXIMITY_THRESHOLD))) && ((hqLocation.y <= Constants.MAP_EDGE_PROXIMITY_THRESHOLD) || (hqLocation.y >= (rc.getMapHeight() - Constants.MAP_EDGE_PROXIMITY_THRESHOLD)))) {
            switch (homeQuad){
                case 1:
                    patrolPath.add(hqLocation.translate(0, neg_radius));
                    patrolPath.add(hqLocation.translate(neg_radius, neg_radius));
                    patrolPath.add(hqLocation.translate(neg_radius, 0));
                    patrolPath.add(hqLocation.translate(neg_radius, neg_radius));
                    break;

                case 2:
                    patrolPath.add(hqLocation.translate(0, neg_radius));
                    patrolPath.add(hqLocation.translate(radius, neg_radius));
                    patrolPath.add(hqLocation.translate(radius, 0));
                    patrolPath.add(hqLocation.translate(radius, radius));
                    break;

                case 3:
                    patrolPath.add(hqLocation.translate(0, radius));
                    patrolPath.add(hqLocation.translate(radius, radius));
                    patrolPath.add(hqLocation.translate(radius, 0));
                    patrolPath.add(hqLocation.translate(radius, radius));
                    break;

                case 4:
                    patrolPath.add(hqLocation.translate(0, neg_radius));
                    patrolPath.add(hqLocation.translate(neg_radius, radius));
                    patrolPath.add(hqLocation.translate(neg_radius, 0));
                    patrolPath.add(hqLocation.translate(neg_radius, radius));
                    break;
            }
        }

        //case 3: HQ is not in a corner but is against the bottom of the map
        //Path: Semi-circle on the side of the HQ facing the center of the map
        //Endpoints: 1 - EAST; 2 - WEST
        else if (((hqLocation.x <= Constants.MAP_EDGE_PROXIMITY_THRESHOLD) || (hqLocation.x >= (rc.getMapWidth() - Constants.MAP_EDGE_PROXIMITY_THRESHOLD))) && ((hqLocation.y >= Constants.MAP_EDGE_PROXIMITY_THRESHOLD) && (hqLocation.y <= (rc.getMapHeight() - Constants.MAP_EDGE_PROXIMITY_THRESHOLD)))) {
            if (hqLocation.y <= Constants.MAP_EDGE_PROXIMITY_THRESHOLD){
                //Bottom of the Map
                patrolPath.add(hqLocation.translate(radius, 0));
                patrolPath.add(hqLocation.translate(radius, radius));
                patrolPath.add(hqLocation.translate(neg_radius, radius));
                patrolPath.add(hqLocation.translate(neg_radius, 0));
                patrolPath.add(hqLocation.translate(neg_radius, radius));
                patrolPath.add(hqLocation.translate(radius, radius));
            }
            else{
                patrolPath.add(hqLocation.translate(neg_radius,0));
                patrolPath.add(hqLocation.translate(neg_radius, neg_radius));
                patrolPath.add(hqLocation.translate(radius, neg_radius));
                patrolPath.add(hqLocation.translate(radius, 0));
                patrolPath.add(hqLocation.translate(radius, neg_radius));
                patrolPath.add(hqLocation.translate(neg_radius, neg_radius));
            }
        }

        //case 4: HQ is not in a corner but is against the side of the map
        //Path: Semi-circle on the side of the HQ facing the center of the map
        //Endpoints: 1 - SOUTH; 2 - NORTH
        else if (((hqLocation.x >= Constants.MAP_EDGE_PROXIMITY_THRESHOLD) && (hqLocation.x <= (rc.getMapWidth() - Constants.MAP_EDGE_PROXIMITY_THRESHOLD))) && ((hqLocation.y <= Constants.MAP_EDGE_PROXIMITY_THRESHOLD) || (hqLocation.y >= (rc.getMapHeight() - Constants.MAP_EDGE_PROXIMITY_THRESHOLD)))) {
            if (hqLocation.x <= Constants.MAP_EDGE_PROXIMITY_THRESHOLD) {
                patrolPath.add(hqLocation.translate(0, neg_radius));
                patrolPath.add(hqLocation.translate(radius, neg_radius));
                patrolPath.add(hqLocation.translate(radius, radius));
                patrolPath.add(hqLocation.translate(0, radius));
                patrolPath.add(hqLocation.translate(radius, radius));
                patrolPath.add(hqLocation.translate(radius, neg_radius));
            }
            else {
                patrolPath.add(hqLocation.translate(0, neg_radius));
                patrolPath.add(hqLocation.translate(neg_radius, neg_radius));
                patrolPath.add(hqLocation.translate(neg_radius, radius));
                patrolPath.add(hqLocation.translate(0, radius));
                patrolPath.add(hqLocation.translate(neg_radius, radius));
                patrolPath.add(hqLocation.translate(neg_radius, neg_radius));
            }
        }

        //case 5: there is some serious issue in the code if it executes this
        else {
            System.out.println("I guess we have either a logic error or a some kind of edge case");
        }


        enemyHQLocations = Unsorted.generatePossibleEnemyHQLocation(hqLocation, rc);
        enemyHQBlacklist.removeAll(enemyHQBlacklist);
    }

    public void turn() throws GameActionException {
        switch(state){
            case PATROL: patrolling(); break;
            case INTERCEPT: intercepting(); break;
            case HELP: helping(); break;
            case DROPOFF: droppingOff(); break;
            case RETURNING: returning(); break;
            case ROAMING: roam(); break;
        }
    }

    public void patrolling() throws GameActionException {
        RobotInfo threats[] = rc.senseNearbyRobots(-1, enemy);
        int closest = Integer.MAX_VALUE;
        RobotInfo target = null;
        RobotInfo friendlies[] = rc.senseNearbyRobots(-1, myTeam);
    }

    public void intercepting() throws GameActionException {

    }

    public void helping() throws GameActionException {

    }

    public void droppingOff() throws GameActionException {

    }

    public void returning() throws GameActionException {

    }

    public void roam() throws GameActionException {

    }

    public void catchup() throws GameActionException{
        int counter = 1;
        while(counter < rc.getRoundNum()){
            checkMessages(counter);
            counter++;
        }
    }

    public void checkMessages() throws GameActionException {
        checkMessages(rc.getRoundNum() - 1);
    }

    public void checkMessages(int roundNumber) throws GameActionException{
        Transaction[] transactions = rc.getBlock(roundNumber);

        for(Transaction trans : transactions){
            int[] message = trans.getMessage();
            if(message[0] == Constants.MESSAGE_KEY){
                processMessage(message);
            }
        }
    }

    public void processMessage(int[] message){
        switch(message[1]){
            case 0:
                hqLocation = CommunicationHelper.convertMessageToLocation(message[2]);
                hqElevation = message[3];
                break;
            case 1:
                wallLocations.remove(CommunicationHelper.convertMessageToLocation(message[2]));
                break;
            case 2:
                wallLocations.add(CommunicationHelper.convertMessageToLocation(message[2]));
                //state = LaticeLandscaper.LaticeState.MOVING_TO_WALL;
                break;
            case 3:
                break;
            case 4:
                enemyHQBlacklist.add(CommunicationHelper.convertMessageToLocation(message[2]));
                if(targetLocation.equals(CommunicationHelper.convertMessageToLocation(message[2]))){
                    targetLocation = null;
                }
                break;
            case 5:
                enemyHQ = CommunicationHelper.convertMessageToLocation(message[2]);
                targetLocation = null;
                break;
        }

    }

}