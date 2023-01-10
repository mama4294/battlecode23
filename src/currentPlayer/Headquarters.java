package currentPlayer;
import battlecode.common.*;



public class Headquarters extends Robot {

    public Headquarters(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {

        //try to build an anchor
        tryBuildAnchor();

        //try to build a carrier
        tryBuild(RobotType.CARRIER);

    }

    public boolean tryBuild(RobotType type) throws GameActionException {
        if(rc.getResourceAmount(ResourceType.ADAMANTIUM) < type.buildCostAdamantium || rc.getResourceAmount(ResourceType.MANA) < type.buildCostMana || rc.getResourceAmount(ResourceType.ELIXIR) < type.buildCostElixir)  return false;
        MapLocation currentLocation = rc.getLocation();
        for(int i = 0; i < directions.length; i++) {
            Direction dir = directions[i];
            if (rc.canBuildRobot(type, currentLocation.add(dir))) {
                rc.buildRobot(type, currentLocation.add(dir));
                return true;
            }
        }
        return false;
    }

    public boolean tryBuildAnchor() throws GameActionException {
        if(rc.getResourceAmount(ResourceType.ADAMANTIUM) < Anchor.STANDARD.adamantiumCost || rc.getResourceAmount(ResourceType.MANA) > Anchor.STANDARD.manaCost) return false;
        MapLocation currentLocation = rc.getLocation();
        for(int i = 0; i < directions.length; i++) {
            Direction dir = directions[i];
            if (rc.canBuildAnchor(Anchor.STANDARD)) {
                rc.buildAnchor(Anchor.STANDARD);
                return true;
            }
        }
        return false;
    }
        

}
