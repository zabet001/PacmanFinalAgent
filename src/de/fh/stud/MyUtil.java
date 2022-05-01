package de.fh.stud;

import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanTileType;

import java.util.Arrays;

public class MyUtil {

    public static byte[][] copyView(byte[][] orig) {
        byte[][] ret = new byte[orig.length][];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = Arrays.copyOf(orig[i], orig[i].length);
        }
        return ret;
    }

    public static byte tileToByte(PacmanTileType tile) {
        return (byte) tile.ordinal();
    }

    public static PacmanTileType byteToTile(byte b) {
        return b < PacmanTileType.values().length ? PacmanTileType.values()[b] : null;
    }

    public static byte[][] createByteView(PacmanTileType[][] world) {
        byte[][] view = new byte[world.length][world[0].length];
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world[i].length; j++) {
                view[i][j] = tileToByte(world[i][j]);
            }
        }

        return view;
    }

    public static PacmanTileType[][] reformatToTileType(byte[][] view) {
        PacmanTileType[][] ret = new PacmanTileType[view.length][view[0].length];
        for (int i = 0; i < ret.length; i++) {
            for (int j = 0; j < ret[0].length; j++) {
                ret[i][j] = byteToTile(view[i][j]);
            }
        }
        return ret;
    }

    public static int adjacentFreeFieldsCnt(PacmanTileType[][] view, int posX, int posY) {
        int neighbourCnt = 0;
        for (byte[] neighbour : Knoten.NEIGHBOUR_POS) {
            if (view[posX + neighbour[0]][posY + neighbour[1]] != PacmanTileType.WALL) {
                neighbourCnt++;
            }
        }
        return neighbourCnt;
    }

    public static PacmanAction oppositeAction(PacmanAction action) {
        switch (action) {
            case GO_NORTH -> {
                return PacmanAction.GO_SOUTH;
            }
            case GO_WEST -> {
                return PacmanAction.GO_EAST;
            }
            case GO_EAST -> {
                return PacmanAction.GO_WEST;
            }
            case GO_SOUTH -> {
                return PacmanAction.GO_NORTH;
            }
            case WAIT -> {
                return PacmanAction.WAIT;
            }
            case QUIT_GAME -> {
                return PacmanAction.QUIT_GAME;
            }
        }
        return null;
    }

    public static void println(String s) {
        System.out.println(s);
    }

    public static <T> T[] mergeArrays(T[] a, T[] b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        T[] ret = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, ret, a.length, b.length);
        return ret;
    }

    public static boolean isGhostType(PacmanTileType type) {
        return type == PacmanTileType.GHOST || type == PacmanTileType.GHOST_AND_DOT || type == PacmanTileType.GHOST_AND_POWERPILL;
    }
}
