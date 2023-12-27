package learningBot;

import battlecode.common.*;
import learningBot.util.FastIterableLocSet;

public class Headquarter extends Unit {

  private static int carrierCnt = 0;

  public static FastIterableLocSet spawnableSet = null; // set by MapRecorder.hqInit()
  public static int sensablePassibleArea = 0; // set by MapRecorder.hqInit()
  public static int hqid;

  public static int lastRoundAnchorBuilt = -1000;
  public static int lastCongestedRound = -100;
  public static int lastEnemyRound = -100;
  public static boolean isCongested = false;

  private static RobotInfo closestEnemy;
  private static int strength;
  private static int friendlyCount = 0;
  private static int spawnableTileOccupied = 0;
  private static boolean canBuildCarrier, canBuildLauncher;
  private static int usableMN, usableAD, usableEL;

  private static boolean builtLauncher = false;

  private static MapLocation curLoc = rc.getLocation();

  public static void run() throws GameActionException {
    if (turnCount == 0) {
      // first turn all HQ report
      hqid = Comm.HQInit(rc.getLocation(), rc.getID());
      // for (WellInfo well : rc.senseNearbyWells()) {
      //   Comm.reportWells(
      //     well.getResourceType().resourceID,
      //     well.getMapLocation()
      //   );
      // }
      // Comm.commit_write();
      //      MapRecorder.hqInit();
    }
    if (hqid == 0) {
      // HQ 0 is in charge of cleaning up shared array
      // Comm.updateExpiredEnemy();
      // Comm.cleanupAmplifier();
      // if (rc.getRoundNum() % Comm.CARRIER_REPORT_FREQ == 0) {
      //   Comm.resetCarrierCnt();
      // }
    }
    // Comm.updateEnemy();

    usableMN = rc.getResourceAmount(ResourceType.MANA);
    usableAD = rc.getResourceAmount(ResourceType.ADAMANTIUM);
    usableEL = rc.getResourceAmount(ResourceType.ELIXIR);

    sense();

    canBuildLauncher = true;
    canBuildCarrier = true;

    tryBuildLauncher();
    tryBuildCarrier();

    // canBuildLauncher = true;
    // canBuildCarrier = true;
    // if (
    //   rc.getRoundNum() - lastEnemyRound > 5 &&
    //   rc.getRobotCount() / Comm.numHQ > 100
    // ) {
    //   canBuildLauncher = false;
    // }
    // if (rc.getRoundNum() > 1900 && rc.getRobotCount() / Comm.numHQ > 30) {
    //   canBuildLauncher = false;
    //   // this is mostly a stalemate, accumulate mana for tiebreaker and hope for the best
    // }
    // if (rc.getRoundNum() - lastEnemyRound <= 5 || Comm.isCongested()) {
    //   canBuildCarrier = false;
    // }
    // tryBuildAnchor();
    // tryBuildAmp();
    // tryBuildLauncher();
    // tryBuildCarrier();

    // Comm.commit_write();

    MapRecorder.initializeMapRecorder();

    if (turnCount <= 3) { // for symmetry check
      MapRecorder.recordSurroundings(500);
    }
  }

  private static void tryBuildLauncher() throws GameActionException {
    if (!builtLauncher) {
      trySpawn(RobotType.LAUNCHER, 1, 1);
      builtLauncher = true;
    }
  }

  private static void tryBuildCarrier() throws GameActionException {
    int maxCarrierSpawn = Math.min(5, usableAD / Constants.CARRIER_COST_AD);
    if (canBuildCarrier) {
      // do not spawn miner if enemies are close as miners getting killed will mess up spanw Q
      for (int i = maxCarrierSpawn; --i >= 0 && rc.isActionReady();) {
        trySpawn(RobotType.CARRIER, curLoc.x, curLoc.y);
      }
    }
  }

  private static void sense() throws GameActionException {
    carrierCnt = Comm.getCarrierCnt();
    closestEnemy = null;
    friendlyCount = 0;
    spawnableTileOccupied = 0;
    strength = 0;
    int dis = Integer.MAX_VALUE;
    for (RobotInfo robot : rc.senseNearbyRobots(-1)) {
      if (robot.team == oppTeam) {
        switch (robot.type) {
          case LAUNCHER:
          case DESTABILIZER:
            lastEnemyRound = rc.getRoundNum();
            strength -= robot.health;
            int newDis = rc.getLocation().distanceSquaredTo(robot.location);
            if (newDis < dis) {
              closestEnemy = robot;
              dis = newDis;
            }
        }
      } else {
        friendlyCount++;
        switch (robot.type) {
          case LAUNCHER:
          case DESTABILIZER:
            strength += robot.health;
        }
      }
    }

    if (closestEnemy != null) {
      // seeing enemy immediately decongests
      isCongested = false;
      lastCongestedRound = -100;
      lastEnemyRound = rc.getRoundNum();
    }
  }

  private static MapLocation trySpawn(RobotType robotType, double x, double y)
    throws GameActionException {
    MapLocation currentLocation = rc.getLocation();
    for (int i = 0; i < Constants.directions.length; i++) {
      Direction dir = Constants.directions[i];
      MapLocation bestSpawn = currentLocation.add(dir);
      if (rc.canBuildRobot(robotType, bestSpawn)) {
        rc.buildRobot(robotType, bestSpawn);
        return bestSpawn;
      }
    }
    return null;
  }
}
