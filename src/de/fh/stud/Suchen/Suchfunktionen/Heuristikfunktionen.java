package de.fh.stud.Suchen.Suchfunktionen;

import de.fh.kiServer.util.Util;
import de.fh.stud.Knoten;
import de.fh.stud.interfaces.IHeuristicFunction;

public class Heuristikfunktionen {
    public static IHeuristicFunction remainingDots() {
        return Knoten::getRemainingDots;
    }

    public static IHeuristicFunction manhattanToTarget(int goalX, int goalY) {
        return node -> Util.manhattan(node.getPosX(), node.getPosY(), goalX, goalY);
    }
}
