package commando.units.astromech;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import commando.base.MobileUnit;
import commando.pathing.Bug;
import commando.utility.Unsorted;

public class Astromech extends MobileUnit {
    RushState state;
    Bug path;
    boolean netGunBuilt;
    boolean designSchoolBuilt;

    public Astromech (RobotController rc) {
        super(rc);
        state = RushState.SEARCHING_FOR_HQ;
        path = null;
        netGunBuilt = false;
        designSchoolBuilt = false;
    }

    enum RushState {
        SEARCHING_FOR_HQ,
        SETUP,
    }


    public void onInitialization() throws GameActionException {
        enemyHQLocations = Unsorted.generatePossibleEnemyHQLocation(hqLocation, rc);
        if (path == null) {
            path = new Bug(rc. getLocation(), enemyHQLocations.get(0), rc);
        }
        if (path.run()){
            return;
        }
    }

    public void turn() throws GameActionException {
        switch (state) {
            case SEARCHING_FOR_HQ: searchForHQ(); break;
            case SETUP: setup(); break;
        }
    }

    public void searchForHQ () throws GameActionException{
        RobotInfo potHQ = null;
        if (path == null) {
            path = new Bug(rc. getLocation(), enemyHQLocations.get(0), rc);
            if (path.run()){
                return;
            }
        }
        if (rc.canSenseLocation(enemyHQLocations.get(0))){
            potHQ = rc.senseRobotAtLocation(enemyHQLocations.get(0));
            if (potHQ == null || potHQ.getType() != RobotType.HQ) {
                enemyHQLocations.remove(0);
                path = new Bug(rc.getLocation(), enemyHQLocations.get(0), rc);
                if (path.run()){
                    return;
                }
            } else {
                enemyHQ = enemyHQLocations.get(0);
                state = RushState.SETUP;
                setup();
                return;
            }
        }

    }

    public void setup () throws GameActionException {
        if (enemyHQ == null) {enemyHQ = enemyHQLocations.get(0);} //to avoid null pointer exceptions if for some reason they happen
        int dist = rc.getLocation().distanceSquaredTo(enemyHQ);
        if (dist <= 20){

            if (rc.getTeamSoup() >= 250 && !netGunBuilt) {
                if (rc.canBuildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(enemyHQ).rotateLeft())) {
                    rc.buildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(enemyHQ).rotateLeft());
                    netGunBuilt = true;
                } else if (rc.canBuildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(enemyHQ).rotateRight())) {
                    rc.buildRobot(RobotType.NET_GUN, rc.getLocation().directionTo(enemyHQ).rotateLeft());
                    netGunBuilt = true;
                }
            }
            if (rc.getTeamSoup() >= 150 && !designSchoolBuilt) {
                if (rc.canBuildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(enemyHQ)) && rc.getLocation().add(rc.getLocation().directionTo(enemyHQ)).distanceSquaredTo(enemyHQ) <= 20){
                    rc.buildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(enemyHQ));
                    designSchoolBuilt = true;
                }
                else if (rc.canBuildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(enemyHQ).rotateRight()) && rc.getLocation().add(rc.getLocation().directionTo(enemyHQ)).distanceSquaredTo(enemyHQ) <= 20){
                    rc.buildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(enemyHQ).rotateRight());
                    designSchoolBuilt = true;
                }
                else if (rc.canBuildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(enemyHQ).rotateLeft()) && rc.getLocation().add(rc.getLocation().directionTo(enemyHQ)).distanceSquaredTo(enemyHQ) <= 20){
                    rc.buildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(enemyHQ).rotateLeft());
                    designSchoolBuilt = true;
                }
            }


        }

    }


}
