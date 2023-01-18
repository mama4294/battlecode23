package currentPlayer;

import battlecode.common.*;

import java.lang.reflect.MalformedParameterizedTypeException;
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


        tryAttack();  // Try to attack someone

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
            //find enemy with lowest health
            MapLocation toAttack = getPrioritizedEnemyToAttack();
            if(toAttack == null) return;

            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
            else{
                Nav.goTo(toAttack);
            }
        }
    }


    public MapLocation getPrioritizedEnemyToAttack () throws GameActionException{
        //Prioritize low heath enemy combatants
        if(nearbyEnemies.length < 1) return null;
        MapLocation prioritizedEnemyLoc = null;
        int prorityScore = Integer.MAX_VALUE; // lower is better

        for(int i = nearbyEnemies.length; --i>=0;){
            RobotInfo enemy = nearbyEnemies[i];
            if(enemy.type == RobotType.HEADQUARTERS) continue;
            int score = enemy.health + getRobotPriority(enemy.type);
            if(score < prorityScore){
                prorityScore = score;
                prioritizedEnemyLoc = enemy.location;
            }
        }
        return prioritizedEnemyLoc;
    }

    public int getRobotPriority(RobotType type) throws  GameActionException{
        switch (type)  {
            case LAUNCHER: return 0;
            case AMPLIFIER: return 100;
            case DESTABILIZER: return 200;
            case BOOSTER: return 300;
            case CARRIER: return 400;
        }return 1000;
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



