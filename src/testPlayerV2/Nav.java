package testPlayerV2;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import static currentPlayer.Robot.directions;
import static currentPlayer.Robot.rng;

public class Nav{
        static RobotController rc;

        static int roundsSinceLastReset = 0;
        static int closestDistToTargetSoFar = Integer.MAX_VALUE;

        static Direction lastMoveDir = Direction.NORTH;
        static MapLocation lastTarget = null;

        enum BugNavMode {
            FREE_PATHING,
            WALL_WALKING
    }
        static BugNavMode bugNavMode = BugNavMode.FREE_PATHING;

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

    //bugnav to a location
    static Direction getBugNavDir(MapLocation destination) throws GameActionException {
        //TODO add dangerous locations to avoid
        roundsSinceLastReset++;
        if (!rc.isMovementReady()) return Direction.CENTER;
        if (rc.getLocation().equals(destination)) return Direction.CENTER;

        // every 20 rounds, reset the closest distance
        if (roundsSinceLastReset >= 20) {
            roundsSinceLastReset = 0;
            closestDistToTargetSoFar = Integer.MAX_VALUE;
        }

        //setup for iteration
        Direction dir = lastMoveDir;
        Direction greedyDir = null;
        int closestGreedyDist = Integer.MAX_VALUE;

        //Check to see if you can get closer to the target than ever before.
        for (int i = 8; --i >= 0; dir = dir.rotateRight()) {
            MapLocation potentialLoc = rc.adjacentLocation(dir);
            int dist = potentialLoc.distanceSquaredTo(destination);
            if (dist < closestGreedyDist && rc.canSenseLocation(potentialLoc) && rc.canMove(dir)) {
                closestGreedyDist = dist;
                greedyDir = dir;
            }
        }
        if (closestGreedyDist < closestDistToTargetSoFar) {
            lastMoveDir = greedyDir;
            closestDistToTargetSoFar = closestGreedyDist;
            return greedyDir;
        }


        //Else, wall walk
        dir = lastMoveDir;
        for (int i = 8; --i >= 0; dir = dir.rotateRight()) {
            if (rc.canMove(dir)) {
                lastMoveDir = dir;
                return dir;
            }
        }

        return null;
    }










    static void moveRandomly() throws GameActionException{
        Direction dir = directions[rng.nextInt(directions.length)];
        Nav.goTo(dir);
    }

}
