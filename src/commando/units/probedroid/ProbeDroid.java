package commando.units.probedroid;

import battlecode.common.*;
import com.sun.tools.internal.jxc.ap.Const;
import commando.base.KillMeNowException;
import commando.base.MobileUnit;
import commando.communication.CommunicationHelper;
import commando.pathing.Bug;
import commando.pathing.Simple;
import commando.units.laticelandscaper.LaticeLandscaper;
import commando.utility.*;


public class ProbeDroid extends MobileUnit {

    DroidList<MapLocation> enemyHQLocations;
    DroneState state;
    DroidList<MapLocation> wallLocations;
    int gridOffsetX, gridOffsetY;
    int homeQuad;
    RobotInfo closestNetGun;
    RobotInfo target;
    MapLocation targetSpot;
    MapLocation patrolPoint;
    MapLocation nearestFlooding;
    Bug path = null;
    DroidList<RobotInfo> knownNetGuns;
    DroidList<MapLocation> knownFlooding;
    DroidList<MapLocation> enemyHQBlacklist;
    DroidList<MapLocation> patrolPath;
    boolean combineToFormBarrier;
    DroidList<MapLocation> defenseGridLocations;
    DroidList<MapLocation> allDefenseGridLocations;

    public ProbeDroid (RobotController rc) {
        super(rc);
        wallLocations = new DroidList<>();
        enemyHQBlacklist = new DroidList<>();
        patrolPath = new DroidList<>();
        knownNetGuns = new DroidList<>();
        knownFlooding = new DroidList<>();
        defenseGridLocations = new DroidList<>();
        allDefenseGridLocations = new DroidList<>();
        closestNetGun = null;
        target = null;
        targetSpot = null;
        patrolPoint = null;
        nearestFlooding = null;
        path = null;
        combineToFormBarrier = false;
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
        DROPOFF_DRONE,
        RETURNING,
        ROAMING,
        MOVING_TO_GRID,
        STANDING_ON_GRID,
        POST_GRID   //This name is a place holder for whatever state we end up with once we get to a point where we do something here
    }


    public void onInitialization() throws GameActionException {
        catchup();

        if(hqLocation.x % 2 == 0){
            gridOffsetX = 1;
        }
        if(hqLocation.y % 2 == 0){
            gridOffsetY = 1;
        }
        Unsorted.updateKnownNetguns(knownNetGuns, rc);
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

        patrolPoint = patrolPath.get(0);
        targetLocation = patrolPoint;
        Bug path = new Bug(rc.getLocation(), patrolPoint, rc);
        enemyHQLocations = Unsorted.generatePossibleEnemyHQLocation(hqLocation, rc);
        enemyHQBlacklist.removeAll(enemyHQBlacklist);
    }

    public void turn() throws GameActionException, KillMeNowException {
        checkMessages();
        lookForFlooding();
        //Unsorted.updateKnownFlooding(knownFlooding, rc);
        switch(state){
            case PATROL: patrolling(); break;
            case INTERCEPT: intercepting(); break;
            case HELP: helping(); break;
            case DROPOFF: droppingOff(); break;
            case DROPOFF_DRONE: droppingOffDrone(); break;
            case RETURNING: returning(); break;
            case ROAMING: roam(); break;
            case MOVING_TO_GRID: movingToGrid(); break;
            case STANDING_ON_GRID: standingOnGrid(); break;
            case POST_GRID: postGrid(); break;
        }
    }

    public void patrolling() throws GameActionException {
        //Unsorted.updateKnownNetguns(knownNetGuns, rc);
        System.out.println("I'M IN PATROL MODE!!!");
        RobotInfo[] threats = rc.senseNearbyRobots(-1, enemy);
        if (threats.length > 0){
            state = DroneState.INTERCEPT;
            target = checkForTargets();
            if(target == null){
                target = threats[0];
            }
            targetLocation = target.getLocation();
            path = new Bug(rc.getLocation(), target.location, rc);
            //intercepting();
            return;
        }
        target = checkForTargets();
        //target = null;
        if (target != null) {
            //There is a target, switch to intercept mode
            targetLocation = target.getLocation();
            path = new Bug(rc.getLocation(), target.location, rc);
            targetSpot = target.location;
            state = DroneState.INTERCEPT;
            intercepting();
            return;
        } else {
            System.out.println("NO TARGETS FOUND!!!");
            //There is no target, stay in patrol mode
            if (rc.getLocation().isAdjacentTo(patrolPoint)){
                //Patrol point reached, setting next patrol point
                System.out.println("PATROL POINT AT ("+patrolPoint.x+", "+patrolPoint.y+") reached");
                patrolPath.add(patrolPath.get(0));
                patrolPath.remove(0);
                patrolPoint = patrolPath.get(0);
                targetLocation = patrolPoint;
                path = new Bug(rc.getLocation(), patrolPoint, rc);
                path.run();
            } else {
                //Patrol point not reached, moving to patrol point
                if (path == null) {
                    targetLocation = patrolPoint;
                    path = new Bug(rc.getLocation(), patrolPoint, rc);
                }
                path.run();
            }
        }
    }

    public void intercepting() throws GameActionException {
        System.out.println("I'M INTERCEPTING A TARGET!!!");
        //If the drone has reached and is adjacent to target
        if (target == null){
            target = checkForTargets();
//            RobotInfo threats[] = rc.senseNearbyRobots(-1, enemy);
//            for (RobotInfo unit : threats) {
//                if (unit.location.isAdjacentTo(rc.getLocation())){
//                    target = unit;
//                    break;
//
//                }
//            }
//            if (target == null){
//                target = Unsorted.getClosestUnit(threats, rc);
//            }
            //if (rc.getLocation().isAdjacentTo(target.location)) {

                //target = Unsorted.getClosestUnit(threats, rc);
        }

        if (rc.canPickUpUnit(target.ID)) {
            rc.pickUpUnit(target.ID);
            if (target.team == myTeam) {
                state = DroneState.HELP;
                helping();
                return;
            } else {
                state = DroneState.DROPOFF;
                droppingOff();
                return;
            }
        }
        //}
        //A quick check of whether it can still sense should solve the potential issue of multiple drones going for a single unit
        if (rc.canSenseRobot(target.ID)) {
            if (path == null) {
                targetLocation = target.getLocation();
                path = new Bug(rc.getLocation(), target.location, rc);
            }
            if(path.run()) {
                return;
            }
        } else {
//            //Switch targets if there are more targets
//            //target = checkForTargets();
//            RobotInfo otherThreats[] = rc.senseNearbyRobots(-1, enemy);
//            if (otherThreats.length > 0) {
//                target = otherThreats[0];
//            }
//            if (target != null) {
//                if (target.location.isAdjacentTo(rc.getLocation())){
//                    if (rc.canPickUpUnit(target.ID)) {
//                        System.out.println("I CAN PICK UP A UNIT!!!");
//                        rc.pickUpUnit(target.ID);
//                        state = DroneState.DROPOFF;
//                        return;
//                    }
//                }
//                targetLocation = target.getLocation();
//                path = new Bug(rc.getLocation(), target.getLocation(), rc);
//
//                //Since a new target was found, call intercepting() again to go through
//                //1. Checking if it's adjacent to the new target
//                //      - Pick up new target and switch to Dropoff mode
//                //2. Otherwise move towards target
//                //Should definitely be able to sense the target if it found a new one, so it theoretically shouldn't reach this point more than once in a turn
//                //intercepting();
//                return;
//            } else {
                //No Valid targets found, switching to return state
                target = null;
                targetLocation = null;
                path = null;
                state = DroneState.RETURNING;
                returning();
                return;
//            }
        }
    }

    public void helping() throws GameActionException {
        DroidList<MapLocation> laticeTiles = Unsorted.lookForLatice(gridOffsetX, gridOffsetY, rc);
        if (!(laticeTiles.isEmpty())){
            for (MapLocation spot : laticeTiles){
                if ((rc.getLocation().isAdjacentTo(spot))&&(rc.canDropUnit(rc.getLocation().directionTo(spot)))){
                    rc.dropUnit(rc.getLocation().directionTo(spot));
                    target = null;
                    path = null;
                    targetLocation = null;
                    state = DroneState.RETURNING;
                    returning();
                    return;
                }
            }
        }

        if (path == null) {
            targetLocation = hqLocation;
            path = new Bug(rc.getLocation(), hqLocation, rc);
        }

        if(path.run()){
            return;
        }
    }

    public void droppingOff() throws GameActionException {
        MapLocation nearestWater = Unsorted.getClosestMapLocation(knownFlooding, rc);

        if(nearestWater == null){
            if(targetLocation == null || rc.getLocation().equals(targetLocation)){
                targetLocation = new MapLocation(rand.nextInt(rc.getMapWidth()), rand.nextInt(rc.getMapHeight()));
                path = new Bug(rc.getLocation(), targetLocation, rc);
            }
            path.run();
            return;
        }

        if (rc.getLocation().isAdjacentTo(nearestWater)) {
            if (rc.canDropUnit(rc.getLocation().directionTo(nearestWater))) {
                rc.dropUnit(rc.getLocation().directionTo(nearestWater));
                target = null;
                state = DroneState.RETURNING;
                returning();
                return;
            }
        }

        if (nearestFlooding == null || !(nearestWater.equals(nearestFlooding))) {
            nearestFlooding = nearestWater;
            targetLocation = nearestWater;
            path = new Bug(rc.getLocation(), nearestWater, rc);
            path.run();
        } else {
            if (path == null) {
                targetLocation = nearestFlooding;
                path = new Bug(rc.getLocation(), nearestFlooding, rc);
            }
            path.run();
        }
    }

    public void droppingOffDrone() throws GameActionException {
        if (rc.getLocation().distanceSquaredTo(hqLocation) <= GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED){

            for (Direction dropDir : Constants.DIRECTIONS){
                if (rc.getLocation().add(dropDir).distanceSquaredTo(closestNetGun.location) <= GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED){
                    if (rc.canDropUnit(dropDir)){
                        rc.dropUnit(dropDir);
                        target = null;
                        state = DroneState.RETURNING;
                        returning();
                        return;
                    }
                }
            }
            if (path == null){
                targetLocation = hqLocation;
                path = new Bug(rc.getLocation(), hqLocation, rc);
            }
            path.run();
        } else {
            if (path == null){
                targetLocation = hqLocation;
                path = new Bug(rc.getLocation(), hqLocation, rc);
            }
            path.run();
        }
    }

    public void returning() throws GameActionException {
        //Purpose is to reset Bug path to the last patrol point and get it back on its patrol path;
        targetLocation = patrolPoint;
        path = new Bug(rc.getLocation(), patrolPoint, rc);
        state = DroneState.PATROL;
        patrolling();
        return;

    }

    public void roam() throws GameActionException {

    }

    public void movingToGrid() throws GameActionException {
        if(allDefenseGridLocations.contains(rc.getLocation())){
            state = DroneState.STANDING_ON_GRID;
            targetLocation = null;
            path = null;
            return;
        }

        MapLocation closest = Unsorted.getClosestMapLocation(defenseGridLocations, rc);

        if(targetLocation == null){
            targetLocation = closest;
            path = new Bug(rc.getLocation(), closest, rc);
        } else if(!targetLocation.equals(path.end)){
            targetLocation = closest;
            path = new Bug(rc.getLocation(), closest, rc);
        }

        path.run();
    }

    public void standingOnGrid() {
        return;
    }

    public void postGrid() {

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
                if(targetLocation != null && targetLocation.equals(CommunicationHelper.convertMessageToLocation(message[2]))){
                    targetLocation = null;
                }
                break;
            case 5:
                enemyHQ = CommunicationHelper.convertMessageToLocation(message[2]);
                targetLocation = null;
                break;
            case 6:
                if(message[2] == 2){
                    state = DroneState.MOVING_TO_GRID;
                    targetLocation = null;
                    path = null;
                }
                break;
            case 12:
                defenseGridLocations.remove(CommunicationHelper.convertMessageToLocation(message[2]));
                if(targetLocation != null && targetLocation.equals(CommunicationHelper.convertMessageToLocation(message[2]))){
                    targetLocation = null;
                    path = null;
                }
                break;
            case 13:
                defenseGridLocations.add(CommunicationHelper.convertMessageToLocation(message[2]));
                if(!allDefenseGridLocations.contains(CommunicationHelper.convertMessageToLocation(message[2]))){
                    allDefenseGridLocations.add(CommunicationHelper.convertMessageToLocation(message[2]));
                }
                break;
        }

    }

    public RobotInfo checkForTargets() {
        RobotInfo targetFound = null;
        RobotInfo threats[] = rc.senseNearbyRobots(-1, enemy);
        RobotInfo friendlies[] = rc.senseNearbyRobots(-1, myTeam);
        if (threats.length >= 1){
            //Enemy spotted
            targetFound = Unsorted.getClosestUnit(threats, rc);
        } else if (!(Unsorted.checkForHelpNeeded(friendlies, gridOffsetX, gridOffsetY, rc).isEmpty())) {
            //No enemies spotted, Unit in need of assistance spotted
            if (rc.getRoundNum()> Constants.ASSIST_START_ROUND) {
                targetFound = Unsorted.getClosestUnit(Unsorted.checkForHelpNeeded(friendlies, gridOffsetX, gridOffsetY, rc), rc);
            }
        } else {
            //No Enemies or Units in need of assistance spotted, should switch to return state
            targetFound = null;
        }

        return targetFound;
    }

    public void lookForFlooding() throws GameActionException {
        DroidList<MapLocation> searchTiles = new DroidList<>();
        searchTiles.add(rc.getLocation());
//        searchTiles.addAll(Unsorted.getTilesAtSquareRadius(rc.getLocation(), 1, rc));
//        searchTiles.addAll(Unsorted.getTilesAtSquareRadius(rc.getLocation(), 2, rc));

        for(MapLocation loc : searchTiles){
            if(rc.canSenseLocation(loc)){
                if(!knownFlooding.contains(loc) && rc.senseFlooding(loc)){
                    knownFlooding.add(loc);
                    if(knownFlooding.size() > 50){
                        knownFlooding.remove(0);
                    }
                }
            }
        }
    }

}