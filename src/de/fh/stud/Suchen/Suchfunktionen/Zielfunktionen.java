package de.fh.stud.Suchen.Suchfunktionen;

import de.fh.stud.Knoten;
import de.fh.stud.MyUtil;
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

    public static IGoalPredicate dotEaten(boolean isStateSearch) {
        if (isStateSearch)
            return node -> node.getPred() != null && node.getRemainingDots() < node.getPred().getRemainingDots();
        return node -> node.getPred() != null && MyUtil.isDotType(MyUtil.byteToTile(node.getPred().getView()[node.getPosX()][node.getPosY()]));
    }

    public static IGoalPredicate allDotsEaten() {
        return node -> node.getRemainingDots() == 0;
    }

    //region Zielzustandsfunktionen
    public static IGoalPredicate reachedDestination(int goalx, int goaly) {
        return node -> {
            System.out.printf("Goal is [%d,%d]\n", goalx, goaly);
            System.out.printf("expCand is [%d,%d]\n", node.getPosX(), node.getPosY());

            return node.getPosX() == goalx && node.getPosY() == goaly;
        };
    }

    public static IGoalPredicate notOnPosition(int startPosX, int startPosY) {
        return node -> node.getPosX() != startPosX || node.getPosY() != startPosY;
    }

    public static IGoalPredicate minimumNeighbours(int numberOfNeighbours) {
        return node -> node.nodeNeighbourCnt() >= numberOfNeighbours;
    }

}
