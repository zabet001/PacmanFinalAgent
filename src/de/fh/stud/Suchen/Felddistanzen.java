package de.fh.stud.Suchen;

import de.fh.pacman.GhostInfo;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.Suchen.Suchfunktionen.CallbackFunktionen;
import de.fh.stud.Suchen.Suchfunktionen.Zugangsfilter;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class Felddistanzen {

    // Optimierungsidee: Momentan noch alle Distanzen doppelt gespeichert ([9][9][1][1]==[1][1][9][9])
    private static short[][][][] distanceMap;
    private static short MAX_DISTANCE;

    public static void initDistances(PacmanTileType[][] world) {
        AtomicReference<Short> maxDist = new AtomicReference<>((short) 0);

        distanceMap = new short[world.length][world[0].length][][];
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world[0].length; j++) {
                if (world[i][j] != PacmanTileType.WALL) {
                    distanceMap[i][j] = allDistances(world, i, j, i1 -> {
                        if (i1 > maxDist.get()) {
                            maxDist.set(i1);
                        }
                    });
                }
            }
        }

        MAX_DISTANCE = maxDist.get();

    }

    private static short[][] allDistances(PacmanTileType[][] world, int fieldX, int fieldY, Consumer<Short> callback) {
        short[][] distancesForThisPos = new short[world.length][world[0].length];

        Suche writeDistances = new Suche(false, Zugangsfilter.noWall(), true, null, null,
                                         CallbackFunktionen.saveStepCost(distancesForThisPos),
                                         expCand -> callback.accept(expCand.getCost()));
        writeDistances.start(world, fieldX, fieldY, Suche.SearchStrategy.BREADTH_FIRST, false);

        return distancesForThisPos;
    }

    public static short calMaxDistance() {
        short max = 0;
        for (int i = 0; i < distanceMap.length; i++)
            for (int j = 0; j < distanceMap[0].length; j++)
                if (distanceMap[i][j] != null) {
                    if (calMaxDistance(i, j) > max) {
                        max = calMaxDistance(i, j);
                    }
                }

        return max;
    }

    public static short calMaxDistance(int posX, int posY) {
        short max = 0;
        for (int i = 0; i < distanceMap.length; i++) {
            for (int j = 0; j < distanceMap[0].length; j++) {
                if (distanceMap[posX][posY][i][j] > max) {
                    max = distanceMap[posX][posY][i][j];
                }

            }
        }
        return max;
    }

    public static void printAllDistances(PacmanTileType[][] world) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < distanceMap[0].length; i++) {
            for (int j = 0; j < distanceMap.length; j++) {
                // ausgabe:
                if (world[j][i] != PacmanTileType.WALL) {
                    ret.append(String.format("!! Distanzmap fuer Position: [%d;%d] !!\n", j, i));
                    ret.append(printAllDistances(world, j, i));
                    ret.append("\n");
                }

            }
        }
        System.out.println(ret);
    }

    private static String printAllDistances(PacmanTileType[][] world, int posX, int posY) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < distanceMap[0].length; i++) {
            for (int j = 0; j < distanceMap.length; j++) {
                ret.append(String.format("[%2s]",
                                         world[j][i] == PacmanTileType.WALL ? "--" : distanceMap[posX][posY][j][i]));
            }
            ret.append("\n");
        }
        return ret.toString();
    }

    public static short getDistance(int firstPosX, int firstPosY, int secondPosX, int secondPosY) {
        assert distanceMap[firstPosX][firstPosY] != null;

        return distanceMap[firstPosX][firstPosY][secondPosX][secondPosY];
    }

    public static short[][] getDistances(int posX, int posY) {
        return distanceMap[posX][posY];
    }

    public static short[][][][] getDistances() {
        return distanceMap;
    }

    public static short getMaxDistance() {
        return MAX_DISTANCE;
    }

    public static class Geisterdistanz {

        public static short[] distanceToAllGhosts(int posX, int posY, List<GhostInfo> ghostInfos) {
            short[] ret = new short[ghostInfos.size()];
            int i = 0;
            for (GhostInfo ghost : ghostInfos) {
                ret[i++] = Felddistanzen.distanceMap[ghost.getPos().x][ghost.getPos().y][posX][posY];
            }
            return ret;
        }

        public static int sumOfGhostDistances(int posX, int posY, List<GhostInfo> ghostInfos) {
            int distanceSum = 0;
            for (short ghostDistance : distanceToAllGhosts(posX, posY, ghostInfos)) {
                distanceSum += ghostDistance;
            }
            return distanceSum;
        }

        public static short maximumGhostDistance(int posX, int posY, List<GhostInfo> ghostInfos) {
            short maxDistance = 0;
            for (short ghostDistance : distanceToAllGhosts(posX, posY, ghostInfos)) {
                if (ghostDistance > maxDistance) {
                    maxDistance = ghostDistance;
                }
            }
            return maxDistance;
        }

        public static short minimumGhostDistance(int posX, int posY, List<GhostInfo> ghostInfos) {
            short minDistance = Short.MAX_VALUE;
            for (short ghostDistance : distanceToAllGhosts(posX, posY, ghostInfos)) {
                if (ghostDistance < minDistance) {
                    minDistance = ghostDistance;
                }
            }
            return minDistance;
        }

    }
}
