package currentPlayer;

import battlecode.common.MapLocation;
import battlecode.common.WellInfo;

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
}
