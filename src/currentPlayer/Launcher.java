package currentPlayer;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Launcher extends Robot{
    public Launcher(RobotController r) {
        super(r);
    }
    Set<MapLocation> enemyIslandLocations = new HashSet<>();

    static int countNearbyAllyLaunchers = 0;

    MapLocation leaderLocation = null;
    boolean isLeader = false;






    public void takeTurn() throws GameActionException {
        super.takeTurn();
        enemyIslandLocations = Comms.getTeamIslandLocations(rc.getTeam().opponent());
        checkLeaderDetails();
        setCountNearbyAllyLaunchers();

        if(nearbyEnemies.length > 0) {
            tryAttack();  // Try to attack someone
        }

        if(!tryGoToEnemyHQ()){ //Try to go to enemy HQ
            if(!isLeader) {
                Nav.goTo(leaderLocation); //follow the leader
            } else if(enemyIslandLocations.size() > 0){ //try to capture enemy islands
                MapLocation closestEnemyIsland = enemyIslandLocations.iterator().next();
                Nav.goTo(closestEnemyIsland);
            } else explore();
        }



    }

    public void checkLeaderDetails() throws GameActionException{
         leaderLocation = getLeaderLocation();
         isLeader = isLeader(leaderLocation);
    }

    public boolean tryGoToEnemyHQ() throws GameActionException{
        for (RobotInfo robot : nearbyEnemies) {
            if(robot.type == RobotType.HEADQUARTERS){
                if(rc.getLocation().isAdjacentTo(robot.location)){
                    Debug.setString("I am adjacent to enemy HQ at " + robot.location);
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
            //fnd enemy with lowest health
            int lowestHealth = Integer.MAX_VALUE;
            MapLocation weakestEnemyLoc = null;
            for(int i = enemies.length; --i>=0;){
                if(enemies[i].health < lowestHealth){
                    lowestHealth = enemies[i].health;
                    weakestEnemyLoc = enemies[i].location;
                }
            }

            if (rc.canAttack(weakestEnemyLoc)) {
                rc.attack(weakestEnemyLoc);
            }
            else{
                Nav.goTo(weakestEnemyLoc);
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



