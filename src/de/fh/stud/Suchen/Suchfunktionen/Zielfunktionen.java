package de.fh.stud.Suchen.Suchfunktionen;

import de.fh.stud.Knoten;
import de.fh.stud.interfaces.IGoalPredicate;

public class Zielfunktionen {

    public static IGoalPredicate merge(IGoalPredicate... goalPredicates) {
        return (node) -> {
            for (IGoalPredicate goalPredicate : goalPredicates) {
                if (!goalPredicate.isGoalNode(node))
                    return false;
            }
            return true;
        };
    }

    public static IGoalPredicate allDotsEaten() {
        return node -> node.getHeuristic() == 0;
    }

    //region Zielzustandsfunktionen
    public static IGoalPredicate reachedDestination(int goalx, int goaly) {
        return node -> node.getPosX() == goalx && node.getPosY() == goaly;
    }

    public static IGoalPredicate minimumNeighbours(int numberOfNeighbours) {
        return node -> Knoten.nodeNeighbourCnt(node) >= numberOfNeighbours;
    }

}
