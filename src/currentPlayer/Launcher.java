package currentPlayer;

import battlecode.common.*;

import javax.rmi.CORBA.Util;
import java.util.HashSet;
import java.util.Set;

public class Launcher extends Robot{
    public Launcher(RobotController r) {
        super(r);
    }
    Set<MapLocation> enemyIslandLocations = new HashSet<>();


    public void takeTurn() throws GameActionException {
        super.takeTurn();
        enemyIslandLocations = Comms.getTeamIslandLocations(rc.getTeam().opponent());

        if(nearbyEnemies.length > 0) {
            tryAttack();  // Try to attack someone
        }else if(enemyIslandLocations.size() > 0){ //try to capture enemy islands
            MapLocation closestEnemyIsland = enemyIslandLocations.iterator().next();
            Nav.goTo(closestEnemyIsland);
        } else explore();

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

}


