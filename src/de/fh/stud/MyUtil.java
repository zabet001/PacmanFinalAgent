package de.fh.stud;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.GhostInfo;
import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanTileType;

import java.util.Arrays;
import java.util.List;

public class MyUtil {

	public static final byte[][] NEIGHBOUR_POS = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};

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

	public static int neighbourFieldsCnt(PacmanTileType[][] view, int posX, int posY) {
		int neighbourCnt = 0;
		for (byte[] neighbour : MyUtil.NEIGHBOUR_POS) {
			if (view[posX + neighbour[0]][posY + neighbour[1]] != PacmanTileType.WALL) {
				neighbourCnt++;
			}
		}
		return neighbourCnt;
	}

	public static boolean isNeighbour(Vector2 pos1, Vector2 pos2) {
		for (byte[] neighbour : NEIGHBOUR_POS) {
			if (pos1.x + neighbour[0] == pos2.x && pos1.y + neighbour[1] == pos2.y) {
				return true;
			}
		}
		return false;
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
		if (a == null) {
			return b;
		}
		if (b == null) {
			return a;
		}
		T[] ret = Arrays.copyOf(a, a.length + b.length);
		System.arraycopy(b, 0, ret, a.length, b.length);
		return ret;
	}

	public static boolean isGhostType(PacmanTileType type) {
		return type == PacmanTileType.GHOST || type == PacmanTileType.GHOST_AND_DOT
				|| type == PacmanTileType.GHOST_AND_POWERPILL;
	}

	public static boolean isDotType(PacmanTileType tile) {
		return tile == PacmanTileType.DOT || tile == PacmanTileType.GHOST_AND_DOT;
	}

	public static boolean isDotType(byte tile) {
		return isDotType(MyUtil.byteToTile(tile));
	}

	public static boolean isPowerpillType(PacmanTileType tile) {
		return tile == PacmanTileType.POWERPILL || tile == PacmanTileType.GHOST_AND_POWERPILL;
	}

	public static boolean isPowerpillType(byte tile) {
		return isPowerpillType(MyUtil.byteToTile(tile));
	}

	public static boolean ghostNextToPos(byte[][] view, int newPosX, int newPosY) {
		for (byte[] neighbour : NEIGHBOUR_POS) {
			if (isGhostType(byteToTile(view[newPosX + neighbour[0]][newPosY + neighbour[1]]))) {
				return true;
			}
		}
		return false;
	}

	public static boolean ghostNextToPos(int newPosX, int newPosY, List<GhostInfo> ghostInfos) {
		Vector2 pos = new Vector2(newPosX, newPosY);
		for (GhostInfo ghost : ghostInfos) {
			if (isNeighbour(pos, ghost.getPos())) {
				return true;
			}
		}
		return false;
	}

	public static boolean ghostNextToPos(PacmanTileType[][] view, int newPosX, int newPosY) {
		for (byte[] neighbour : NEIGHBOUR_POS) {
			if (isGhostType(view[newPosX + neighbour[0]][newPosY + neighbour[1]])) {
				return true;
			}
		}
		return false;
	}

	public static short countOccurrences(byte[][] view,
										 java.util.function.Function<Byte, Boolean> occurrenceIdentifier) {
		short cnt = 0;
		for (int row = 0; row < view.length; row++) {
			for (int col = 0; col < view[0].length; col++) {
				if (occurrenceIdentifier.apply(view[row][col])) {
					cnt++;
				}
			}
		}
		return cnt;
	}

	public static <T> short countOccurrences(T[][] view,
											 java.util.function.Function<T, Boolean> occurrenceIdentifier) {
		short cnt = 0;
		for (int row = 0; row < view.length; row++) {
			for (int col = 0; col < view[0].length; col++) {
				if (occurrenceIdentifier.apply(view[row][col])) {
					cnt++;
				}
			}
		}
		return cnt;
	}
}
