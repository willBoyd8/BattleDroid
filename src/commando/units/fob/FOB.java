package commando.units.fob;

import battlecode.common.*;
import bb8.base.Building;

public class FOB extends Building {
    int numberOfUnitsBuilt;
    FOBState state;
    RobotInfo enemyHQ;

    public FOB (RobotController rc) {
        super(rc);
        numberOfUnitsBuilt = 0;
        state = FOBState.NO_LANDSCAPERS;
        enemyHQ = null;
    }

    public void onInitialization() throws GameActionException {
        enemyHQ = checkForHQ(rc);
    }

    enum FOBState {
        NO_LANDSCAPERS,
        SOME_LANDSCAPERS,
    }

    public void turn() throws GameActionException {
        switch (state) {
            case NO_LANDSCAPERS: break;
            case SOME_LANDSCAPERS: break;
        }

    }

    public void noLandscapers() throws GameActionException{
        if (checkForLandscapers(rc)) {
            state = FOBState.SOME_LANDSCAPERS;
            someLandscapers();
            return;
        }
        if (enemyHQ != null) {
            if (rc.canBuildRobot(RobotType.LANDSCAPER, rc.getLocation().directionTo(enemyHQ.location))) {
                rc.buildRobot(RobotType.LANDSCAPER, rc.getLocation().directionTo(enemyHQ.location));
                state = FOBState.SOME_LANDSCAPERS;
                someLandscapers();
                return;
            }
            else if (rc.canBuildRobot(RobotType.LANDSCAPER, rc.getLocation().directionTo(enemyHQ.location).rotateRight())) {
                rc.buildRobot(RobotType.LANDSCAPER, rc.getLocation().directionTo(enemyHQ.location).rotateRight());
                state = FOBState.SOME_LANDSCAPERS;
                someLandscapers();
                return;
            }
            else if (rc.canBuildRobot(RobotType.LANDSCAPER, rc.getLocation().directionTo(enemyHQ.location).rotateLeft())) {
                rc.buildRobot(RobotType.LANDSCAPER, rc.getLocation().directionTo(enemyHQ.location).rotateLeft());
                state = FOBState.SOME_LANDSCAPERS;
                someLandscapers();
                return;
            }
        }

    }

    public void someLandscapers() throws GameActionException {
        if (!(checkForLandscapers(rc))) {
            state = FOBState.NO_LANDSCAPERS;
            noLandscapers();
            return;
        }
        if (rc.getTeamSoup() > 300 && enemyHQ != null) {
            if (rc.canBuildRobot(RobotType.LANDSCAPER, rc.getLocation().directionTo(enemyHQ.location))) {
                rc.buildRobot(RobotType.LANDSCAPER, rc.getLocation().directionTo(enemyHQ.location));
                state = FOBState.SOME_LANDSCAPERS;
                someLandscapers();
                return;
            }
            else if (rc.canBuildRobot(RobotType.LANDSCAPER, rc.getLocation().directionTo(enemyHQ.location).rotateRight())) {
                rc.buildRobot(RobotType.LANDSCAPER, rc.getLocation().directionTo(enemyHQ.location).rotateRight());
                state = FOBState.SOME_LANDSCAPERS;
                someLandscapers();
                return;
            }
            else if (rc.canBuildRobot(RobotType.LANDSCAPER, rc.getLocation().directionTo(enemyHQ.location).rotateLeft())) {
                rc.buildRobot(RobotType.LANDSCAPER, rc.getLocation().directionTo(enemyHQ.location).rotateLeft());
                state = FOBState.SOME_LANDSCAPERS;
                someLandscapers();
                return;
            }

        }

    }

    public boolean checkForLandscapers(RobotController rc) {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, myTeam);
        int landscapers = 0;
        for (RobotInfo unit : robots) {
            if (unit.getType() == RobotType.LANDSCAPER) {
                landscapers++;
            }
        }
        if (landscapers >= 1) {
            return true;

        }
        return false;
    }

    public RobotInfo checkForHQ(RobotController rc) {
        RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
        for (RobotInfo unit : robots) {
            if (unit.getType() == RobotType.HQ) {
                return unit;
            }
        }
        return null;
    }


}
