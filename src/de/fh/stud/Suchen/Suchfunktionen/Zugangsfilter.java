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
        return nonDangerousField(false);
    }

    public static IAccessibilityChecker nonDangerousField(boolean ignorePowerpill) {
        if (ignorePowerpill) {
            return (node, newPosX, newPosY) -> {
                PacmanTileType field = MyUtil.byteToTile(node.getView()[newPosX][newPosY]);
                return field == PacmanTileType.EMPTY || field == PacmanTileType.DOT || field == PacmanTileType.POWERPILL;
            };
        } else {
            return (node, newPosX, newPosY) -> {
                PacmanTileType field = MyUtil.byteToTile(node.getView()[newPosX][newPosY]);
                if (node.getPowerpillTimer() != 0) {
                    return field != PacmanTileType.WALL;
                }
                return field == PacmanTileType.EMPTY || field == PacmanTileType.DOT || field == PacmanTileType.POWERPILL;
            };
        }
    }

    public static IAccessibilityChecker nonDangerousEnvironment(boolean ignorePowerpill) {
        if (ignorePowerpill) {
            return (node, newPosX, newPosY) -> {
                if (!nonDangerousField(true).isAccessible(node, newPosX, newPosY))
                    return false;
                for (byte[] neighbour : Knoten.NEIGHBOUR_POS) {
                    if (MyUtil.isGhostType(MyUtil.byteToTile(node.getView()[newPosX + neighbour[0]][newPosY + neighbour[1]]))) {
                        return false;
                    }
                }
                return true;
            };
        } else {
            return (node, newPosX, newPosY) -> {
                if (!nonDangerousField(false).isAccessible(node, newPosX, newPosY))
                    return false;
                if (node.getPowerpillTimer() > 0 || MyUtil.byteToTile(node.getView()[newPosX][newPosY]) == PacmanTileType.POWERPILL)
                    return true;
                for (byte[] neighbour : Knoten.NEIGHBOUR_POS) {
                    if (MyUtil.isGhostType(MyUtil.byteToTile(node.getView()[newPosX + neighbour[0]][newPosY + neighbour[1]]))) {
                        return false;
                    }
                }
                return true;
            };
        }
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
