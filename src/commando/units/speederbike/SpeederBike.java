package commando.units.speederbike;

import battlecode.common.*;
import commando.base.KillMeNowException;
import commando.base.MobileUnit;
import commando.pathing.Simple;
import commando.units.buzzdroid.BuzzDroid;
import commando.utility.Constants;
import commando.utility.Unsorted;

public class SpeederBike extends MobileUnit {

    /**
     * The number of landscapers to deploy to the wall before we start raiding
     */
    private static final boolean DEBUG = true;

    private enum states {
        /**
         * Waiting for a landscaper to move
         */
        WAITING_FOR_PASSENGER,

        /**
         * Once we pick up a passenger, we need to move out of the wall
         */
        LEAVING_WALL,

        /**
         * we are putting the passenger down (with a potential wait for free space)
         */
        PLACE_PASSENGER,

        /**
         * Re entering the wall after dropping the passenger
         */
        ENTERING_WALL,

        /**
         * check to see if we want to continue
         */
        CHECK_TO_CONTINUE,

        /**
         *
         */
        RAIDING
    }

    boolean hasHelped;
    boolean hasMoved1;
    boolean hasMoved2;
    boolean hasDropped;
    RobotInfo holding;

    /**
     * The direction to exit the wall. Use this - 180 to re-enter
     */
    Direction exit_direction;

    /**
     * The state we're currently running
     */
    states current_state;

    public SpeederBike(RobotController rc){
        super(rc);
        hasHelped = false;
        hasMoved1 = false;
        hasMoved2 = false;
        hasDropped = false;
        holding = null;

        this.exit_direction = Direction.SOUTH;
        this.current_state = states.WAITING_FOR_PASSENGER;
    }

    @Override
    public void onInitialization() throws GameActionException, KillMeNowException {
        super.onInitialization();

        RobotType type = RobotType.LANDSCAPER;

        if (enoughWorkersOnWall()) {
            // we want to go raiding
            System.out.println(
                    rc.getType().toString() +
                            ": STATE CHANGE FROM \"" +
                            this.current_state.toString() +
                            "\" TO \"" + states.RAIDING.toString() +
                            "\"...");
            this.current_state = states.RAIDING;
        }
    }

    private boolean enoughWorkersOnWall() {
        return Unsorted.getNumberOfNearbyFriendlyUnitType(RobotType.LANDSCAPER, rc) > Constants.LANDSCAPERS_ON_WALL;
    }

    public void turn() throws GameActionException, KillMeNowException {




        switch (this.current_state) {
            case WAITING_FOR_PASSENGER:

                if(Unsorted.getNumberOfNearbyFriendlyUnitType(RobotType.LANDSCAPER,rc) > Constants.LANDSCAPERS_ON_WALL){
                    this.current_state = states.RAIDING;
                    turn();
                    return;
                }

                // Get the location to check and see if someone is there
                if (rc.isLocationOccupied(this.spawn.add(this.exit_direction.rotateLeft().rotateLeft()))) {
                    RobotInfo bot = rc.senseRobotAtLocation(this.spawn.add(this.exit_direction.rotateLeft().rotateLeft()));
                    if (bot.getType() == RobotType.LANDSCAPER) {
                        if (this.rc.canPickUpUnit(bot.ID)) {
                            // We found the unit. We want to pick him up and transition to the LEAVING_WALL state
                            this.rc.pickUpUnit(bot.ID);
                            holding = bot;
                            this.current_state = states.LEAVING_WALL;
                            System.out.println("SPEEDER: FOUND A LANDSCAPER. BEGINNING AIRLIFT, AND MOVING STATE TO \"LEAVING_WALL\"");
                        }
                        else {
                            // We found a unit, but cannot pick him up for some reason. Wait for the next turn
                            System.out.println("SPEEDER: FOUND A LANDSCAPER, BUT CANNOT AIRLIFT. ENDING TURN");
                            break;
                        }
                    }
                    else {
                        // THIS IS AN ERROR STATE
                        throw new GameActionException(GameActionExceptionType.CANT_DO_THAT,
                                "There was something other than a landscaper where I expect the landscapers to appear!");
                    }
                }
                else {
                    // We found no one
                    System.out.println("SPEEDER: FOUND NO ONE. ENDING TURN");
                    break;
                }
                // If we didn't break, let's move out of the wall
            case LEAVING_WALL:
                if (exitCompoundWall()) {
                    this.current_state = states.PLACE_PASSENGER;
                }
                else {
                    break;
                }
            case PLACE_PASSENGER:
                // Try to place the passenger on the wall
                if (this.rc.canDropUnit(this.exit_direction.opposite())) {
                    // Drop the unit and move to ENTERING_WALL
                    this.rc.dropUnit(this.exit_direction.opposite());
                    holding = null;
                    System.out.println("SPEEDER: SUCCESSFULLY DEPLOYED UNIT. MOVING TO \"ENTERING_WALL\"");
                }
                // TODO: Account for this implementation in the landscaper code
                /*
                else if (this.rc.canDropUnit(this.exit_direction.opposite().rotateLeft())) {
                    this.rc.dropUnit(this.exit_direction.opposite().rotateLeft());
                    System.out.println("SPEEDER: SUCCESSFULLY DEPLOYED UNIT. MOVING TO \"ENTERING_WALL\"");
                }
                else if (this.rc.canDropUnit(this.exit_direction.opposite().rotateRight())) {
                    this.rc.dropUnit(this.exit_direction.opposite().rotateRight());
                    System.out.println("SPEEDER: SUCCESSFULLY DEPLOYED UNIT. MOVING TO \"ENTERING_WALL\"");
                }

                 */
                else {
                    // We cannot drop. try next time
                    System.out.println("SPEEDER: COULD NOT DEPLOY UNIT. WILL TRY NEXT TURN");
                    break;
                }

                // If we made it here, we didn't break, AKA we're ready to move
                this.current_state = states.ENTERING_WALL;
            case ENTERING_WALL:
                if (enterCompoundWall()) {
                    this.current_state = states.CHECK_TO_CONTINUE;
                }
                else {
                    break;
                }
            case CHECK_TO_CONTINUE:
                if (enoughWorkersOnWall()) {
                    // there are enough workers on the wall. we should transition to RAIDING
                    System.out.println("SPEEDER: CRITICAL MASS OF LANDSCAPERS REACHED (" + Constants.LANDSCAPERS_ON_WALL + "). MOVING TO \"RAIDING\"");
                }
                else {
                    // Moar!
                    System.out.println("SPEEDER: CRITICAL MASS OF LANDSCAPERS NOT REACHED. RETURNING TO \"WAITING_FOR_PASSENGER\"");
                    this.current_state = states.WAITING_FOR_PASSENGER;

                    // Unfortunately, because we only running a single turn and not a loop, we
                    // cannot go back to WAITING_FOR_PASSENGER until next turn...
                    break;
                }
            case RAIDING:
                BuzzDroid buzz = new BuzzDroid(this);
                buzz.holding = holding;
                buzz.run();
                break;
        }
//        if(!hasHelped){
//            RobotInfo robot = rc.senseRobotAtLocation(rc.getLocation().add(Direction.EAST));
//            if(robot == null){
//                return;
//            } else {
//                if(rc.isReady() && rc.canPickUpUnit(robot.ID)){
//                    rc.pickUpUnit(robot.ID);
//                    hasHelped = true;
//                } else {
//                    return;
//                }
//            }
//        } else if(!hasMoved1){
//            hasMoved1 = tryMove(Direction.SOUTH, rc);
//        } else if(!hasMoved2){
//            hasMoved2 = tryMove(Direction.SOUTH, rc);
//        } else if(!hasDropped){
//            if(rc.isReady() && rc.canDropUnit(Direction.NORTH)){
//                rc.dropUnit(Direction.NORTH);
//                hasDropped = true;
//            }
//            return;
//        } else {
//            tryMove(Direction.EAST, rc);
//        }
    }

    /**
     * Exit the compound wall.
     * @return ``true`` if we successfully crossed the boundry, ``false`` if otherwise
     * @throws GameActionException if something auto-toasters
     */
    private boolean exitCompoundWall() throws GameActionException {
        System.out.println("SPEEDER: EXITING COMPOUND...");
        // Try to move two units along the this.exit_direction
        Direction direction = this.exit_direction;

        String direction_string = "EXIT";
        states next_state = states.PLACE_PASSENGER;

        MapLocation current_location = this.rc.getLocation();
        if ((current_location.x == this.spawn.x) && (current_location.y == this.spawn.y)) {
            // We have yet to move
            // Try to move one unit
            if (Simple.tryMove(direction, rc)) {
                // try to move another
                if (Simple.tryMove(direction, rc)) {
                    // Move to the next state
                    System.out.println("SPEEDER: " + direction_string + "ED COMPOUND. MOVING TO \"" + next_state.toString() + "\"");
                }
                else {
                    // We only moved one unit. For some reason we cannot advance, so we'll end here
                    System.out.println("SPEEDER: COULD NOT " + direction_string + " COMPOUND. WILL TRY NEXT TURN");
                    return false;
                }
            }
            else {
                // We could not get out
                System.out.println("SPEEDER: COULD NOT FIND A HOLE TO " + direction_string + " WITH. WILL TRY NEXT TURN");
                return false;
            }
        }
        else if ((current_location.x == this.spawn.add(direction).x) && (current_location.y == this.spawn.add(direction).y)) {
            if (Simple.tryMove(direction, rc)) {
                // Move and go to the PLACE_PASSENGER state
                System.out.println("SPEEDER: SUCCESSFULLY " + direction_string + "ED THE COMPOUND. MOVING TO \"" + next_state.toString() + "\"");
            }
            else {
                // Could not move. Try next turn
                System.out.println("SPEEDER: COULD NOT " + direction_string + " COMPOUND. WILL TRY NEXT TURN");
                return false;
            }
        }
        else {
            throw new GameActionException(GameActionExceptionType.CANT_DO_THAT,
                    "We appear to have moved too far!");
        }
        return true;
    }

    /**
     * Enter the compound wall.
     * @return ``true`` if we successfully crossed the boundry, ``false`` if otherwise
     * @throws GameActionException if something auto-toasters
     */
    private boolean enterCompoundWall() throws GameActionException {
        System.out.println("SPEEDER: ENTERING COMPOUND...");
        // Try to move two units along the this.exit_direction
        Direction direction = this.exit_direction.opposite();

        String direction_string = "ENTER";
        states next_state = states.CHECK_TO_CONTINUE;

        MapLocation current_location = this.rc.getLocation();
        if ((current_location.x == this.spawn.add(direction.opposite()).add(direction.opposite()).x) && ((current_location.y == this.spawn.add(direction.opposite()).add(direction.opposite()).y))) {
            // We have yet to move
            // Try to move one unit
            if (Simple.tryMove(direction, rc)) {
                // try to move another
                if (Simple.tryMove(direction, rc)) {
                    // Move to the next state
                    System.out.println("SPEEDER: " + direction_string + "ED COMPOUND. MOVING TO \"" + next_state.toString() + "\"");
                }
                else {
                    // We only moved one unit. For some reason we cannot advance, so we'll end here
                    System.out.println("SPEEDER: COULD NOT " + direction_string + " COMPOUND. WILL TRY NEXT TURN");
                    return false;
                }
            }
            else {
                // We could not get out
                System.out.println("SPEEDER: COULD NOT FIND A HOLE TO " + direction_string + " WITH. WILL TRY NEXT TURN");
                return false;
            }
        }
        else if ((current_location.x == this.spawn.add(direction.opposite()).x) && (current_location.y == this.spawn.add(direction.opposite()).y)) {

            if (Simple.tryMove(direction, rc)) {
                // Move and go to the PLACE_PASSENGER state
                System.out.println("SPEEDER: SUCCESSFULLY " + direction_string + "ED THE COMPOUND. MOVING TO \"" + next_state.toString() + "\"");
            }
            else {
                // Could not move. Try next turn
                System.out.println("SPEEDER: COULD NOT " + direction_string + " COMPOUND. WILL TRY NEXT TURN");
                return false;
            }
        }
        else {
            throw new GameActionException(GameActionExceptionType.CANT_DO_THAT,
                    "We appear to have moved too far!");
        }
        return true;
    }
}
