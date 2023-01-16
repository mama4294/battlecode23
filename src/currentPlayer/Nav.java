package currentPlayer;
import battlecode.common.*;

import static currentPlayer.Robot.*;

public class Nav{
        static RobotController rc;

        static int roundsSinceLastReset = 0;
        static int closestDistToTargetSoFar = Integer.MAX_VALUE;

        static MapLocation lastTarget = null;
        static Direction currecntDirection = null;

        enum Handedness{
            LEFT,
            RIGHT
        }

        static Handedness robothandedness = null;


        public static void init(RobotController r) {
            rc = r;
            robothandedness = rng.nextBoolean() ? Handedness.LEFT : Handedness.RIGHT;
        }


    // tries to move in the general direction of dir
    static boolean goTo(Direction dir) throws GameActionException {
        if(!rc.isMovementReady()) return false;
        if(dir == Direction.CENTER){
            if(rc.canMove(dir)) return true;
        }

        if(rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        return false;
    }

    static boolean goTo(MapLocation destination) throws GameActionException {
            //Reset if new target
            if(destination != lastTarget){
                closestDistToTargetSoFar = Integer.MAX_VALUE;
            }
            lastTarget = destination;
        if(!rc.isMovementReady())  return false;
        if (rc.getLocation().equals(destination)) return false;


        //Bug pathing to find best direction
//        Direction bestDir = BFS.getDirFromBFS(destination);
        Direction bestDir = getBugNavDir(destination);
        if(bestDir != null){
            goTo(bestDir);
        }else{
            fuzzyNavTo(destination);
        }


        return false;
    }



    static boolean fuzzyNavTo(MapLocation destination) throws GameActionException {
        if(!rc.isMovementReady())  return false;
        if (rc.getLocation().equals(destination)) {
            return false;
        } else {
            Direction dir = rc.getLocation().directionTo(destination);
            if (rc.canMove(dir)) {
                rc.move(dir);
                return true;
            } else {
                Direction left = dir.rotateLeft();
                Direction right = dir.rotateRight();
                if (rc.canMove(left)) {
                    rc.move(left);
                    return true;
                } else if (rc.canMove(right)) {
                    rc.move(right);
                    return true;
                }
            }
        }
        return false;
    }



    //Pathing to a location
    public static Direction getBugNavDir(MapLocation target) throws GameActionException {

        if(!lastTarget.equals(target))closestDistToTargetSoFar = Integer.MAX_VALUE; //reset if new target
        if(roundsSinceLastReset > 30){ //reset every so often
            roundsSinceLastReset = 0;
            closestDistToTargetSoFar = Integer.MAX_VALUE;
        }
        lastTarget = target;
        roundsSinceLastReset++;

        if (rc.getLocation().equals(target)) return Direction.CENTER; //if you are already there, do nothing
        if (!rc.isMovementReady()) return Direction.CENTER; //if you can't move, do nothing

        Direction d = rc.getLocation().directionTo(target);
        int dist = rc.getLocation().add(d).distanceSquaredTo(target);
        if (rc.canMove(d) && dist < closestDistToTargetSoFar) { //try move directly towards target
            currecntDirection = null; //reset current direction
            closestDistToTargetSoFar = dist; //update closest distance
            return d;
        } else {
            //obstacle in the way, try to move around it
            if (currecntDirection == null) currecntDirection = d; //set current direction if null
            for (int i = directions.length; --i >= 0; currecntDirection = robothandedness == Handedness.LEFT ? currecntDirection.rotateLeft() : currecntDirection.rotateRight()) {
                if (rc.canMove(currecntDirection)) {
                    dist = rc.getLocation().add(currecntDirection).distanceSquaredTo(target);

                    if(dist < closestDistToTargetSoFar)closestDistToTargetSoFar = dist;
                    Direction bestDir = currecntDirection;
                    currecntDirection = robothandedness == Handedness.LEFT ? currecntDirection.rotateRight() : currecntDirection.rotateLeft(); //angle in next current direction to account for concave obsticles.
                    return bestDir;
                }
            }
        }
        return null;
    }










    static void moveRandomly() throws GameActionException{
        Direction dir = directions[rng.nextInt(directions.length)];
        Nav.goTo(dir);
    }

}
