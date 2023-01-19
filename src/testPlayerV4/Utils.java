package testPlayerV4;

import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.WellInfo;

import java.util.Set;

public class Utils {

    static MapLocation getClosestWell(MapLocation currentLoc, WellInfo[] wells){
        int closestDist = Integer.MAX_VALUE;
        MapLocation closestWell = null;

        for(WellInfo well : wells){
            if(currentLoc.distanceSquaredTo(well.getMapLocation()) < closestDist){
                closestDist = currentLoc.distanceSquaredTo(well.getMapLocation());
                closestWell = well.getMapLocation();
            }
        }
        return closestWell;
    }


    static MapLocation nearestRobot (MapLocation currentLoc, RobotInfo[] robots){
        int closestDist = Integer.MAX_VALUE;
        MapLocation closestRobot = null;

        for(RobotInfo robot : robots){
            if(currentLoc.distanceSquaredTo(robot.location) < closestDist){
                closestDist = currentLoc.distanceSquaredTo(robot.location);
                closestRobot = robot.location;
            }
        }
        return closestRobot;
    }

    static MapLocation nearestLocation (MapLocation currentLoc, Set<MapLocation> locations){
        int closestDist = Integer.MAX_VALUE;
        MapLocation closestLocation = null;
        for(MapLocation loc: locations){
            if(currentLoc.distanceSquaredTo(loc) < closestDist){
                closestDist = currentLoc.distanceSquaredTo(loc);
                closestLocation = loc;
            }
        }
        return closestLocation;
    }
}
