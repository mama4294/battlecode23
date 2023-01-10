package currentPlayer;
import battlecode.common.*;


import static currentPlayer.Robot.directions;
import static currentPlayer.Robot.rng;

public class Nav{
        static RobotController rc;

        public static void init(RobotController r) {
            rc = r;
        }


    // tries to move in the general direction of dir
    static boolean goTo(Direction dir) throws GameActionException {
        if(!rc.isMovementReady()) return false;
        if(dir == Direction.CENTER){
            if(rc.canMove(dir)) return true;
        }

        Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateLeft().rotateLeft(), dir.rotateRight(), dir.rotateRight().rotateRight()};
        for (Direction d : toTry) {
            if (rc.canMove(d))
                rc.move(d);
                return true;
        }
        return false;
    }

    // navigate towards a particular location
    static boolean goTo(MapLocation destination) throws GameActionException {
        Debug.setIndicatorLineYellow(destination);
        if(!rc.isMovementReady())  return false;

        if (rc.getLocation().equals(destination)) {
            return goTo(Direction.CENTER);
        } else {
            return goTo(rc.getLocation().directionTo(destination));
        }
    }

    static void moveRandomly() throws GameActionException{
        Direction dir = directions[rng.nextInt(directions.length)];
        Nav.goTo(dir);
    }

}
