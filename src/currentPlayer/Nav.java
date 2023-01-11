package currentPlayer;
import battlecode.common.*;
import scala.Int;


import static currentPlayer.Robot.directions;
import static currentPlayer.Robot.rng;

public class Nav{
        static RobotController rc;

        static int roundsSinceLastReset = 0;
        static int closestDistToTargetSoFar = Integer.MAX_VALUE;

        static Direction lastMoveDir = Direction.NORTH;
        static MapLocation lastWallChecked = null;

        public static void init(RobotController r) {
            rc = r;
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
        if(!rc.isMovementReady())  return false;
        if (rc.getLocation().equals(destination)) return false;

        //Bug pathing to find best direction
        Direction bestDir = getBugNavDir(destination);
        Debug.consoleLog("best Bug path Dir " + bestDir);
        goTo(bestDir);

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

    //bugnav to a location
    static Direction getBugNavDir(MapLocation destination) throws GameActionException {
            //TODO add dangerous locations to avoid
        roundsSinceLastReset++;
        if(!rc.isMovementReady()) return Direction.CENTER;;
        if (rc.getLocation().equals(destination)) return Direction.CENTER;

        Debug.consoleLog("BugNav to " + destination);

        // every 20 rounds, reset the closest distance
        if (roundsSinceLastReset >= 20) {
            roundsSinceLastReset = 0;
            closestDistToTargetSoFar = Integer.MAX_VALUE;
        }

        //setup for iteration
        Direction dir = lastMoveDir;
        Direction greedyDir = null;
        Direction wallDir = lastMoveDir;
        boolean lastWallUpdated = false;
        int closestGreedyDist = Integer.MAX_VALUE;
        boolean wallDirSet = false;

        if (lastWallChecked != null) {
            wallDir = rc.getLocation().directionTo(lastWallChecked);
            dir = rc.getLocation().directionTo(lastWallChecked);
        }

        //iterate through directions
        for (int i = 8; --i >= 0; ) {

            //Find the direction that is closest to the target (greedyDir)
                MapLocation potentialLoc = rc.adjacentLocation(dir);
                int greedyDist = potentialLoc.distanceSquaredTo(destination);
                if (greedyDist < closestGreedyDist && rc.canSenseLocation(potentialLoc) && rc.canMove(dir)) {
                    closestGreedyDist = greedyDist;
                    greedyDir = dir;
                }

                //Keep your left hand on the wall. Find that direction (wallDir)
                Direction potentialWallDir = dir.rotateLeft().rotateLeft();
                MapLocation potentialWallLoc = rc.adjacentLocation(wallDir);

                if (!wallDirSet) {
                    if (rc.canSenseLocation(potentialWallLoc) && rc.canMove(potentialWallDir)) {
                        wallDir = potentialWallDir;
                        wallDirSet = true;
                    }

                    Debug.consoleLog("Iteration"+ i + ", Closest Dist" + greedyDist);
                }
                dir = dir.rotateRight(); // try again with rotated direction
            }

        //Check if breaking off from the wall is a good idea
        if (closestGreedyDist < closestDistToTargetSoFar) {
            closestDistToTargetSoFar = closestGreedyDist;
            lastMoveDir = greedyDir;
            return greedyDir;
        }
        //Else keep following the wall
        lastMoveDir = wallDir;
        return wallDir;

        }






    static void moveRandomly() throws GameActionException{
        Direction dir = directions[rng.nextInt(directions.length)];
        Nav.goTo(dir);
    }

}
