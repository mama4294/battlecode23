package currentPlayer;

import battlecode.common.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Launcher extends Robot{
    public Launcher(RobotController r) {
        super(r);
    }
    Set<MapLocation> enemyIslandLocations = new HashSet<>();

    static int countNearbyAllyLaunchers = 0;

    MapLocation leaderLocation = null;
    MapLocation defensiveLocation = null;
    boolean isLeader = false;






    public void takeTurn() throws GameActionException {
        super.takeTurn();
        enemyIslandLocations = Comms.getTeamIslandLocations(rc.getTeam().opponent());
        checkLeaderDetails();
        setCountNearbyAllyLaunchers();
        if(turnCount == 1) guessEnemyHQLocsFromSymmetry();
        if(defensiveLocation == null) setDefensivePosition();

        getOutOfDangerousArea();
        tryAttack();  // Try to attack someone
        MapLocation enemyIslandLoc = senseNearbyForEnemyIslands();
        if(enemyIslandLoc != null) Nav.goTo(enemyIslandLoc); //GoTo enemy island location

        if(!tryGoToEnemyHQ()){ //Try to go to enemy HQ
            if(!isLeader) {
                Nav.goTo(leaderLocation); //follow the leader
                Debug.setString("Following the leader at " + leaderLocation);
            } else if(enemyIslandLocations.size() > 0){ //try to capture enemy islands
                MapLocation closestEnemyIsland = enemyIslandLocations.iterator().next();
                Nav.goTo(closestEnemyIsland);
                Debug.setString("Capturing enemy island at" + closestEnemyIsland);
            } else if( rc.getRoundNum() < 300 && possibleEnemyHQLocations.size() > 0){
                    MapLocation possibleEnemyHQ = possibleEnemyHQLocations.iterator().next();
                    Nav.goTo(possibleEnemyHQ);
                Debug.setString("Going to possible enemy HQ at " + possibleEnemyHQ);
                if(rc.canSenseLocation(possibleEnemyHQ)) possibleEnemyHQLocations.remove(); //reset
            } else{
                if(defensiveLocation != null) Nav.goTo(defensiveLocation);
//                MapLocation center = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
//                Nav.goTo(center);
                Debug.setString("Going to defensive location at: " + defensiveLocation);
            }
        }
    }

    public void setDefensivePosition() throws GameActionException{
        MapLocation center = new MapLocation(rc.getMapWidth()/2, rc.getMapHeight()/2);
        int x = center.x - homeHQ.x;
        int y = center.y - homeHQ.y;
        defensiveLocation = new MapLocation(homeHQ.x+x/2, homeHQ.y + y/2);
    }

    public void checkLeaderDetails() throws GameActionException{
         leaderLocation = getLeaderLocation();
         isLeader = isLeader(leaderLocation);
    }

    public void getOutOfDangerousArea() throws GameActionException{
        for (RobotInfo robot : nearbyEnemies) {
            if(robot.type == RobotType.HEADQUARTERS){
                if(rc.getLocation().distanceSquaredTo(robot.location) <= RobotType.HEADQUARTERS.actionRadiusSquared) {
                    Nav.goTo(rc.getLocation().directionTo(robot.location).opposite());
                    Debug.setString("Getting out of HQ attack range");
                }
            }
        }
    }

    public boolean tryGoToEnemyHQ() throws GameActionException{
        for (RobotInfo robot : nearbyEnemies) {
            if(robot.type == RobotType.HEADQUARTERS){
                if(rc.getLocation().distanceSquaredTo(robot.location) > RobotType.HEADQUARTERS.actionRadiusSquared){
                    Debug.setString("I'm near the enemy HQ at " + robot.location);
                }else{
                    Nav.goTo(robot.location);
                    Debug.setString("I am going to enemy HQ at " + robot.location);
                }
                return true;
            }
        }
        return false;
    }


    public void setCountNearbyAllyLaunchers() throws GameActionException{
        int count = 0;
        for(int i = nearbyAllies.length; --i>=0;){
            if(nearbyAllies[i].type == RobotType.LAUNCHER){
                count++;
            }
        }
        countNearbyAllyLaunchers = count;
    }



    public void tryAttack() throws GameActionException {
        RobotInfo[] enemies = nearbyEnemies;

        if (enemies.length > 0) {
            //find enemy with lowest health
            MapLocation toAttack = getPrioritizedEnemyToAttack();
            if(toAttack == null) return;

            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
                Direction retreatDir = rc.getLocation().directionTo(toAttack).opposite();
                if(rc.canMove(retreatDir)) rc.move(retreatDir); //retreat after attacking to avoid counter attack
            }
            else{
                Nav.goTo(toAttack);
            }
        }
    }






    public MapLocation getLeaderLocation () throws GameActionException {
        int leaderId = rc.getID();
        MapLocation leaderLocation = rc.getLocation();
       for(int i = nearbyAllies.length; --i>=0;){
           if(nearbyAllies[i].type == RobotType.LAUNCHER){
               if(nearbyAllies[i].ID < leaderId){
                   leaderId = nearbyAllies[i].ID;
                   leaderLocation = nearbyAllies[i].location;
               }
           }
       }
       return leaderLocation;
    }






    public boolean isLeader (MapLocation leaderLoc) throws GameActionException{
        return rc.getLocation().equals(leaderLoc);
    }

}



