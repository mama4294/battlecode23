package learningBot;

import battlecode.common.*;
import learningBot.util.FastIterableLocSet;

public class Carrier extends Unit {

  // state constants
  public static final int MINING = 10;
  public static final int DROPPING_RESOURCE = 11;
  public static final int REPORT_AND_RUNAWAY = 20;
  public static final int RUNAWAY = 21;
  public static final int SCOUTING = 30;
  public static final int REPORTING_INFO = 31;
  public static final int ANCHORING = 40;

  public static int state = 0;

  // carriers try to report every 100 turns and get their ID
  public static int lastCarrierReportRound = -1000;
  public static int carrierID = 0;

  public static ResourceType miningResourceType;
  public static MapLocation miningWellLoc;
  public static MapLocation miningHQLoc;

  public static MapLocation lastEnemyLoc = null;
  public static int lastEnemyRound = 0;
  public static RobotInfo closestEnemy = null;

  public static FastIterableLocSet congestedMines = new FastIterableLocSet(290);
  // public static FastLocIntMap lastEnemyOnMine = new FastLocIntMap();

  // scouting vars
  static int startHQID;
  static MapLocation startHQLoc;
  static int scoutStartRound;
  static MapLocation scoutTarget = null;
  static MapLocation scoutCenter = null;
  static double scoutAngle = 0;
  public static FastIterableLocSet[] wellsSeen = { null, null, null };
  static FastIterableLocSet[] wellsToReport = { null, null, null };
  // for islands only report location
  static MapLocation[] islandLocations = new MapLocation[GameConstants.MAX_NUMBER_ISLANDS +
  1];
  static int[] islandsToReport = new int[GameConstants.MAX_NUMBER_ISLANDS];
  static int islandReportIndex = -1;

  static void run() throws GameActionException {
    if (turnCount == 0) {}

    indicator += String.format("S%dR%s,", state, miningResourceType);
  }
}
