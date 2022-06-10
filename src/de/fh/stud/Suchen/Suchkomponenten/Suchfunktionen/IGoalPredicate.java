package de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen;

import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Suchkomponenten.Knoten;

public interface IGoalPredicate {
	static boolean powerpillEaten(Knoten node) {
		return node.getPred() != null && MyUtil.isPowerpillType(MyUtil.byteToTile(node
																						  .getPred()
																						  .getView()[node.getPosX()][node.getPosY()]));
	}

	static boolean dotEaten(Knoten node) {
		return node.getPred() != null && MyUtil.isDotType(MyUtil.byteToTile(node
																					.getPred()
																					.getView()[node.getPosX()][node.getPosY()]));
	}

	static boolean amountOfDotsEaten(Knoten node, int amount, int currentDotAmount) {
		return node.getRemainingDots() == 0 || currentDotAmount - amount >= node.getRemainingDots();
	}

	static boolean allDotsEaten(Knoten node) {
		return node.getRemainingDots() == 0;
	}

	static boolean reachedDestination(Knoten node, int goalx, int goaly) {
		// System.out.printf("reachedDestination: Goal is [%d,%d]\n", goalx, goaly);
		// System.out.printf("reachedDestination: expCand is [%d,%d]\n", node.getPosX(), node.getPosY());

		return node.getPosX() == goalx && node.getPosY() == goaly;
	}

	static boolean notOnPosition(Knoten node, int startPosX, int startPosY) {
		return node.getPosX() != startPosX || node.getPosY() != startPosY;
	}

	static boolean didAnAction(Knoten node, int startPosX, int startPosY) {
		return node.getPred() != null;
	}

	static boolean minimumNeighbours(Knoten node, int numberOfNeighbours,
									 IAccessibilityChecker... accessibilityChecker) {
		return node.nodeNeighbourCnt(accessibilityChecker) >= numberOfNeighbours;
	}

	boolean isGoalNode(Knoten node);
}
