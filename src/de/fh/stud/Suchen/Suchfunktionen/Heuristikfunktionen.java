package de.fh.stud.Suchen.Suchfunktionen;

import de.fh.kiServer.util.Util;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.MyUtil;
import de.fh.stud.interfaces.IHeuristicFunction;

public class Heuristikfunktionen {
    public static IHeuristicFunction remainingDots() {
        return node -> {
            if (node.getPred() == null)
                return node.countDots();
            else if (node.getPred().getView()[node.getPosX()][node.getPosY()] == MyUtil.tileToByte(PacmanTileType.DOT)
                    || node.getPred().getView()[node.getPosX()][node.getPosY()] == MyUtil.tileToByte(PacmanTileType.GHOST_AND_DOT))
                return node.getPred().getHeuristic() - 1;
            else
                return node.getPred().getHeuristic();
        };
    }

    public static IHeuristicFunction manhattanToTarget(int goalX, int goalY) {
        return node -> Util.manhattan(node.getPosX(), node.getPosY(), goalX, goalY);
    }
}
