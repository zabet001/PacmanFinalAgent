package de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen;

import de.fh.kiServer.util.Util;
import de.fh.pacman.GhostInfo;
import de.fh.stud.Suchen.Felddistanzen;
import de.fh.stud.Suchen.Sackgassen;
import de.fh.stud.Suchen.Suchkomponenten.Knoten;

import java.util.Arrays;
import java.util.List;

public class Heuristikfunktionen {
    // TODO: Idee fuer Anwendung mehrerer Heuristiken
    //  - 1: heuristik als double speichern, und zweiter heuristikwert normieren (z.B. 1) Anzahl Dots [0,...) 2)
    //  Distanz naechster Dot [0,1]
    //  - 2: Mehrere Heuristikfunktionen speichern und fuer Heuristikfunktionen im Comparator gucken: if(a.heuristik1
    //  == b.heuristik2) compare(heuristik2)

    public static IHeuristicFunction combine(IHeuristicFunction... funcs) {
        return node -> {
            float ret = 0;
            for (IHeuristicFunction func : funcs) {
                ret += func.calcHeuristic(node);
            }
            return ret;
        };
    }

    public static IHeuristicFunction remainingDots() {
        return Knoten::getRemainingDots;
    }

    public static IHeuristicFunction manhattanToTarget(int goalX, int goalY) {
        return node -> Util.manhattan(node.getPosX(), node.getPosY(), goalX, goalY);
    }

    public static IHeuristicFunction realDistanceToTarget(int goalX, int goalY) {
        return node -> Felddistanzen.getDistance(goalX, goalY, node.getPosX(), node.getPosY());
    }

    public static IHeuristicFunction sumDistanceToGhosts(List<GhostInfo> ghosts) {
        return node -> Felddistanzen.getMaxDistance() - Felddistanzen.Geisterdistanz.sumOfGhostDistances(node.getPosX(),
                                                                                                         node.getPosY(),
                                                                                                         ghosts);
    }

    public static IHeuristicFunction distanceToCloserGhosts(List<GhostInfo> ghosts) {
        return node -> {
            short[] ghostDist = Felddistanzen.Geisterdistanz.distanceToAllGhosts(node.getPosX(), node.getPosY(),
                                                                                 ghosts);
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
                ret *= 100*Sackgassen.deadEndDepth[node.getPosX()][node.getPosY()];
            }

            return ret;
        };
    }

    public static IHeuristicFunction isDeadEndField() {
        return node -> Sackgassen.deadEndDepth[node.getPosX()][node.getPosY()] <= 0 ? 0 : 1;
    }
}
