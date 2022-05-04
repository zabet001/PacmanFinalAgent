package de.fh.stud.Suchen.Suchfunktionen;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.GhostInfo;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.GameStateObserver;
import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Sackgassen;
import de.fh.stud.interfaces.IAccessibilityChecker;

import java.util.List;

public class Zugangsfilter {

    public enum AvoidMode {
        ONLY_WALLS, GHOSTS_ON_FIELD, GHOSTS_THREATENS_FIELD
    }

    public static IAccessibilityChecker merge(IAccessibilityChecker... accessibilityCheckers) {
        return (node, newPosX, newPosY) -> {
            for (IAccessibilityChecker accessibilityChecker : accessibilityCheckers) {
                if (!accessibilityChecker.isAccessible(node, newPosX, newPosY))
                    return false;
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
                // TODO: Auch bei aktivem PillTimer kann ein gefressener Geist gefaehrlich sein
                //  -> ueberpruefen, ob auf dem Feld ein Geist mit abgelaufenem powerpillTimer ist
                return field != PacmanTileType.WALL;
            }
            return field == PacmanTileType.EMPTY || field == PacmanTileType.DOT || field == PacmanTileType.POWERPILL;
        };
    }

    public static IAccessibilityChecker nonDangerousEnvironment() {
        return (node, newPosX, newPosY) -> {
            if (!nonDangerousField().isAccessible(node, newPosX, newPosY))
                return false;
            if (node.getRemainingDots() == 1 && MyUtil.byteToTile(node.getView()[newPosX][newPosY]) == PacmanTileType.DOT) {
                return true;
            }
            if (MyUtil.byteToTile(node.getView()[newPosX][newPosY]) == PacmanTileType.POWERPILL)
                return true;

            // TODO IDEE: In einen geist kann man nur ein mal pro Powerpille expandieren
            if (node.getPowerpillTimer() == 0) {
                for (GhostInfo ghost : GameStateObserver.newPercept.getGhostInfos()) {
                    if (ghost.getPillTimer() == 0 && MyUtil.isNeighbour(ghost.getPos(),
                            new Vector2(newPosX, newPosY))) {
                        return false;
                    }
                }
                // TODO: Sackgassenerkennung (Problem: Suche in Suche nicht moeglich wegen stativc attribute)
/*                if (Sackgassen.deadEndDepth[newPosX][newPosY] > 0)
                    for (GhostInfo ghosts : GameStateObserver.newPercept.getGhostInfos()) {
                        if (ghosts.getPillTimer() == 0 && Heuristikfunktionen.realDistance(newPosX, newPosY,
                                ghosts.getPos().x, ghosts.getPos().y) <= 2 * Sackgassen.deadEndDepth[newPosX][newPosY])
                            return false;
                    }*/
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
