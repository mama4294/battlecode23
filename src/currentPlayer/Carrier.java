package currentPlayer;
import battlecode.common.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Carrier extends Robot {

    int heldWeight = 0;
    boolean isFull = false;

    ResourceType myResourceType = null;

    MapLocation targetWell;

    State state = State.EXPLORE;

    enum State {

        MINE,
        EXPLORE,
        RETURN,
        ANCHORDELIVER,
    }

    public Carrier(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        heldWeight = getHeldWeight();
        isFull = heldWeight >= 39;
        getMinerType();   //Sets myResourceType (Adamantium, Mana, Elixir)
        findWell();       //Sets targetWell if null
        tryChangeState(); //Ex: change from an Explorer to a Miner if there is a well nearby
        runStateAction(); //Ex: if you are a Miner, mine
    }

    public void runStateAction() throws GameActionException{
        switch (state){
            case MINE:
                if(targetWell != null) {
                    Debug.setString(myResourceType + " Miner: Moving to well at: " + targetWell);
                    collectFromWell(targetWell);
                    Nav.goTo(targetWell);
                }else{
                    Nav.moveRandomly();
                    Debug.setString(myResourceType + " Miner: Moving randomly");
                }
                break;
            case EXPLORE:
                explore();
                Debug.setString(myResourceType + " EXPlORER: exploring to " + explorationTarget);
                break;
            case RETURN:
                Debug.setString(myResourceType + " RETURNER: Delivering paylod to " + homeHQ);
                tryTransferToHomeHQ();
                boolean isAdjecentToHomeHQ = rc.getLocation().isAdjacentTo(homeHQ);
                if(!isAdjecentToHomeHQ){
                    Nav.goTo(homeHQ);
                }
                break;
            case ANCHORDELIVER:
                Debug.setString("ANCHOR: taking anchor to island");
                if(rc.canTakeAnchor(homeHQ, Anchor.STANDARD)){
                    rc.takeAnchor(homeHQ, Anchor.STANDARD);
                }
                bringAnchorToIsland();
                break;
        }
    }

    public void findWell() throws GameActionException {
        if(targetWell == null && myResourceType != null){
            targetWell = getWellLoc(myResourceType);
        }
    }

    public void getMinerType() throws GameActionException {
        if(myResourceType == null){
            if(rc.getRoundNum() % 2 ==0 ){
                myResourceType = ResourceType.ADAMANTIUM;
            }else{
                myResourceType = ResourceType.MANA;
            }
        }
    }




    public void tryChangeState() throws GameActionException{

        switch (state){
            case MINE:
                if(isFull){
                    state = State.RETURN;
                }
                break;
            case EXPLORE:
                if(targetWell != null){
                    state = State.MINE;
                }
        }

        if(!isFull && rc.canTakeAnchor(homeHQ, Anchor.STANDARD)) state = State.ANCHORDELIVER;

    }

    public boolean collectFromWell(MapLocation wellLocation) throws GameActionException{
        if (rc.canCollectResource(wellLocation, -1)) {

                rc.collectResource(wellLocation, -1);
                Debug.setString("Collecting, now have, AD:" +
                        rc.getResourceAmount(ResourceType.ADAMANTIUM) +
                        " MN: " + rc.getResourceAmount(ResourceType.MANA) +
                        " EX: " + rc.getResourceAmount(ResourceType.ELIXIR));
            return true;

        }
        return false;
    }

    public boolean tryTransferToHomeHQ() throws GameActionException{
        if(homeHQ == null) return false;
        boolean transfered = false;
        Debug.setIndicatorDotGreen(rc.getLocation());

        int carriedAdmantium = rc.getResourceAmount(ResourceType.ADAMANTIUM);
        int carriedMana = rc.getResourceAmount(ResourceType.MANA);
        int carriedElixer = rc.getResourceAmount(ResourceType.ELIXIR);

        if(carriedAdmantium > 0 && rc.canTransferResource(homeHQ, ResourceType.ADAMANTIUM, carriedAdmantium)){
            Debug.setIndicatorDotYellow(homeHQ);
            rc.transferResource(homeHQ, ResourceType.ADAMANTIUM, carriedAdmantium);
            transfered = true;
        }
        if(carriedMana > 0 && rc.canTransferResource(homeHQ, ResourceType.MANA, carriedMana)){
            Debug.setIndicatorDotBlue(homeHQ);
            rc.transferResource(homeHQ, ResourceType.MANA, carriedMana);
            transfered = true;
        }
        if(carriedElixer > 0 && rc.canTransferResource(homeHQ, ResourceType.ELIXIR, carriedElixer)){
            Debug.setIndicatorDotGreen(homeHQ);
            rc.transferResource(homeHQ, ResourceType.ELIXIR, carriedElixer);
            transfered = true;
        }

        if(transfered && getHeldWeight() < 40) state = State.EXPLORE;
        return transfered;
    }

    public MapLocation getWellLoc(ResourceType resource) throws GameActionException{
        if(resource == null) return null;
        WellInfo[] wells = rc.senseNearbyWells(resource);
        if (wells.length > 0){
            return Utils.getClosestWell(rc.getLocation(), wells); //finds closest well
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

