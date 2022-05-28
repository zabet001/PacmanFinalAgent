package de.fh.stud.Suchen.Suchfunktionen;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.Suchen.Suchkomponenten.Knoten;
import de.fh.stud.interfaces.ICallbackFunction;

import java.util.List;

public class CallbackFunktionen {
    public static ICallbackFunction saveStepCost(short[][] costMap) {
        return expCand -> costMap[expCand.getPosX()][expCand.getPosY()] = expCand.getCost();
    }

    public static ICallbackFunction saveVisitedPos(boolean[][] visitedMap) {
        return expCand -> visitedMap[expCand.getPosX()][expCand.getPosY()] = true;
    }

    public static ICallbackFunction saveVisitedType(List<PacmanTileType> visitedTypesList) {
        return expCand -> visitedTypesList.add(Knoten.getStaticWorld()[expCand.getPosX()][expCand.getPosY()]);
    }

    public static ICallbackFunction saveVisitedPos(List<Vector2> visitedList, boolean duplicates) {
        return expCand -> {
            if (duplicates || !visitedList.contains(expCand.getPosition())) {
                visitedList.add(expCand.getPosition());
            }
        };
    }

    public static ICallbackFunction saveVisited(List<Vector2> visitedList, List<PacmanTileType> visitedTypesList,
                                                boolean duplicates) {
        return expCand -> {
            if (duplicates || !visitedList.contains(expCand.getPosition())) {
                saveVisitedPos(visitedList, true).callback(expCand); // duplicates kann hier ignoriert werden
                saveVisitedType(visitedTypesList).callback(expCand);
            }
        };
    }

    public static <T> ICallbackFunction setVisitedValue(T[][] map, T value) {
        return expCand -> map[expCand.getPosX()][expCand.getPosY()] = value;
    }

    public static <T> ICallbackFunction setVisitedValue(byte[][] map, byte value) {
        return expCand -> map[expCand.getPosX()][expCand.getPosY()] = value;
    }

    public static ICallbackFunction printNodePositions() {
        return expCand -> System.out.printf("Position: %d|%d\n", expCand.getPosX(), expCand.getPosY());
    }
}
