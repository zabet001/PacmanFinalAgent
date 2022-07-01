package de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen;

import de.fh.kiServer.util.Util;
import de.fh.pacman.GhostInfo;
import de.fh.stud.GameStateObserver;
import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Felddistanzen;
import de.fh.stud.Suchen.Sackgassen;
import de.fh.stud.Suchen.Suchkomponenten.Knoten;

import java.util.Arrays;
import java.util.List;

public interface IHeuristicFunction {

	float calcHeuristic(Knoten node);

	// region Util

	static float normalize(float val, float maxValue) {
		return maxValue == 0 ? val : val / maxValue;
	}
	// endregion

	// region Heuristicfunctions
	static float isDot(Knoten node) {
		return node.getPred() != null && MyUtil.isDotType(node
																  .getPred()
																  .getView()[node.getPosX()][node.getPosY()]) ? 0 : 1;
	}

	static int isPowerpill(Knoten node) {
		return node.getPred() != null && MyUtil.isPowerpillType(node
																		.getPred()
																		.getView()[node.getPosX()][node.getPosY()]) ?
				0 : 1;
	}

	static float remainingDots(Knoten node) {
		return node.getRemainingDots();
	}

	static float normedRemainingDots(Knoten node, int startingDotsAmt) {
		return normalize(remainingDots(node), startingDotsAmt);
	}

	static float manhattanDist(Knoten node, int targetX, int targetY) {
		return Util.manhattan(node.getPosX(), node.getPosY(), targetX, targetY);
	}

	static float realDist(Knoten node, int targetX, int targetY) {
		return Felddistanzen.getDistance(targetX, targetY, node.getPosX(), node.getPosY());
	}

	static float invGhostsDistsSum(Knoten node, List<GhostInfo> ghosts) {
		return Felddistanzen.getMaxDistance() - Felddistanzen.Geisterdistanz.sumOfGhostDistances(node.getPosX(),
																								 node.getPosY(),
																								 ghosts);
	}

	static float invGhostsDistsAsc(Knoten node, List<GhostInfo> ghosts) {
		if (ghosts.size() == 0) {
			return 0;
		}

		float ret = 0;
		short[] ghostDist = Felddistanzen.Geisterdistanz.distanceToAllGhosts(node.getPosX(), node.getPosY(), ghosts);
		Arrays.sort(ghostDist);

		// Aufschieben um n Ziffern (n ist die Anzahl Ziffern der maximalen Distanz)
		final byte maxDistanceDigitCnt = (byte) (Math.log10(Felddistanzen.getMaxDistance()) + 1);
		for (short dist : ghostDist) {
			ret *= Math.pow(10, maxDistanceDigitCnt);
			ret += Felddistanzen.getMaxDistance() - dist;
		}

		return ret;
	}

	static float normedInvGhostsDistsAsc(Knoten node) {
		float ret = invGhostsDistsAsc(node,
									  GameStateObserver
											  .getGameState()
											  .getNewPercept()
											  .getGhostInfos());
		if (ret == 0) {
			return 0;
		}

		// Fuer Normieren wird maximale Distanz zu allen Ghosts als float benoetigt
		float maxGhostDistances = 0;
		byte digitCnt = (byte) (Math.log10(Felddistanzen.getMaxDistance()) + 1);

		for (int i = 0; i < GameStateObserver
				.getGameState()
				.getNewPercept()
				.getGhostInfos()
				.size(); i++) {
			maxGhostDistances *= Math.pow(10, digitCnt);
			maxGhostDistances += Felddistanzen.getMaxDistance();
		}
		ret = normalize(ret, maxGhostDistances);

		return ret;
	}

	static float isDeadEndField(Knoten node) {
		return Sackgassen.deadEndDepth[node.getPosX()][node.getPosY()] <= 0 ? 0 : 1;
	}

	static float deadEndDepth(Knoten node) {
		return Sackgassen.deadEndDepth[node.getPosX()][node.getPosY()];
	}

	static float canClearDeadEnd(Knoten node, List<GhostInfo> ghosts) {
		// Strafterm: So viele Schritte muessen gegangen werden, bis die Sackgasse in betracht gezogen wird
		// Hohe Zahl: Auf keinen Fall hier rein gehen (zwar ungefaehrlich, aber macht 0 Sinn) (nichz
		final float FAIL_PENALTY = 1000000;
		// Prioritaet: So viele Schritte, bis normale Dots interessanter sind, als DeadEnds
		final float DEAD_END_PRIORITY = 10;

		if (Sackgassen.deadEndEntry[node.getPosX()][node.getPosY()] == null) {
			return DEAD_END_PRIORITY;
		}

		if (!IAccessibilityChecker.clearableDeadEnd(node, ghosts)) {
			return FAIL_PENALTY;
		}
		return 0;
	}
	//endregion
}
