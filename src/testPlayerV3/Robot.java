package testPlayerV3;

import battlecode.common.*;

import java.util.Random;

public class Robot {

    RobotController rc;
    static Random rng;
    int turnCount = 0;

    int robotNumber;

    static int explorationBoredom = 0;

    static final int MAX_EXPLORE_BOREDOM = 50;

    MapLocation explorationTarget = null;

    MapLocation homeHQ = null;

    MapLocation[] hqLocations;

    RobotInfo[] nearbyEnemies = null;
    RobotInfo[] nearbyAllies = null;



    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public Robot(RobotController r) {
        this.rc = r;
        rng = new Random(rc.getRoundNum()*23981 + rc.getID()*10289);
        Debug.init(rc);
        Nav.init(rc);
        Comms.init(rc);
        BFS.init(rc);
    }

    public void takeTurn() throws GameActionException {
        if(turnCount==0){
            turnCount=rc.getRoundNum();
        }
        if(hqLocations == null){
            hqLocations = Comms.getHQLocations();
        }
        turnCount += 1;
        findHomeHQ();
        senseNearbyRobots();
        robotNumber = Comms.reportAlive();
        senseNearbyIslands();
    }

    public void findHomeHQ() throws GameActionException {
        if (homeHQ == null) {
            //set home HQ
            int radius = rc.getType().visionRadiusSquared;
            RobotInfo[] allies = rc.senseNearbyRobots(radius, rc.getTeam());
            if (allies.length >= 0) {
                for(int i = 0; i< allies.length; i++){
                    if(allies[i].type == RobotType.HEADQUARTERS){
                        homeHQ = allies[i].location;
                        break;
                    }
                }
            }
        }
    }


    public void explore() throws GameActionException{
        if(explorationTarget == null || explorationBoredom > MAX_EXPLORE_BOREDOM || rc.getLocation().distanceSquaredTo(explorationTarget) < rc.getType().actionRadiusSquared){
            //create new exploration target
            int width = rc.getMapWidth();
            int height = rc.getMapHeight();

            double randomX = Math.random()*width;
            double randomY = Math.random()*height;

            int randX = (int) randomX;
            int randY = (int) randomY;

            explorationTarget = new MapLocation(randX, randY);
            explorationBoredom = 0;
        }

        if(explorationTarget != null){
            explorationBoredom++;
            Nav.goTo(explorationTarget);
        }
    }

    public void senseNearbyRobots() throws GameActionException{
        int radius = rc.getType().visionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        Team ally = rc.getTeam();
        nearbyEnemies = rc.senseNearbyRobots(radius, opponent);
        nearbyAllies = rc.senseNearbyRobots(radius, ally);
    }

    public void senseNearbyIslands() throws GameActionException{
        int[] islandLocations = rc.senseNearbyIslands();
        if(islandLocations.length > 0){
            for(int i = 0; i < islandLocations.length; i++){
                Comms.updateIslandInfo(islandLocations[i]);
            }
        }
    }

}
