package de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen;

import de.fh.kiServer.util.Vector2;
import de.fh.stud.Suchen.Suchkomponenten.Knoten;

import java.util.List;

public interface ICallbackFunction {
	void callback(Knoten expCand);

	static void saveStepCost(Knoten expCand, short[][] costMap) {
		costMap[expCand.getPosX()][expCand.getPosY()] = expCand.getCost();
	}

	static void saveVisitedPos(Knoten expCand, boolean[][] visitedMap) {
		visitedMap[expCand.getPosX()][expCand.getPosY()] = true;
	}

	static void saveVisitedPos(Knoten expCand, List<Vector2> visitedList, boolean duplicates) {
		if (duplicates || !visitedList.contains(expCand.getPosition())) {
			visitedList.add(expCand.getPosition());
		}
	}

	static <T> void setVisitedValue(Knoten expCand, T[][] map, T value) {
		map[expCand.getPosX()][expCand.getPosY()] = value;
	}

	static <T> void setVisitedValue(Knoten expCand, byte[][] map, byte value) {
		map[expCand.getPosX()][expCand.getPosY()] = value;
	}

	static void printNodePositions(Knoten expCand) {
		System.out.printf("Position: %d|%d\n", expCand.getPosX(), expCand.getPosY());
	}
}
