package commando.units.probedroid;

import battlecode.common.*;
import commando.base.KillMeNowException;
import commando.base.MobileUnit;
import commando.communication.CommunicationHelper;
import commando.pathing.Bug;
import commando.pathing.Simple;
import commando.utility.*;

import java.util.Map;
import java.util.Random;


public class ProbeDroid extends MobileUnit {

    DroidList<MapLocation> enemyHQLocations;
    DroneState state;
    attackState roamState;
    DroidList<MapLocation> wallLocations;
    int gridOffsetX, gridOffsetY;
    int homeQuad;
    int prepRoundStart;
    RobotInfo closestNetGun;
    RobotInfo target;
    RobotInfo transportTarget;
    MapLocation targetSpot;
    MapLocation patrolPoint;
    MapLocation nearestFlooding;
    MapLocation roamingWayPoint;
    MapLocation dropPoint;
    MapLocation transportRally;
    Bug path = null;
    DroidList<RobotInfo> knownNetGuns;
    DroidList<MapLocation> knownFlooding;
    DroidList<MapLocation> enemyHQBlacklist;
    DroidList<MapLocation> patrolPath;
    boolean combineToFormBarrier;
    DroidList<MapLocation> defenseGridLocations;
    DroidList<MapLocation> allDefenseGridLocations;
    DroidList<RobotInfo> loggedFriendlies;
    boolean rushEnable;
    boolean roamEnable;
    boolean noMoreHelp;
    boolean advanceDrone;
    boolean troopTransport;
    boolean endGame;
    boolean ready;
    boolean wallAssistEnable;

    public ProbeDroid (RobotController rc) {
        super(rc);
        wallLocations = new DroidList<>();
        enemyHQBlacklist = new DroidList<>();
        patrolPath = new DroidList<>();
        knownNetGuns = new DroidList<>();
        knownFlooding = new DroidList<>();
        defenseGridLocations = new DroidList<>();
        allDefenseGridLocations = new DroidList<>();
        enemyHQLocations = new DroidList<>();
        loggedFriendlies = new DroidList<>();
        closestNetGun = null;
        target = null;
        transportTarget = null;
        targetSpot = null;
        patrolPoint = null;
        nearestFlooding = null;
        roamingWayPoint = null;
        dropPoint = null;
        path = null;
        combineToFormBarrier = false;
        state = DroneState.PATROL;
        roamState = attackState.RANDOM_MOVE;
        homeQuad = 0;
        gridOffsetX = 0;
        gridOffsetY = 0;
        rushEnable = false;
        roamEnable = false;
        noMoreHelp = false;
        advanceDrone = false;
        troopTransport = false;
        endGame = false;
        ready = false;
        prepRoundStart = 0;
        wallAssistEnable = true;

    }

    enum DroneState {
        PATROL,
        INTERCEPT,
        HELP,
        DROPOFF,
        RETURNING,
        ROAMING,
        RUSHING,
        PUTTING_UNIT_ON_WALL,
        MOVING_TO_GRID,
        STANDING_ON_GRID,
        POST_GRID   //This name is a place holder for whatever state we end up with once we get to a point where we do something here
    }

    enum attackState {
        RANDOM_MOVE,
        KIDNAP,
        ROAM_DROP,
        PICK_TRANSPORT_TARGET,
        SWARM_PROTOCOL_PREP,
        SWARM_PROTOCOL_STAGING_ADVANCE,
        SWARM_PROTOCAL_STAGING_TRANSPORT,
        SWARM_PROTOCOL_EXECUTE,
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



        if (homeQuad == 1){
            transportRally = new MapLocation(hqLocation.x - Constants.TRANSPORT_RALLY_OFFSET, hqLocation.y - Constants.TRANSPORT_RALLY_OFFSET);
        } else if (homeQuad == 2) {
            transportRally = new MapLocation(hqLocation.x + Constants.TRANSPORT_RALLY_OFFSET, hqLocation.y - Constants.TRANSPORT_RALLY_OFFSET);
        } else if (homeQuad == 3) {
            transportRally = new MapLocation(hqLocation.x + Constants.TRANSPORT_RALLY_OFFSET, hqLocation.y + Constants.TRANSPORT_RALLY_OFFSET);
        } else if (homeQuad == 4) {
            transportRally = new MapLocation(hqLocation.x - Constants.TRANSPORT_RALLY_OFFSET, hqLocation.y + Constants.TRANSPORT_RALLY_OFFSET);
        }





    }

    public void turn() throws GameActionException, KillMeNowException {
        checkMessages();
        lookForFlooding();
        RobotInfo friendlies[] = rc.senseNearbyRobots(-1, myTeam);
        for (RobotInfo unit : friendlies) {
            if (!(loggedFriendlies.contains(unit))){
                loggedFriendlies.add(unit);
            }
        }


        if(state == DroneState.MOVING_TO_GRID && defenseGridLocations.size() <= 0){
            state = DroneState.POST_GRID;
            targetLocation = null;
            path = null;
        }

        if(wallAssistEnable && wallLocations.size() > 0){
            state = DroneState.PUTTING_UNIT_ON_WALL;
        } else {
            roamEnable = true;
        }

        if(rushEnable){
            state = DroneState.RUSHING;
        } else if (roamEnable && state != DroneState.MOVING_TO_GRID){
            state = DroneState.ROAMING;
        }

        //Unsorted.updateKnownFlooding(knownFlooding, rc);
        switch(state){
            case PATROL: patrolling(); break;
            case INTERCEPT: intercepting(); break;
            case HELP: helping(); break;
            case DROPOFF: droppingOff(); break;
            case RETURNING: returning(); break;
            case ROAMING: roam(); break;
            case RUSHING: rushing(); break;
            case PUTTING_UNIT_ON_WALL: puttingUnitOnWall(); break;
            case MOVING_TO_GRID: movingToGrid(); break;
            case STANDING_ON_GRID: standingOnGrid(); break;
            case POST_GRID: postGrid(); break;
        }
    }

    public void puttingUnitOnWall() throws GameActionException {
        if(rc.isCurrentlyHoldingUnit()){
            if(wallLocations.size() <= 0){
                state = DroneState.HELP;
                targetLocation = null;
                path = null;
                helping();
                return;
            } else {
                MapLocation closest = Unsorted.getClosestMapLocation(wallLocations, rc);
                if(rc.getLocation().isAdjacentTo(closest)){
                    if(rc.canDropUnit(rc.getLocation().directionTo(closest))){
                        rc.dropUnit(rc.getLocation().directionTo(closest));
                        state = DroneState.PATROL;
                        targetLocation = null;
                        path = null;
                        return;
                    }
                } else if(targetLocation == null || !targetLocation.equals(closest)){
                    targetLocation = closest;
                    path = new Bug(rc.getLocation(), targetLocation, rc);
                }
                path.run();
                return;
            }
        } else {
            RobotInfo[] robots = rc.senseNearbyRobots(-1, myTeam);
            RobotInfo best = null;
            int closest = Integer.MAX_VALUE;

            if (robots == null || robots.length <= 0) {
                state = DroneState.PATROL;
                targetLocation = null;
                path = null;
                patrolling();
                return;
            } else {
                for (RobotInfo robot : robots) {
                    int dist = rc.getLocation().distanceSquaredTo(robot.getLocation());
                    if (robot.getType() == RobotType.LANDSCAPER && !robot.getLocation().isAdjacentTo(hqLocation) && dist < closest) {
                        best = robot;
                        closest = dist;
                    }
                }
            }

            if (best == null) {
                state = DroneState.PATROL;
                targetLocation = null;
                path = null;
                patrolling();
                return;
            } else if (rc.canPickUpUnit(best.getID())){
                rc.pickUpUnit(best.getID());
                targetLocation = null;
                path = null;
                return;
            } else {
                if(targetLocation == null || !targetLocation.equals(best.getLocation())){
                    targetLocation = best.getLocation();
                    path = new Bug(rc.getLocation(), targetLocation, rc);
                }
                path.run();
            }
        }
    }

    public void rushing() throws GameActionException {
        if(rc.isCurrentlyHoldingUnit()){

            checkEnemyHQ();

            MapLocation closest = Unsorted.getClosestMapLocation(enemyHQLocations, rc);

            if(targetLocation == null || enemyHQ != null || !targetLocation.equals(closest)){
                if(enemyHQ != null){
                    if(targetLocation == null || !targetLocation.equals(enemyHQ)) {
                        targetLocation = enemyHQ;
                        path = new Bug(rc.getLocation(), targetLocation, rc);
                    }
                } else if(targetLocation == null || !targetLocation.equals(closest)){
                    targetLocation = closest;
                    path = new Bug(rc.getLocation(), targetLocation, rc);
                }
            }
            path.run();
        } else {
            RobotInfo[] robots = rc.senseNearbyRobots(-1, myTeam);

            RobotInfo target = null;
            int closest = Integer.MAX_VALUE;

            if(robots != null && robots.length > 0){
                for(RobotInfo robot : robots){
                    int dist = rc.getLocation().distanceSquaredTo(robot.getLocation());
                    if(robot.getType() == RobotType.MINER && dist < closest){
                        target = robot;
                        closest = dist;
                    }
                }
            }

            if(target == null){
                state = DroneState.PATROL;
                patrolling();
                return;
            } else {
                if(rc.canPickUpUnit(target.ID)){
                    rc.pickUpUnit(target.ID);
                    int[] message = new int[7];
                    message[0] = Constants.MESSAGE_KEY;
                    message[1] = 14;
                    message[2] = rc.getID();
                    messageQueue.add(message);
                    targetLocation = null;
                    path = null;
                } else {
                    if(targetLocation == null || !targetLocation.equals(target.getLocation())){
                        targetLocation = target.getLocation();
                        path = new Bug(rc.getLocation(), targetLocation, rc);
                    }
                    path.run();
                }
            }
        }
    }

    public void patrolling() throws GameActionException {
        if(combineToFormBarrier){
            state = DroneState.MOVING_TO_GRID;
            targetLocation = null;
            path = null;
        }

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
                    roamingWayPoint = null;
                    if (roamEnable){
                        state = DroneState.ROAMING;
                        roamState = attackState.RANDOM_MOVE;

                        return;
                    }
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

    public void returning() throws GameActionException {
        //Purpose is to reset Bug path to the last patrol point and get it back on its patrol path;
        if (!roamEnable) {
            targetLocation = patrolPoint;
            path = new Bug(rc.getLocation(), patrolPoint, rc);
            state = DroneState.PATROL;
            patrolling();
            return;
        } else {
            state = DroneState.ROAMING;
            roam();
            return;
        }

    }

    public void roam() throws GameActionException {
        switch (roamState) {
            case RANDOM_MOVE: randomRoaming(); break;
            case KIDNAP: kidnap(); break;
            case ROAM_DROP: roamDrop(); break;
            case PICK_TRANSPORT_TARGET: pickTransportTarget(); break;
            case SWARM_PROTOCOL_PREP: swarmPrep();break;
            case SWARM_PROTOCOL_STAGING_ADVANCE: swarmStagingAdvance(); break;
            case SWARM_PROTOCAL_STAGING_TRANSPORT: swarmStagingTransport();
            case SWARM_PROTOCOL_EXECUTE: swarmExecute(); break;
        }
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

    public void standingOnGrid() throws GameActionException{
        if(rc.canSenseRadiusSquared(2)){
            RobotInfo[] robots = rc.senseNearbyRobots(2, enemy);

            if(robots != null && robots.length > 0){
                for(RobotInfo robot : robots){
                    if(rc.canPickUpUnit(robot.ID)){
                        rc.pickUpUnit(robot.ID);
                        break;
                    }
                }
            }
        }

        return;
    }

    public void postGrid() throws GameActionException{
        /*target = checkForTargets();
        //target = null;
        if (target != null) {
            //There is a target, switch to intercept mode
            targetLocation = target.getLocation();
            path = new Bug(rc.getLocation(), target.location, rc);
            targetSpot = target.location;
            state = DroneState.ROAMING;
            roamState = attackState.SWARM_PROTOCOL_STAGING;

            return;
        }

        if(targetLocation == null || rc.getLocation().equals(targetLocation)){
            targetLocation = new MapLocation(rand.nextInt(rc.getMapWidth()), rand.nextInt(rc.getMapHeight()));
            path = new Bug(rc.getLocation(), targetLocation, rc);
        }

        path.run();*/

        state = DroneState.ROAMING;
        roamState = attackState.PICK_TRANSPORT_TARGET;
        endGame = true;
        noMoreHelp = true;
        pickTransportTarget();

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
                enemyHQLocations.remove(CommunicationHelper.convertMessageToLocation(message[2]));
                if(targetLocation != null && targetLocation.equals(CommunicationHelper.convertMessageToLocation(message[2]))){
                    targetLocation = null;
                    path = null;
                }
                break;
            case 5:
                enemyHQ = CommunicationHelper.convertMessageToLocation(message[2]);
                if(state == DroneState.RUSHING){
                    targetLocation = null;
                    path = null;
                }
                break;
            case 6:
                if(message[2] == 1){
                    wallAssistEnable = true;
                }

                if(message[2] == 2){
                    state = DroneState.MOVING_TO_GRID;
                    targetLocation = null;
                    path = null;
                    combineToFormBarrier = true;
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
            case 14:
                if(state == DroneState.RUSHING && rc.getID() > message[2]){
                    if(rc.isCurrentlyHoldingUnit()){
                        state = DroneState.HELP;
                    } else {
                        state = DroneState.PATROL;
                    }
                }
                rushEnable = false;
                break;

        }

    }

    public RobotInfo checkForTargets() {
        RobotInfo targetFound = null;
        RobotInfo threats[] = rc.senseNearbyRobots(-1, enemy);
        RobotInfo friendlies[] = rc.senseNearbyRobots(-1, myTeam);
        DroidList<RobotInfo> nonDrones = new DroidList<>();
        if (threats.length >= 1){
            for (RobotInfo unit : threats){
                if (unit.type != RobotType.DELIVERY_DRONE){
                    nonDrones.add(unit);
                }
            }
            if (nonDrones.size() > 0) {
                targetFound = Unsorted.getClosestUnit(nonDrones, rc);
            }
        } else if (!(Unsorted.checkForHelpNeeded(friendlies, gridOffsetX, gridOffsetY, rc).isEmpty())&&!noMoreHelp) {
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

    public void tryTerminate(RobotController rc) throws GameActionException{
        for (Direction dir : Constants.DIRECTIONS){
            if (rc.senseFlooding(rc.getLocation().add(dir)) && rc.canDropUnit(dir)){
                rc.dropUnit(dir);
            }
        }
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

    public void checkEnemyHQ() throws GameActionException{
        if(enemyHQ == null && targetLocation != null){
            if(rc.canSenseLocation(targetLocation)){
                RobotInfo robot = rc.senseRobotAtLocation(targetLocation);
                if(robot == null || robot.getType() != RobotType.HQ) {
                    int[] message = new int[7];
                    message[0] = Constants.MESSAGE_KEY;
                    message[1] = 4;
                    message[2] = CommunicationHelper.convertLocationToMessage(targetLocation);
                    messageQueue.add(message);
                } else {
                    if(robot.getType() == RobotType.HQ){
                        int[] message = new int[7];
                        message[0] = Constants.MESSAGE_KEY;
                        message[1] = 5;
                        message[2] = CommunicationHelper.convertLocationToMessage(targetLocation);
                        messageQueue.add(message);
                    }
                }
            }
        }
    }

    public void randomRoaming() throws GameActionException {
        //Step 1: check if there is a wayPoint or not
        if ((Unsorted.getNumberOfThisType(loggedFriendlies, RobotType.LANDSCAPER) > Constants.SWARM_PREP_LANDSCAPER_COUNT) || (rc.getRoundNum() > Constants.SWARM_PREP_ROUND_OVERIDE)){
            prepRoundStart = rc.getRoundNum();
            roamState = attackState.PICK_TRANSPORT_TARGET;
            pickTransportTarget();
            return;
        }

        RobotInfo[] threats = rc.senseNearbyRobots(-1, enemy);
        if (threats.length > 0){
            roamState = attackState.KIDNAP;
            target = checkForTargets();
            if(target == null){
                target = threats[0];
            }
            targetLocation = target.getLocation();
            path = new Bug(rc.getLocation(), target.location, rc);
            kidnap();
            return;
        }


        randomSearch();


    }

    public void kidnap() throws GameActionException {
        if (target == null) {
            target = checkForTargets();
            if (target == null) {
                roamState = attackState.RANDOM_MOVE;
                return;
            }
        }
        if (rc.canSenseRobot(target.ID)) {


            if (path == null) {
                path = new Bug(rc.getLocation(), target.location, rc);
            }
            if (rc.getLocation().isAdjacentTo(target.location) && rc.canPickUpUnit(target.ID)) {
                rc.pickUpUnit(target.ID);
                roamState = attackState.ROAM_DROP;
                return;
            }
            path.run();
        } else {
            roamState = attackState.RANDOM_MOVE;
            randomRoaming();
            return;
        }



    }

    public void roamDrop() throws GameActionException {
        tryTerminate(rc);
        if (!(rc.isCurrentlyHoldingUnit())){
            roamState = attackState.RANDOM_MOVE;
        }
        randomSearch();




    }

    public void pickTransportTarget() throws GameActionException {
        if (transportTarget == null) {
            RobotInfo[] friendlies = rc.senseNearbyRobots(-1, myTeam);
            if (friendlies.length > 0) {
                DroidList<RobotInfo> landscapers = Unsorted.filterByType(friendlies, hqLocation, RobotType.LANDSCAPER);
                if (landscapers.size() > 0) {
                    transportTarget = Unsorted.getClosestUnit(landscapers, rc);
                    path = new Bug(rc.getLocation(), transportTarget.location, rc);
                }
            }
        }
        if(transportTarget != null && rc.canSenseRobot(transportTarget.ID)) {
            if (rc.canPickUpUnit(transportTarget.ID)){
                rc.pickUpUnit(transportTarget.ID);
            }
            if (rc.isCurrentlyHoldingUnit()) {
                roamState = attackState.SWARM_PROTOCOL_PREP;
                swarmPrep();
                return;
            }
            if (path == null){
                path = new Bug(rc.getLocation(), transportTarget.location, rc);
            }
            path.run();



        } else {
            transportTarget = null;
        }
        if ((rc.getRoundNum() - prepRoundStart > 50) && (transportTarget == null)){ //They have 50 rounds to find and pickup their target
            roamState = attackState.SWARM_PROTOCOL_PREP;
            path = new Bug(rc.getLocation(), enemyHQ, rc);
            swarmPrep();
            return;
        }

        randomSearch();


    }

    public void swarmPrep() throws GameActionException {
        if (rc.isCurrentlyHoldingUnit()){
            troopTransport = true;
        } else {
            advanceDrone = true;
        }
        int distBetweenHQ = hqLocation.distanceSquaredTo(enemyHQ);

        if (rc.getLocation().distanceSquaredTo(enemyHQ) > distBetweenHQ/2){
            if(path == null){
                path = new Bug(rc.getLocation(), enemyHQ, rc);
            }
            path.run();
        } else {
            if (troopTransport){
                roamState = attackState.SWARM_PROTOCAL_STAGING_TRANSPORT;
            }
            if (advanceDrone) {
                roamState = attackState.SWARM_PROTOCOL_STAGING_ADVANCE;
            }

        }



    }

    public void swarmStagingAdvance() throws GameActionException {

        if (rc.getRoundNum() - prepRoundStart > 200){
            roamState = attackState.SWARM_PROTOCOL_EXECUTE;
            swarmExecute();
            return;
        }
        Simple.moveToLocationFuzzyNoFlyZone(enemyHQ, GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED, rc);
    }

    public void swarmStagingTransport() throws GameActionException {
        if (rc.getRoundNum() - prepRoundStart > 200){
            roamState = attackState.SWARM_PROTOCOL_EXECUTE;
            swarmExecute();
            return;
        }
        Simple.moveToLocationFuzzyNoFlyZone(enemyHQ, GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED+7, rc);
    }

    public void swarmExecute() throws GameActionException {

    }

    public void randomSearch() throws GameActionException {
        Random rand = new Random();
        int wayPointX = rand.nextInt(rc.getMapWidth());
        int wayPointY = rand.nextInt(rc.getMapHeight());
        if (roamingWayPoint == null){
            roamingWayPoint = new MapLocation(wayPointX, wayPointY);
            path = new Bug(rc.getLocation(), roamingWayPoint, rc);
            path.run();

        } else if (rc.getLocation().isAdjacentTo(roamingWayPoint)){
            roamingWayPoint = new MapLocation(wayPointX, wayPointY);
            path = new Bug(rc.getLocation(), roamingWayPoint, rc);
            path.run();
        } else {
            if (path == null) {
                path = new Bug(rc.getLocation(), roamingWayPoint, rc);
            }
            path.run();
        }
    }

}