package de.fh.stud.Suchen.Suchfunktionen;

import de.fh.kiServer.util.Util;
import de.fh.pacman.GhostInfo;
import de.fh.stud.Knoten;
import de.fh.stud.Suchen.Suche;
import de.fh.stud.Suchen.Suchszenario;
import de.fh.stud.interfaces.IAccessibilityChecker;
import de.fh.stud.interfaces.ICallbackFunction;
import de.fh.stud.interfaces.IGoalPredicate;
import de.fh.stud.interfaces.IHeuristicFunction;

import java.util.List;

public class Heuristikfunktionen {
    public static IHeuristicFunction remainingDots() {
        return Knoten::getRemainingDots;
    }

    public static IHeuristicFunction manhattanToTarget(int goalX, int goalY) {
        return node -> Util.manhattan(node.getPosX(), node.getPosY(), goalX, goalY);
    }

    public static IHeuristicFunction distanceToGhosts(List<GhostInfo> ghostInfos) {
        return node -> {
            int ret = 0;
            // IDEE: wenn ein Ghost gefressen werden kann, wird dessen Distanz ignoriert
            for (GhostInfo ghost : ghostInfos) {
                if (ghost.getPillTimer() == 0) {
                    // TODO: Heuristik ist negativ (sollte der optimalitaet halber nicht sein), stattdessen
                    //  MAX_DISTANCE - calculatedDistance

                    int realDistance = realDistance(node.getPosX(), node.getPosY(), ghost.getPos().x,ghost.getPos().y);
                    ret -= realDistance;
                }
            }

            return ret;
        };
    }

    public static int realDistance(int firstLocationX, int firstLocationY, int secondLocationX, int secondLocationY) {
        // TODO: !!!! DRINGEND Suchfunktionen in Suche non-static machen!!!!
/*        IAccessibilityChecker accessibilityChecker = Suche.getAccessCheck();
        IGoalPredicate goalPredicate = Suche.getGoalPred();
        IHeuristicFunction heuristicFunction = Suche.getHeuristicFunc();
        ICallbackFunction[] callbackFunction = Suche.getCallbackFuncs();*/


        Suche s = new Suche(Suchszenario.findDestination(false, Zugangsfilter.AvoidMode.GHOSTS_ON_FIELD,
                firstLocationX, firstLocationY));
        s.setNoWaitAction(true);
        Knoten node = s.start(Knoten.getStaticWorld(), secondLocationX,
                secondLocationY, Suche.SearchStrategy.A_STAR, false);
        return node != null ? node.getCost() : 0;

    }
}
