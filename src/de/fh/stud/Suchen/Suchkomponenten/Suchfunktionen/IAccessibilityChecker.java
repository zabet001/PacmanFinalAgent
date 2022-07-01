package de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.GhostInfo;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.GameStateObserver;
import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Felddistanzen;
import de.fh.stud.Suchen.Sackgassen;
import de.fh.stud.Suchen.Suche;
import de.fh.stud.Suchen.Suchkomponenten.Knoten;

import java.util.Comparator;
import java.util.List;

public interface IAccessibilityChecker {
	boolean isAccessible(Knoten node, byte newPosX, byte newPosY);

	enum AvoidMode {
		ONLY_WALLS, GHOSTS_ON_FIELD, GHOSTS_THREATENS_FIELD

	}

	static IAccessibilityChecker avoidThese(IAccessibilityChecker.AvoidMode avoidMode) {
		return switch (avoidMode) {
			case ONLY_WALLS -> IAccessibilityChecker::noWall;
			case GHOSTS_ON_FIELD -> IAccessibilityChecker::nonDangerousField;
			case GHOSTS_THREATENS_FIELD -> IAccessibilityChecker::nonDangerousEnvironment;
		};
	}

	static boolean nonDangerousField(Knoten node, byte newPosX, byte newPosY) {
		PacmanTileType field = MyUtil.byteToTile(node.getView()[newPosX][newPosY]);
		if (node.getPowerpillTimer() > 0) {
			return field != PacmanTileType.WALL;
		}
		return field == PacmanTileType.EMPTY || field == PacmanTileType.DOT || field == PacmanTileType.POWERPILL;
	}

	static boolean nonDangerousEnvironment(Knoten node, byte newPosX, byte newPosY) {
		// Ist das Feld ueberhaupt betretbar?
		if (!nonDangerousField(node, newPosX, newPosY)) {
			return false;
		}

		// Powerpille: Unverwundbarkeit (Wenn Powerpille zu tief in Sackgasse ablaeuft: Koennte Vllt. eingesperrt sein)
		if (node.getPowerpillTimer() != 0) {
			return true;
		}

		// Auf Powerpill gehen -> Unverwundbar
		if (MyUtil.byteToTile(node.getView()[newPosX][newPosY]) == PacmanTileType.POWERPILL) {
			return true;
		}

		// Letzten Dot gefressen: Spiel gewonnen
		if (node.getRemainingDots() == 1 && MyUtil.byteToTile(node.getView()[newPosX][newPosY]) == PacmanTileType.DOT) {
			return true;
		}

		// Geist neben dem Feld? Sehr wahrscheinlicher Tod
		if (MyUtil.ghostNextToPos(newPosX,
								  newPosY,
								  GameStateObserver
										  .getGameState()
										  .getNewPercept()
										  .getGhostInfos())) {
			return false;
		}

		// Keine Sackgasse: Sicher (bezieht nicht "dynamische Geistersackgassen" ein)
		if (Sackgassen.deadEndDepth[newPosX][newPosY] <= 0) {
			return true;
		}

		// Sicher, wenn der Pacman rechtzeitig aus der Sackgasse wieder raus kann
		if (Felddistanzen.Geisterdistanz.minimumGhostDistance(Sackgassen.deadEndEntry[newPosX][newPosY].x,
															  Sackgassen.deadEndEntry[newPosX][newPosY].y,
															  GameStateObserver
																	  .getGameState()
																	  .getNewPercept()
																	  .getGhostInfos())
				> 1 + Sackgassen.deadEndDepth[newPosX][newPosY]) {
			return true;
		}

		// Wenn alle verbleibenden Dots in dieser Sackgasse sind: Sicher (gilt nicht fuer verzweigte Sackgassen)
		if (Sackgassen.allDotsInThisDeadEnd(node.getView(), newPosX, newPosY)) {
			return true;
		}
		return false;
		// Idee fuer verzweigte Sackgassen: schauen, ob man alle Dots in dieser Sackgasse so fressen koennte
		// return Sackgassen.canEatAllDotsInOneGo(node.getView(), newPosX, newPosY);

	}

	static boolean noWall(Knoten node, byte newPosX, byte newPosY) {
		return MyUtil.byteToTile(node.getView()[newPosX][newPosY]) != PacmanTileType.WALL;
	}

	static boolean excludePositions(byte newPosX, byte newPosY, Vector2... positions) {
		for (Vector2 pos : positions) {
			if (pos.x == newPosX && pos.y == newPosY) {
				return false;
			}
		}
		return true;
	}

	static boolean clearableDeadEnd(Knoten node, List<GhostInfo> ghosts) {
		// TODO: Dies funktioniert nicht fuer AccessCheck, wenn in einer Sackgasse gestartet wurde
		// Keine Sackgasse
		if (node.getPowerpillTimer() > 0 || Sackgassen.deadEndEntry[node.getPosX()][node.getPosY()] == null) {
			return true;
		}

		Suche clearSearch = new Suche.SucheBuilder()
				.setAccessChecks(IAccessibilityChecker::noWall,
								 (chilren, newPosX, newPosY) -> IAccessibilityChecker.excludePositions(newPosX,
																									   newPosY,
																									   Sackgassen.deadEndEntry[node.getPosX()][node.getPosY()]))
				.setHeuristicFuncs(IHeuristicFunction::remainingDots)
				.setGoalPred(IGoalPredicate::dotEaten)
				.noSolutionLimit()
				.createSuche();
		List<Knoten> dotEatenNodes = clearSearch.start(node.getView(),
													   node.getPosX(),
													   node.getPosY(),
													   Suche.SearchStrategy.A_STAR);
		if (dotEatenNodes.size() == 0) {
			// Die Suche begann auf dem einzig verbliebenen Dot der Sackgasse
			if (node.getPred() != null && MyUtil.isDotType(node
																   .getPred()
																   .getView()[node.getPosX()][node.getPosY()])) {
				return true;
			}
			// Keine Dots in der Sackgasse gefunden
			return false;
		}

		// Die kuerzeste Loesung mit den meisten gefressenen Dots
		dotEatenNodes.sort(Comparator
								   .comparingInt(Knoten::getRemainingDots)
								   .thenComparingInt(Knoten::getCost));

		Knoten fastestSolution = dotEatenNodes.get(0);

		if (Felddistanzen.Geisterdistanz.minimumGhostDistance(Sackgassen.deadEndEntry[node.getPosX()][node.getPosY()].x,
															  Sackgassen.deadEndEntry[node.getPosX()][node.getPosY()].y,
															  ghosts) <= fastestSolution.getCost() + 1
				+ Sackgassen.deadEndDepth[fastestSolution.getPosX()][fastestSolution.getPosY()]) {
			return false;
		}
		return true;
	}
}
