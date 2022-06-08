package de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.GameStateObserver;
import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Felddistanzen;
import de.fh.stud.Suchen.Sackgassen;

public class Zugangsfilter {
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
            // Ist das Feld ueberhaupt betretbar?
            if (!nonDangerousField().isAccessible(node, newPosX, newPosY)) {
                return false;
            }

            // Powerpille: Unverwundbarkeit (Wenn Powerpille zu tief in Sackgasse ablaeuft: Koennte Vllt. eingesperrt sein)
            if (node.getPowerpillTimer() != 0) {
                return true;
            }

            // Auf Powerpill gehen -> Unverwundbar
            if (MyUtil.byteToTile(node.getView()[newPosX][newPosY]) == PacmanTileType.POWERPILL) {
                return true;
            }

            // Letzten Dot gefressen: Spiel gewonnen
            if (node.getRemainingDots() == 1
                    && MyUtil.byteToTile(node.getView()[newPosX][newPosY]) == PacmanTileType.DOT) {
                return true;
            }

            // Geist neben dem Feld? Sehr wahrscheinlicher Tod
            if (MyUtil.ghostNextToPos(newPosX, newPosY, GameStateObserver
                    .getGameState()
                    .getNewPercept()
                    .getGhostInfos())) {
                return false;
            }

            // Keine Sackgasse: Sicher (bezieht nicht "dynamische Geistersackgassen" ein)
            if (Sackgassen.deadEndDepth[newPosX][newPosY] <= 0) {
                return true;
            }

            // Sicher, wenn der Pacman rechtzeitig aus der Sackgasse wieder raus kann
            if (Felddistanzen.Geisterdistanz.minimumGhostDistance(Sackgassen.deadEndEntry[newPosX][newPosY].x,
                                                                  Sackgassen.deadEndEntry[newPosX][newPosY].y,
                                                                  GameStateObserver
                                                                          .getGameState()
                                                                          .getNewPercept()
                                                                          .getGhostInfos())
                    > 1 + Sackgassen.deadEndDepth[newPosX][newPosY]) {
                return true;
            }

            // Wenn alle verbleibenden Dots in dieser Sackgasse sind: Sicher (gilt nicht fuer verzweigte Sackgassen)
            if (Sackgassen.allDotsInThisDeadEnd(node.getView(), newPosX, newPosY)) {
                return true;
            }
            return false;
            // Idee fuer verzweigte Sackgassen: schauen, ob man alle Dots in dieser Sackgasse so fressen koennte
            // return Sackgassen.canEatAllDotsInOneGo(node.getView(), newPosX, newPosY);

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

    public enum AvoidMode {
        ONLY_WALLS, GHOSTS_ON_FIELD, GHOSTS_THREATENS_FIELD
    }

}
