package de.fh.stud.Suchen.Suchfunktionen;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanTileType;
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

    public static IAccessibilityChecker safeToWalkOn() {
        return safeToWalkOn(false);
    }

    public static IAccessibilityChecker safeToWalkOn(boolean noPowerpill) {
        if (noPowerpill) {
            return (node, newPosX, newPosY) -> {
                PacmanTileType field = MyUtil.byteToTile(node.getView()[newPosX][newPosY]);
                return field == PacmanTileType.EMPTY || field == PacmanTileType.DOT || field == PacmanTileType.POWERPILL;
            };
        } else
            // TODO: Powerpille einbeziehen, falls Geist
            return null;
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
