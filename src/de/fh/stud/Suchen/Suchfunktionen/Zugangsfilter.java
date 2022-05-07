package de.fh.stud.Suchen.Suchfunktionen;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.GameStateObserver;
import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Felddistanzen;
import de.fh.stud.Suchen.Sackgassen;
import de.fh.stud.interfaces.IAccessibilityChecker;

public class Zugangsfilter {

    public enum AvoidMode {
        ONLY_WALLS, GHOSTS_ON_FIELD, GHOSTS_THREATENS_FIELD
    }

    public static IAccessibilityChecker merge(IAccessibilityChecker... accessibilityCheckers) {
        return (node, newPosX, newPosY) -> {
            for (IAccessibilityChecker accessibilityChecker : accessibilityCheckers) {
                if (!accessibilityChecker.isAccessible(node, newPosX, newPosY)) {
                    return false;
                }
            }
            return true;
        };
    }

    public static IAccessibilityChecker avoidThese(Zugangsfilter.AvoidMode avoidMode) {
        return switch (avoidMode) {
            case ONLY_WALLS -> Zugangsfilter.noWall();
            case GHOSTS_ON_FIELD -> Zugangsfilter.nonDangerousField();
            case GHOSTS_THREATENS_FIELD -> Zugangsfilter.nonDangerousEnvironment();
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
            if (!nonDangerousField().isAccessible(node, newPosX, newPosY)) {
                return false;
            }
            if (node.getRemainingDots() == 1
                    && MyUtil.byteToTile(node.getView()[newPosX][newPosY]) == PacmanTileType.DOT) {
                return true;
            }
            if (MyUtil.byteToTile(node.getView()[newPosX][newPosY]) == PacmanTileType.POWERPILL) {
                return true;
            }
            if (node.getPowerpillTimer() == 0) {
                // TODO: Pacman stirbt, wenn der Ghost wieder respawnt und trotz aktiver Pille den Pacman schlaegt
                //  -> Man muesste den Spawnpoint des Ghosts finden, und der Ghost von dort aus ist dann gefaehrlich (was zu viel Arbeit abverlangt)
                if (MyUtil.ghostNextToPos(newPosX, newPosY, GameStateObserver.getGameState().getNewPercept().getGhostInfos())) {
                    return false;
                }
                return node.getRemainingDots() == 1 || Sackgassen.deadEndDepth[newPosX][newPosY] <= 0 ||
                        Felddistanzen.Geisterdistanz.minimumGhostDistance(node.getPosX(), node.getPosY(),
                                                                          GameStateObserver.getGameState().getNewPercept().getGhostInfos())
                                > 2 * Sackgassen.deadEndDepth[newPosX][newPosY];
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
                if (pos.x == newPosX && pos.y == newPosY) {
                    return false;
                }
            }
            return true;
        };
    }

}
