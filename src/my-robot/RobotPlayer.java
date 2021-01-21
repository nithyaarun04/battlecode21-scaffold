package MyRobot;
import battlecode.common.*;
import java.util.ArrayList;
import java.util.Collections;

public strictfp class RobotPlayer {
    static RobotController rc;

    static final RobotType[] spawnableRobot = {
        RobotType.POLITICIAN,
        RobotType.SLANDERER,
        RobotType.MUCKRAKER,
    };

    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    static int turnCount;

    static final double passabilityBound = 0.7;

    static Direction bugDirection = null;

    static double percentage = (Math.random()*(31)+5) / 100.0;

    static int currentVotes = 0;

    static ArrayList<Integer> muckrakersCreatedIDs = new ArrayList<Integer>();

    static ArrayList<Integer> politiciansAndSlanderersCreatedIDs = new ArrayList<Integer>();

    static ArrayList<MapLocation> nonFriendlyEnlightenmentCenterLocations = new ArrayList<MapLocation>();

    static ArrayList<MapLocation> enemyEnlightenmentCenterLocations = new ArrayList<MapLocation>();

    static ArrayList<MapLocation> enemyEnlightenmentCentersWithoutPlantsLocations = new ArrayList<MapLocation>();

    static ArrayList<Integer> infoToSend = new ArrayList<Integer>();

    static int parentID;

    static MapLocation target = null;

    static Direction toMove = null;

    static boolean move = true;

    static boolean isPlant = false;

    static final int plantInfluence = 10;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException 
    {
        if (!rc.getType().equals(RobotType.ENLIGHTENMENT_CENTER))
        {            
            for (RobotInfo robot : rc.senseNearbyRobots(2, rc.getTeam())) 
            {
                if (robot.type.equals(RobotType.ENLIGHTENMENT_CENTER)) 
                {
                    parentID = robot.ID;
                    break;
                }
            }
        }

        if (rc.getType().equals(RobotType.MUCKRAKER))
        {            
            if (rc.getInfluence() == plantInfluence)
            {
                isPlant = true;
            }
        }

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;

        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER: runEnlightenmentCenter(); break;
                    case POLITICIAN:           runPolitician();          break;
                    case SLANDERER:            runSlanderer();           break;
                    case MUCKRAKER:            runMuckraker();           break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runEnlightenmentCenter() throws GameActionException 
    {
        // CHECKING FLAGS

        for (int ID : muckrakersCreatedIDs)
        {
            if (rc.canGetFlag(ID) && rc.getFlag(ID) != 0)
            {
                MapLocation enlightenmentCenterLocation = getLocationFromFlag(ID);
                int loyalty = getLoyaltyFromFlag(ID);
                int ecTeam = getECTeamFromFlag(ID);

                if (loyalty == 1 && nonFriendlyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation))
                {
                    nonFriendlyEnlightenmentCenterLocations.remove(enlightenmentCenterLocation);
                    infoToSend.add(locationToSend(enlightenmentCenterLocation, loyalty, ecTeam));
                }

                else if (loyalty == 0 && !nonFriendlyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation))
                {
                    nonFriendlyEnlightenmentCenterLocations.add(enlightenmentCenterLocation);
                    infoToSend.add(locationToSend(enlightenmentCenterLocation, loyalty, ecTeam));
                }

                if (loyalty == 1 && enemyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation))
                {
                    enemyEnlightenmentCenterLocations.remove(enlightenmentCenterLocation);
                    infoToSend.add(locationToSend(enlightenmentCenterLocation, loyalty, ecTeam));
                }

                else if (loyalty == 0 && !enemyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation) && ecTeam == 0)
                {
                    enemyEnlightenmentCenterLocations.add(enlightenmentCenterLocation);
                    infoToSend.add(locationToSend(enlightenmentCenterLocation, loyalty, ecTeam));
                }
            }
        }

        for (int ID : politiciansAndSlanderersCreatedIDs)
        {
            if (rc.canGetFlag(ID) && rc.getFlag(ID) != 0)
            {
                MapLocation enlightenmentCenterLocation = getLocationFromFlag(ID);
                int loyalty = getLoyaltyFromFlag(ID);
                int ecTeam = getECTeamFromFlag(ID);

                if (loyalty == 1 && nonFriendlyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation))
                {
                    nonFriendlyEnlightenmentCenterLocations.remove(enlightenmentCenterLocation);
                    infoToSend.add(locationToSend(enlightenmentCenterLocation, loyalty, ecTeam));
                }

                else if (loyalty == 0 && !nonFriendlyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation))
                {
                    nonFriendlyEnlightenmentCenterLocations.add(enlightenmentCenterLocation);
                    infoToSend.add(locationToSend(enlightenmentCenterLocation, loyalty, ecTeam));
                }

                if (loyalty == 1 && enemyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation))
                {
                    enemyEnlightenmentCenterLocations.remove(enlightenmentCenterLocation);
                    infoToSend.add(locationToSend(enlightenmentCenterLocation, loyalty, ecTeam));
                }

                else if (loyalty == 0 && !enemyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation) && ecTeam == 0)
                {
                    enemyEnlightenmentCenterLocations.add(enlightenmentCenterLocation);
                    infoToSend.add(locationToSend(enlightenmentCenterLocation, loyalty, ecTeam));
                }
            }
        }

        // CHECKING FOR PLANTS

        ArrayList<MapLocation> plants = new ArrayList<MapLocation>();

        for (int ID : muckrakersCreatedIDs)
        {
            if (rc.canGetFlag(ID) && rc.getFlag(ID) != 0)
            {
                MapLocation enlightenmentCenterLocation = getLocationFromFlag(ID);
                int isPlant = getPlantFromFlag(ID);

                if (isPlant == 1)
                {
                    plants.add(enlightenmentCenterLocation);
                }
            }
        }

        for (MapLocation loc : enemyEnlightenmentCenterLocations)
        {
            if (Collections.frequency(plants, loc) < 2 && !enemyEnlightenmentCentersWithoutPlantsLocations.contains(loc))
            {
                enemyEnlightenmentCentersWithoutPlantsLocations.add(loc);
                infoToSend.add(locationToSend(loc, 0, 0, 0));
            }

            else if (Collections.frequency(plants, loc) >= 2 && enemyEnlightenmentCentersWithoutPlantsLocations.contains(loc))
            {
                enemyEnlightenmentCentersWithoutPlantsLocations.remove(loc);
                infoToSend.add(locationToSend(loc, 0, 0, 1));
            }
        }

        // SETTING FLAGS

        if (infoToSend.size() > 0 && rc.canSetFlag(infoToSend.get(0)))
        {
            rc.setFlag(infoToSend.get(0));
            infoToSend.remove(infoToSend.get(0));
        }

        else
        {
            if (rc.canSetFlag(0))
            {
                rc.setFlag(0);
            }
        }

        // CREATING ROBOTS

        double random = Math.random();
        RobotType toBuild = null;
        int influence = 0;

        if (rc.getRoundNum() < 200)
        {
            if (random < 0.4 && rc.getInfluence() >= 42)
            {
                toBuild = RobotType.SLANDERER;
                if ((int) (0.5 * rc.getInfluence()) <= 40)
                {
                    influence = 21; // Generates 1 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 62)
                {
                    influence = 41; // Generates 2 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 84)
                {
                    influence = 63; // Generates 3 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 106)
                {
                    influence = 85; // Generates 4 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 129)
                {
                    influence = 107; // Generates 5 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 153)
                {
                    influence = 130; // Generates 6 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 177)
                {
                    influence = 154; // Generates 7 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 202)
                {
                    influence = 178; // Generates 8 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 227)
                {
                    influence = 203; // Generates 9 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 254)
                {
                    influence = 228; // Generates 10 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 281)
                {
                    influence = 255; // Generates 11 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 309)
                {
                    influence = 282; // Generates 12 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 338)
                {
                    influence = 310; // Generates 13 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 367)
                {
                    influence = 339; // Generates 14 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 398)
                {
                    influence = 368; // Generates 15 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 430)
                {
                    influence = 399; // Generates 16 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 462)
                {
                    influence = 431; // Generates 17 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 496)
                {
                    influence = 463; // Generates 18 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 531)
                {
                    influence = 497; // Generates 19 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 567)
                {
                    influence = 532; // Generates 20 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 604)
                {
                    influence = 568; // Generates 21 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 642)
                {
                    influence = 605; // Generates 22 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 682)
                {
                    influence = 643; // Generates 23 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 723)
                {
                    influence = 683; // Generates 24 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 765)
                {
                    influence = 724; // Generates 25 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 809)
                {
                    influence = 766; // Generates 26 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 854)
                {
                    influence = 810; // Generates 27 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 901)
                {
                    influence = 855; // Generates 28 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 948)
                {
                    influence = 902; // Generates 29 influence per round
                }
                else
                {
                    influence = 949; // Generates 30 influence per round
                }
            }
            else
            {
                toBuild = RobotType.POLITICIAN;
                influence = 100;
            }
        }

        else
        {
            if (random < 0.2)
            {
                toBuild = RobotType.POLITICIAN;
                influence = (int) (0.2 * rc.getInfluence());
            }

            if (random < 0.45 && rc.getInfluence() >= 42)
            {
                toBuild = RobotType.SLANDERER;
                if ((int) (0.5 * rc.getInfluence()) <= 40)
                {
                    influence = 21; // Generates 1 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 62)
                {
                    influence = 41; // Generates 2 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 84)
                {
                    influence = 63; // Generates 3 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 106)
                {
                    influence = 85; // Generates 4 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 129)
                {
                    influence = 107; // Generates 5 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 153)
                {
                    influence = 130; // Generates 6 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 177)
                {
                    influence = 154; // Generates 7 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 202)
                {
                    influence = 178; // Generates 8 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 227)
                {
                    influence = 203; // Generates 9 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 254)
                {
                    influence = 228; // Generates 10 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 281)
                {
                    influence = 255; // Generates 11 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 309)
                {
                    influence = 282; // Generates 12 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 338)
                {
                    influence = 310; // Generates 13 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 367)
                {
                    influence = 339; // Generates 14 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 398)
                {
                    influence = 368; // Generates 15 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 430)
                {
                    influence = 399; // Generates 16 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 462)
                {
                    influence = 431; // Generates 17 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 496)
                {
                    influence = 463; // Generates 18 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 531)
                {
                    influence = 497; // Generates 19 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 567)
                {
                    influence = 532; // Generates 20 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 604)
                {
                    influence = 568; // Generates 21 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 642)
                {
                    influence = 605; // Generates 22 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 682)
                {
                    influence = 643; // Generates 23 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 723)
                {
                    influence = 683; // Generates 24 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 765)
                {
                    influence = 724; // Generates 25 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 809)
                {
                    influence = 766; // Generates 26 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 854)
                {
                    influence = 810; // Generates 27 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 901)
                {
                    influence = 855; // Generates 28 influence per round
                }
                else if ((int) (0.5 * rc.getInfluence()) <= 948)
                {
                    influence = 902; // Generates 29 influence per round
                }
                else
                {
                    influence= 949; // Generates 30 influence per round
                }
            }

            else
            {
                toBuild = RobotType.MUCKRAKER;
                influence = 1;
            }
        }

        for (Direction dir : directions)
        {
            if (rc.canBuildRobot(toBuild, dir, influence))
            {
                rc.buildRobot(toBuild, dir, influence);
                if (toBuild.equals(RobotType.MUCKRAKER))
                {
                    RobotInfo[] nearbyRobots = rc.senseNearbyRobots(2,rc.getTeam());
                    for (RobotInfo robot : nearbyRobots)
                    {
                        if (robot.location.equals(rc.getLocation().add(dir)))
                        {
                            muckrakersCreatedIDs.add(robot.ID);
                            break;
                        }
                    }
                }

                else
                {
                    RobotInfo[] nearbyRobots = rc.senseNearbyRobots(2,rc.getTeam());
                    for (RobotInfo robot : nearbyRobots)
                    {
                        if (robot.location.equals(rc.getLocation().add(dir)))
                        {
                            politiciansAndSlanderersCreatedIDs.add(robot.ID);
                            break;
                        }
                    }
                }
                break;
            }
        }

        // BIDDING

        int maxBid = 50;
        boolean lastBidMax = false;

        if (rc.getRoundNum() >= 50)
        {
            if (currentVotes == rc.getTeamVotes()) // Lost or tied the previous round
            {
                if (percentage+0.025 < 0.8)
                {
                    percentage += 0.025;
                }

                else
                {
                    percentage = 0.8;
                }
            }

            if (currentVotes == rc.getTeamVotes() && lastBidMax)
            {
                maxBid += 5;
                lastBidMax = false;
            }
        
            else // Won the previous round
            {
                if (percentage-0.005 > 0.002)
                {
                    percentage -= 0.005;
                }

                else
                {
                    percentage = 0.002;
                }
            }

            currentVotes = rc.getTeamVotes();

            if (rc.canBid((int) Math.ceil(rc.getInfluence()*percentage)) && (int) Math.ceil(rc.getInfluence()*percentage) < maxBid)
            {
                rc.bid((int) Math.ceil(rc.getInfluence()*percentage));
            }

            else if (rc.canBid(maxBid))
            {
                rc.bid(maxBid);
                lastBidMax = true;
            }
        }
    }

    static void runPolitician() throws GameActionException 
    {
        // CHECKING AND SETTING FLAGS 

        int actionRadius = rc.getType().actionRadiusSquared;

        if (rc.canGetFlag(parentID) && rc.getFlag(parentID) != 0)
        {
            MapLocation enlightenmentCenterLocation = getLocationFromFlag(parentID);
            int loyalty = getLoyaltyFromFlag(parentID);
            int ecTeam = getECTeamFromFlag(parentID);

            if (loyalty == 1 && nonFriendlyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation))
            {
                nonFriendlyEnlightenmentCenterLocations.remove(enlightenmentCenterLocation);
            }

            else if (loyalty == 0 && !nonFriendlyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation))
            {
                nonFriendlyEnlightenmentCenterLocations.add(enlightenmentCenterLocation);
            }

            if (loyalty == 1 && enemyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation))
            {
                enemyEnlightenmentCenterLocations.remove(enlightenmentCenterLocation);
            }

            else if (loyalty == 0 && !enemyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation) && ecTeam == 0)
            {
                enemyEnlightenmentCenterLocations.add(enlightenmentCenterLocation);
            }
        }

        boolean setFlag = false;

        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius))
        {
            if (robot.type.equals(RobotType.ENLIGHTENMENT_CENTER) && robot.team != rc.getTeam() && !nonFriendlyEnlightenmentCenterLocations.contains(robot.location))
            {
                if (robot.team == rc.getTeam().opponent() && rc.canSetFlag(locationToSend(robot.location, 0, 0)))
                {
                    rc.setFlag(locationToSend(robot.location, 0, 0));
                }

                else
                {
                    if (rc.canSetFlag(locationToSend(robot.location, 0, 1)))
                    {
                        rc.setFlag(locationToSend(robot.location, 0, 1));
                    }
                }

                setFlag = true;
            }
        }

        for (MapLocation loc : nonFriendlyEnlightenmentCenterLocations)
        {
            if (rc.canSenseLocation(loc) && getRobotAtLocation(loc).team == rc.getTeam() && rc.canSetFlag(locationToSend(loc, 1, 0)))
            {
                rc.setFlag(locationToSend(loc, 1, 0));
                setFlag = true;
            }
        }

        if (!setFlag && rc.canSetFlag(0))
        {
            rc.setFlag(0);
        }

        // GIVING SPEECHES

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(actionRadius);

        for (RobotInfo a : nearbyRobots)
        {
            if (a.type.equals(RobotType.ENLIGHTENMENT_CENTER) && a.getTeam() != rc.getTeam())
            {
                if (rc.canEmpower(actionRadius))
                {
                    rc.empower(actionRadius);
                }
            }
        }

        // MOVING TOWARDS TARGET
        
        if ((target == null || !enemyEnlightenmentCentersWithoutPlantsLocations.contains(target)) && enemyEnlightenmentCentersWithoutPlantsLocations.size() != 0)
        {
            target = enemyEnlightenmentCentersWithoutPlantsLocations.get((int) (Math.random()*enemyEnlightenmentCentersWithoutPlantsLocations.size()));
        }

        else if (!enemyEnlightenmentCentersWithoutPlantsLocations.contains(target))
        {
            target = null;
        }

        if (target != null)
        {
            basicBug(target);
        }

        // MOVING IN A RANDOM DIRECTION UNTIL HIT A WALL

        if (target == null && rc.getCooldownTurns() < 1 && move)
        {
            ArrayList<Direction> possibleDirections;

            if (toMove == null)
            {
                toMove = randomDirection();
            }

            possibleDirections = new ArrayList<Direction>();

            if (rc.canMove(toMove))
            {
                possibleDirections.add(toMove);
            }

            if (rc.canMove(toMove.rotateLeft()))
            {
                possibleDirections.add(toMove.rotateLeft());
            }

            if (rc.canMove(toMove.rotateRight()))
            {
                possibleDirections.add(toMove.rotateLeft());
            }

            while (possibleDirections.size() == 0 || !rc.onTheMap(rc.getLocation().add(toMove)))
            {
                toMove = randomDirection();

                possibleDirections = new ArrayList<Direction>();

                if (rc.canMove(toMove))
                {
                    possibleDirections.add(toMove);
                }

                if (rc.canMove(toMove.rotateLeft()))
                {
                    possibleDirections.add(toMove.rotateLeft());
                }

                if (rc.canMove(toMove.rotateRight()))
                {
                    possibleDirections.add(toMove.rotateLeft());
                }
            }

            Direction actualMove = possibleDirections.get((int) (Math.random() * possibleDirections.size()));

            for (Direction dir : possibleDirections)
            {
                if (rc.canSenseLocation(rc.getLocation().add(dir)) && rc.canSenseLocation(rc.getLocation().add(actualMove)) && rc.sensePassability(rc.getLocation().add(dir)) > rc.sensePassability(rc.getLocation().add(actualMove)))
                {
                    actualMove = dir;
                }
            }

            if (rc.canMove(actualMove))
            {
                rc.move(actualMove);
            }
        }
    }

    static void runSlanderer() throws GameActionException 
    {
        // CHECKING AND SETTING FLAGS 

        int actionRadius = rc.getType().actionRadiusSquared;

        if (rc.canGetFlag(parentID) && rc.getFlag(parentID) != 0)
        {
            MapLocation enlightenmentCenterLocation = getLocationFromFlag(parentID);
            int loyalty = getLoyaltyFromFlag(parentID);
            int ecTeam = getECTeamFromFlag(parentID);

            if (loyalty == 1 && nonFriendlyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation))
            {
                nonFriendlyEnlightenmentCenterLocations.remove(enlightenmentCenterLocation);
            }

            else if (loyalty == 0 && !nonFriendlyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation))
            {
                nonFriendlyEnlightenmentCenterLocations.add(enlightenmentCenterLocation);
            }

            if (loyalty == 1 && enemyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation))
            {
                enemyEnlightenmentCenterLocations.remove(enlightenmentCenterLocation);
            }

            else if (loyalty == 0 && !enemyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation) && ecTeam == 0)
            {
                enemyEnlightenmentCenterLocations.add(enlightenmentCenterLocation);
            }
        }

        boolean setFlag = false;

        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius))
        {
            if (robot.type.equals(RobotType.ENLIGHTENMENT_CENTER) && robot.team != rc.getTeam() && !nonFriendlyEnlightenmentCenterLocations.contains(robot.location))
            {
                if (robot.team == rc.getTeam().opponent() && rc.canSetFlag(locationToSend(robot.location, 0, 0)))
                {
                    rc.setFlag(locationToSend(robot.location, 0, 0));
                }

                else
                {
                    if (rc.canSetFlag(locationToSend(robot.location, 0, 1)))
                    {
                        rc.setFlag(locationToSend(robot.location, 0, 1));
                    }
                }

                setFlag = true;
            }
        }

        for (MapLocation loc : nonFriendlyEnlightenmentCenterLocations)
        {
            if (rc.canSenseLocation(loc) && getRobotAtLocation(loc).team == rc.getTeam() && rc.canSetFlag(locationToSend(loc, 1, 0)))
            {
                rc.setFlag(locationToSend(loc, 1, 0));
                setFlag = true;
            }
        }

        if (!setFlag && rc.canSetFlag(0))
        {
            rc.setFlag(0);
        }

        // MOVING IN THE DIRECTION WITH GREATEST PASSABILITY

        Direction[] possibleDirections = new Direction[8];
        int index1 = 0;

        for (Direction dir : directions)
        {
            if (rc.canMove(dir))
            {
                possibleDirections[index1] = dir;
                index1++;
            }
        }

        Direction[] possiblePassableDirections = new Direction[8];
        int index2 = 0;

        for (Direction dir : possibleDirections)
        {
            if (dir != null)
            {
                if (rc.sensePassability(rc.getLocation().add(dir)) >= passabilityBound)
                {
                    possiblePassableDirections[index2] = dir;
                    index2++;
                }
            }

            else
            {
                break;
            }
        }

        if (possiblePassableDirections[0] != null)
        {
            rc.move(possiblePassableDirections[(int) (Math.random()*index2)]);
        }

        else if (possibleDirections[0] != null)
        {
            rc.move(possibleDirections[(int) (Math.random()*index1)]);
        }
    }

    static void runMuckraker() throws GameActionException 
    {
        // CHECKING AND SETTING FLAGS 

        int actionRadius = rc.getType().actionRadiusSquared;

        if (rc.canGetFlag(parentID) && rc.getFlag(parentID) != 0)
        {
            MapLocation enlightenmentCenterLocation = getLocationFromFlag(parentID);
            int loyalty = getLoyaltyFromFlag(parentID);
            int ecTeam = getECTeamFromFlag(parentID);
            int plant = getPlantFromFlag(parentID);

            if (loyalty == 1 && nonFriendlyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation))
            {
                nonFriendlyEnlightenmentCenterLocations.remove(enlightenmentCenterLocation);
            }

            else if (loyalty == 0 && !nonFriendlyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation))
            {
                nonFriendlyEnlightenmentCenterLocations.add(enlightenmentCenterLocation);
            }

            if (loyalty == 1 && enemyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation))
            {
                enemyEnlightenmentCenterLocations.remove(enlightenmentCenterLocation);
            }

            else if (loyalty == 0 && !enemyEnlightenmentCenterLocations.contains(enlightenmentCenterLocation) && ecTeam == 0)
            {
                enemyEnlightenmentCenterLocations.add(enlightenmentCenterLocation);
            }

            if (plant == 1 && !enemyEnlightenmentCentersWithoutPlantsLocations.contains(enlightenmentCenterLocation))
            {
                enemyEnlightenmentCentersWithoutPlantsLocations.add(enlightenmentCenterLocation);
            }

            if (plant == 0 && enemyEnlightenmentCentersWithoutPlantsLocations.contains(enlightenmentCenterLocation))
            {
                enemyEnlightenmentCentersWithoutPlantsLocations.remove(enlightenmentCenterLocation);
            }
        }

        boolean setFlag = false;

        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius))
        {
            if (robot.type.equals(RobotType.ENLIGHTENMENT_CENTER) && robot.team != rc.getTeam() && !nonFriendlyEnlightenmentCenterLocations.contains(robot.location))
            {
                if (robot.team == rc.getTeam().opponent() && rc.canSetFlag(locationToSend(robot.location, 0, 0)))
                {
                    rc.setFlag(locationToSend(robot.location, 0, 0));
                }

                else
                {
                    if (rc.canSetFlag(locationToSend(robot.location, 0, 1)))
                    {
                        rc.setFlag(locationToSend(robot.location, 0, 1));
                    }
                }

                setFlag = true;
            }
        }

        for (MapLocation loc : nonFriendlyEnlightenmentCenterLocations)
        {
            if (rc.canSenseLocation(loc) && getRobotAtLocation(loc).team == rc.getTeam() && rc.canSetFlag(locationToSend(loc, 1, 0)))
            {
                rc.setFlag(locationToSend(loc, 1, 0));
                setFlag = true;
            }
        }

        if (!setFlag && rc.canSetFlag(0))
        {
            rc.setFlag(0);
        }

        // EXPOSING ENEMY SLANDERERS

        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, rc.getTeam().opponent())) 
        {
            if (robot.type.canBeExposed()) 
            {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) 
                {
                    rc.expose(robot.location);
                    return;
                }
            }
        }

        // MOVING TOWARDS TARGET

        if (isPlant)
        {
            System.out.println("I'm a plant");

            if ((target == null || !enemyEnlightenmentCentersWithoutPlantsLocations.contains(target)) && enemyEnlightenmentCentersWithoutPlantsLocations.size() != 0)
            {
                target = enemyEnlightenmentCentersWithoutPlantsLocations.get((int) (Math.random()*enemyEnlightenmentCentersWithoutPlantsLocations.size()));
            }

            else if (!enemyEnlightenmentCentersWithoutPlantsLocations.contains(target))
            {
                target = null;
            }

            if (rc.getLocation().distanceSquaredTo(target) < actionRadius && rc.canSetFlag(locationToSend(target, 0, 0, 1)))
            {
                move = false;
                rc.setFlag(locationToSend(target, 0, 0, 1));
            }

            if (target != null && move)
            {
                basicBug(target);
            }
        }

        // MOVING IN A RANDOM DIRECTION UNTIL HIT A WALL

        if (target == null && rc.getCooldownTurns() < 1 && move)
        {
            ArrayList<Direction> possibleDirections;

            if (toMove == null)
            {
                toMove = randomDirection();
            }

            possibleDirections = new ArrayList<Direction>();

            if (rc.canMove(toMove))
            {
                possibleDirections.add(toMove);
            }

            if (rc.canMove(toMove.rotateLeft()))
            {
                possibleDirections.add(toMove.rotateLeft());
            }

            if (rc.canMove(toMove.rotateRight()))
            {
                possibleDirections.add(toMove.rotateLeft());
            }

            while (possibleDirections.size() == 0 || !rc.onTheMap(rc.getLocation().add(toMove)))
            {
                toMove = randomDirection();

                possibleDirections = new ArrayList<Direction>();

                if (rc.canMove(toMove))
                {
                    possibleDirections.add(toMove);
                }

                if (rc.canMove(toMove.rotateLeft()))
                {
                    possibleDirections.add(toMove.rotateLeft());
                }

                if (rc.canMove(toMove.rotateRight()))
                {
                    possibleDirections.add(toMove.rotateLeft());
                }
            }

            Direction actualMove = possibleDirections.get((int) (Math.random() * possibleDirections.size()));

            for (Direction dir : possibleDirections)
            {
                if (rc.canSenseLocation(rc.getLocation().add(dir)) && rc.canSenseLocation(rc.getLocation().add(actualMove)) && rc.sensePassability(rc.getLocation().add(dir)) > rc.sensePassability(rc.getLocation().add(actualMove)))
                {
                    actualMove = dir;
                }
            }

            if (rc.canMove(actualMove))
            {
                rc.move(actualMove);
            }
        }
    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random spawnable RobotType
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    static RobotInfo getRobotAtLocation(MapLocation location) throws GameActionException
    {
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(actionRadius);
        
        for (RobotInfo robot : nearbyRobots)
        {
            if (robot.location.equals(location))
            {
                return robot;
            }
        }

        // Location is out of sensing range or there is no robot at that location
        throw new GameActionException(GameActionExceptionType.CANT_SENSE_THAT, "Location out of sensing range or location is unoccupied");
    }

    ////////////////////////////////////////////////////////////////////////////
    // Pathfinding Lecture Code

    static final int NBITS = 7;
    static final int BITMASK = (1 << NBITS) - 1;

    static int locationToSend(MapLocation location) throws GameActionException {
        int x = location.x, y = location.y;
        int encodedLocation = ((x & BITMASK) << NBITS) + (y & BITMASK);
        
        return encodedLocation;
    }

    @SuppressWarnings("unused")
    static int locationToSend(MapLocation location, int loyalty, int ecTeam) throws GameActionException {
        int x = location.x, y = location.y;
        int encodedLocation = (ecTeam << (2*NBITS + 1)) + (loyalty << (2*NBITS)) + ((x & BITMASK) << NBITS) + (y & BITMASK);
        
        return encodedLocation;
    }

    @SuppressWarnings("unused")
    static int locationToSend(MapLocation location, int loyalty, int ecTeam, int plant) throws GameActionException {
        int x = location.x, y = location.y;
        int encodedLocation = (ecTeam << (2*NBITS + 2)) + (ecTeam << (2*NBITS + 1)) + (loyalty << (2*NBITS)) + ((x & BITMASK) << NBITS) + (y & BITMASK);
        
        return encodedLocation;
    }

    static MapLocation getLocationFromFlag(int flag) {
        int y = flag & BITMASK;
        int x = (flag >> NBITS) & BITMASK;
        // int extraInformation = flag >> (2*NBITS);

        MapLocation currentLocation = rc.getLocation();
        int offsetX128 = currentLocation.x >> NBITS;
        int offsetY128 = currentLocation.y >> NBITS;
        MapLocation actualLocation = new MapLocation((offsetX128 << NBITS) + x, (offsetY128 << NBITS) + y);

        // You can probably code this in a neater way, but it works
        MapLocation alternative = actualLocation.translate(-(1 << NBITS), 0);
        if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }
        alternative = actualLocation.translate(1 << NBITS, 0);
        if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }
        alternative = actualLocation.translate(0, -(1 << NBITS));
        if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }
        alternative = actualLocation.translate(0, 1 << NBITS);
        if (rc.getLocation().distanceSquaredTo(alternative) < rc.getLocation().distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }
        return actualLocation;
    }

    static int getLoyaltyFromFlag(int flag) 
    {
        // 0 if EC is nonfriendly and 1 if EC is friendly
        return (flag >> 14) & 1;
    }

    static int getECTeamFromFlag(int flag) 
    {
        // 0 if EC is enemy and 1 if EC is neutral
        return (flag >> 15) & 1;
    }

    static int getPlantFromFlag(int flag) 
    {
        // 0 if EC is not plant and 1 if EC is plant
        return (flag >> 16) & 1;
    }

    ////////////////////////////////////////////////////////////////////////////
    // BASIC BUG - just follow the obstacle while it's in the way
    //             not the best bug, but works for "simple" obstacles
    //             for better bugs, think about Bug 2!

    static final double passabilityThreshold = 0.7;

    static void basicBug(MapLocation target) throws GameActionException {
        Direction d = rc.getLocation().directionTo(target);
        if (rc.getLocation().equals(target)) {
            // do something else, now that you're there
            // here we'll just explode
            if (rc.canEmpower(1)) {
                rc.empower(1);
            }
        } else if (rc.isReady()) {
            if (rc.canMove(d) && rc.sensePassability(rc.getLocation().add(d)) >= passabilityThreshold) {
                rc.move(d);
                bugDirection = null;
            } else {
                if (bugDirection == null) {
                    bugDirection = d;
                }
                for (int i = 0; i < 8; ++i) {
                    if (rc.canMove(bugDirection) && rc.sensePassability(rc.getLocation().add(bugDirection)) >= passabilityThreshold) {
                        rc.setIndicatorDot(rc.getLocation().add(bugDirection), 0, 255, 255);
                        rc.move(bugDirection);
                        bugDirection = bugDirection.rotateLeft();
                        break;
                    }
                    rc.setIndicatorDot(rc.getLocation().add(bugDirection), 255, 0, 0);
                    bugDirection = bugDirection.rotateRight();
                }
            }
        }
    }
}
