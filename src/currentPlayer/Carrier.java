package currentPlayer;
import battlecode.common.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Carrier extends Robot {

    int heldWeight = 0;
    boolean isFull = false;

    public Carrier(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        heldWeight = getHeldWeight();
        isFull = heldWeight == 40;
        bringAnchorToIsland();

        if(!isFull){
            MapLocation targetWell = getWellLoc();
            if(targetWell != null){
                collectFromWell(targetWell);
                Nav.goTo(targetWell);

            }else{
                Nav.moveRandomly();
            }
        }
        else{
            tryTransferToHomeHQ();
            boolean isAdjecentToHomeHQ = rc.getLocation().isAdjacentTo(homeHQ);
            if(!isAdjecentToHomeHQ){
                Nav.goTo(homeHQ);
            }else{
                Nav.moveRandomly();
            }
          Debug.setString("I'm full");
        }




    }

    public void collectFromWell(MapLocation wellLocation) throws GameActionException{
        if (rc.canCollectResource(wellLocation, -1)) {
                rc.collectResource(wellLocation, -1);
                Debug.setString("Collecting, now have, AD:" +
                        rc.getResourceAmount(ResourceType.ADAMANTIUM) +
                        " MN: " + rc.getResourceAmount(ResourceType.MANA) +
                        " EX: " + rc.getResourceAmount(ResourceType.ELIXIR));

        }
    }

    public void tryTransferToHomeHQ() throws GameActionException{
        if(rc.canTransferResource(homeHQ, ResourceType.ADAMANTIUM, rc.getResourceAmount(ResourceType.ADAMANTIUM))){
            rc.transferResource(homeHQ, ResourceType.ADAMANTIUM, rc.getResourceAmount(ResourceType.ADAMANTIUM));
        }
        if(rc.canTransferResource(homeHQ, ResourceType.MANA, rc.getResourceAmount(ResourceType.MANA))){
            rc.transferResource(homeHQ, ResourceType.MANA, rc.getResourceAmount(ResourceType.MANA));
        }
        if(rc.canTransferResource(homeHQ, ResourceType.ELIXIR, rc.getResourceAmount(ResourceType.ELIXIR))){
            rc.transferResource(homeHQ, ResourceType.ELIXIR, rc.getResourceAmount(ResourceType.ELIXIR));
        }
    }

    public MapLocation getWellLoc() throws GameActionException{
        WellInfo[] wells = rc.senseNearbyWells();
        if (wells.length > 1){
            return wells[1].getMapLocation();
        }
        return null;
    }


    public int getHeldWeight() throws GameActionException {
        return rc.getResourceAmount(ResourceType.ADAMANTIUM) + rc.getResourceAmount(ResourceType.MANA) + rc.getResourceAmount(ResourceType.ELIXIR);
    }

    public void bringAnchorToIsland() throws GameActionException{
        if (rc.getAnchor() != null) {
            // If I have an anchor singularly focus on getting it to the first island I see
            int[] islands = rc.senseNearbyIslands();
            Set<MapLocation> islandLocs = new HashSet<>();
            for (int id : islands) {
                MapLocation[] thisIslandLocs = rc.senseNearbyIslandLocations(id);
                islandLocs.addAll(Arrays.asList(thisIslandLocs));
            }
            if (islandLocs.size() > 0) {
                MapLocation islandLocation = islandLocs.iterator().next();
                Debug.setString("Moving my anchor towards " + islandLocation);
                while (!rc.getLocation().equals(islandLocation)) {
                    Direction dir = rc.getLocation().directionTo(islandLocation);
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                    }
                }
                if (rc.canPlaceAnchor()) {
                    Debug.setString("Huzzah, placed anchor!");
                    rc.placeAnchor();
                }
            }
        }
    }

}

