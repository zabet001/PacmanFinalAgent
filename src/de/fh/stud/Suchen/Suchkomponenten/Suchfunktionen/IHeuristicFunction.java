package de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen;

import de.fh.kiServer.util.Util;
import de.fh.pacman.GhostInfo;
import de.fh.stud.Suchen.Felddistanzen;
import de.fh.stud.Suchen.Sackgassen;
import de.fh.stud.Suchen.Suchkomponenten.Knoten;

import java.util.Arrays;
import java.util.List;

public interface IHeuristicFunction {
	float calcHeuristic(Knoten node);

	static float combine(Knoten node, IHeuristicFunction... funcs) {
		float ret = 0;
		for (IHeuristicFunction func : funcs) {
			ret += func.calcHeuristic(node);
		}
		return ret;
	}

	static float remainingDots(Knoten node) {
		return node.getRemainingDots();
	}

	static float manhattanToTarget(Knoten node, int goalX, int goalY) {
		return Util.manhattan(node.getPosX(), node.getPosY(), goalX, goalY);
	}

	static float realDistanceToTarget(Knoten node, int goalX, int goalY) {
		return Felddistanzen.getDistance(goalX, goalY, node.getPosX(), node.getPosY());
	}

	static float sumDistanceToGhosts(Knoten node, List<GhostInfo> ghosts) {
		return Felddistanzen.getMaxDistance() - Felddistanzen.Geisterdistanz.sumOfGhostDistances(node.getPosX(),
																								 node.getPosY(),
																								 ghosts);
	}

	static float distanceToCloserGhosts(Knoten node, List<GhostInfo> ghosts) {
		short[] ghostDist = Felddistanzen.Geisterdistanz.distanceToAllGhosts(node.getPosX(), node.getPosY(), ghosts);
		if (ghostDist.length == 0) {
			return 0;
		}
		float ret = 0;
		Arrays.sort(ghostDist);
		// Aufschieben um n Ziffern (n ist die Anzahl Ziffern der maximalen Distanz)
		final byte maxDistanceDigitCnt = (byte) (Math.log10(Felddistanzen.getMaxDistance()) + 1);
		for (short dist : ghostDist) {
			ret *= Math.pow(10, maxDistanceDigitCnt);
			ret += Felddistanzen.getMaxDistance() - dist;
		}

		// (Pacman soll nicht in Sackgassen reinlaufen, aber aus Sackgassen rauslaufen) TODO: Unschön, fixen!!!
		if (Sackgassen.deadEndDepth[node.getPosX()][node.getPosY()] != 0) {
			ret *= 100 * Sackgassen.deadEndDepth[node.getPosX()][node.getPosY()];
		}

		return ret;
	}

	static float isDeadEndField(Knoten node) {
		return Sackgassen.deadEndDepth[node.getPosX()][node.getPosY()] <= 0 ? 0 : 1;
	}
}
