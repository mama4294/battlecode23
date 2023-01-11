package testPlayerV1;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Launcher extends Robot {
    public Launcher(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        if(nearbyEnemies.length > 0) {
            tryAttack();  // Try to attack someone
        }else{
            explore();
        }
    }

    public void tryAttack() throws GameActionException {
        RobotInfo[] enemies = nearbyEnemies;

        if (enemies.length > 0) {
            MapLocation toAttack = Utils.nearestRobot(rc.getLocation(), enemies);
            if (rc.canAttack(toAttack)) {
                rc.attack(toAttack);
            }
            else{
                Nav.goTo(toAttack);
            }
        }
    }

}


