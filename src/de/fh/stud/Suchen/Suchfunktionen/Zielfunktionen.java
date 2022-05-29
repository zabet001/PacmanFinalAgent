package de.fh.stud.Suchen.Suchfunktionen;

import de.fh.stud.MyUtil;
import de.fh.stud.interfaces.IAccessibilityChecker;
import de.fh.stud.interfaces.IGoalPredicate;

public class Zielfunktionen {

    public static IGoalPredicate merge(IGoalPredicate... goalPredicates) {
        return (node) -> {
            for (IGoalPredicate goalPredicate : goalPredicates) {
                if (!goalPredicate.isGoalNode(node)) {
                    return false;
                }
            }
            return true;
        };
    }

    public static IGoalPredicate powerpillEaten() {
        return node -> node.getPred() != null && MyUtil.isPowerpillType(MyUtil.byteToTile(node
                                                                                                  .getPred()
                                                                                                  .getView()[node.getPosX()][node.getPosY()]));
    }

    public static IGoalPredicate dotEaten() {
        return node -> node.getPred() != null && MyUtil.isDotType(MyUtil.byteToTile(node
                                                                                            .getPred()
                                                                                            .getView()[node.getPosX()][node.getPosY()]));
    }

    public static IGoalPredicate amountOfDotsEaten(int amount, int currentDotAmount) {
        return node -> node.getRemainingDots() == 0 || currentDotAmount - amount >= node.getRemainingDots();
    }

    public static IGoalPredicate allDotsEaten() {
        return node -> node.getRemainingDots() == 0;
    }

    //region Zielzustandsfunktionen
    public static IGoalPredicate reachedDestination(int goalx, int goaly) {
        return node -> {
            // System.out.printf("reachedDestination: Goal is [%d,%d]\n", goalx, goaly);
            // System.out.printf("reachedDestination: expCand is [%d,%d]\n", node.getPosX(), node.getPosY());

            return node.getPosX() == goalx && node.getPosY() == goaly;
        };
    }

    public static IGoalPredicate notOnPosition(int startPosX, int startPosY) {
        return node -> node.getPosX() != startPosX || node.getPosY() != startPosY;
    }

    public static IGoalPredicate didAnAction(int startPosX, int startPosY) {
        return node -> node.getPred() != null;
    }

    public static IGoalPredicate minimumNeighbours(int numberOfNeighbours, IAccessibilityChecker accessibilityChecker) {
        return node -> node.nodeNeighbourCnt(accessibilityChecker) >= numberOfNeighbours;
    }

}
