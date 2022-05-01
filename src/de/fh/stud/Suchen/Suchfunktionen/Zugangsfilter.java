package de.fh.stud.Suchen.Suchfunktionen;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.Knoten;
import de.fh.stud.MyUtil;
import de.fh.stud.interfaces.IAccessibilityChecker;

public class Zugangsfilter {

    public static IAccessibilityChecker merge(IAccessibilityChecker... accessibilityCheckers) {
        return (node, newPosX, newPosY) -> {
            for (IAccessibilityChecker accessibilityChecker : accessibilityCheckers) {
                if (!accessibilityChecker.isAccessible(node, newPosX, newPosY))
                    return false;
            }
            return true;
        };
    }

    public static IAccessibilityChecker nonDangerousField() {
        return (node, newPosX, newPosY) -> {
            PacmanTileType field = MyUtil.byteToTile(node.getView()[newPosX][newPosY]);
            if (node.getPowerpillTimer() > 0) {
                return field != PacmanTileType.WALL;
            }
            return field == PacmanTileType.EMPTY || field == PacmanTileType.DOT || field == PacmanTileType.POWERPILL;
        };
    }

    public static IAccessibilityChecker nonDangerousEnvironment() {
        return (node, newPosX, newPosY) -> {
            if (!nonDangerousField().isAccessible(node, newPosX, newPosY))
                return false;
            if (node.getPowerpillTimer() > 0 || MyUtil.byteToTile(node.getView()[newPosX][newPosY]) == PacmanTileType.POWERPILL)
                return true;
            if (node.getRemainingDots() == 1 && MyUtil.byteToTile(node.getView()[newPosX][newPosY]) == PacmanTileType.DOT) {
                return true;
            }
            for (byte[] neighbour : Knoten.NEIGHBOUR_POS) {
                if (MyUtil.isGhostType(MyUtil.byteToTile(node.getView()[newPosX + neighbour[0]][newPosY + neighbour[1]]))) {
                    return false;
                }
            }
            return true;
        };
    }

    public static IAccessibilityChecker noWall() {
        return (node, newPosX, newPosY) -> MyUtil.byteToTile(node.getView()[newPosX][newPosY]) != PacmanTileType.WALL;
    }

    public static IAccessibilityChecker excludePositions(Vector2... positions) {
        return (node, newPosX, newPosY) -> {
            for (Vector2 pos : positions) {
                if (pos.x == newPosX && pos.y == newPosY)
                    return false;
            }
            return true;
        };
    }

}
