package de.fh.stud.Suchen;

import de.fh.stud.Suchen.Suchfunktionen.Heuristikfunktionen;
import de.fh.stud.Suchen.Suchfunktionen.Zielfunktionen;
import de.fh.stud.Suchen.Suchfunktionen.Zugangsfilter;
import de.fh.stud.interfaces.IAccessibilityChecker;
import de.fh.stud.interfaces.ICallbackFunction;
import de.fh.stud.interfaces.IGoalPredicate;
import de.fh.stud.interfaces.IHeuristicFunction;

public class Suchszenario {
    private final IAccessibilityChecker accessCheck;
    private final IGoalPredicate goalPred;
    private final IHeuristicFunction heuristicFunc;
    private final ICallbackFunction[] callbackFuncs;

    private final boolean isStateProblem;

    // region Konstruktoren

    public Suchszenario(boolean isStateProblem, IAccessibilityChecker accessCheck, IGoalPredicate goalPred,
                        IHeuristicFunction heuristicFunc) {
        this(isStateProblem, accessCheck, goalPred, heuristicFunc, (ICallbackFunction[]) null);
    }

    public Suchszenario(boolean isStateProblem, IAccessibilityChecker accessCheck, IGoalPredicate goalPred,
                        IHeuristicFunction heuristicFunc, ICallbackFunction... callbackFuncs) {
        this.isStateProblem = isStateProblem;
        this.accessCheck = accessCheck;
        this.goalPred = goalPred;
        this.heuristicFunc = heuristicFunc;
        this.callbackFuncs = callbackFuncs;
    }

    public Suchszenario(IAccessibilityChecker accessCheck, IGoalPredicate goalPred, IHeuristicFunction heuristicFunc) {
        this(true, accessCheck, goalPred, heuristicFunc, (ICallbackFunction[]) null);
    }
    //endregion

    /**
     Felder vor Geister werden nicht betreten, es sei denn die Powerpille ist aktiv
     */
    public static Suchszenario eatAllDots() {
        return Suchszenario.eatAllDots(true, false);
    }

    /**
     @param carefulMode - AccessibilityChecker: bei true: Feldern mit benachbarten Geister werden ignoriert
     */
    public static Suchszenario eatAllDots(boolean carefulMode, boolean ignorePowerpill) {
        return new Suchszenario(carefulMode ? Zugangsfilter.nonDangerousEnvironment(ignorePowerpill) :
                Zugangsfilter.nonDangerousField(ignorePowerpill), Zielfunktionen.allDotsEaten(),
                Heuristikfunktionen.remainingDots());
    }

    /**
     Felder vor Geister werden nicht betreten, es sei denn die Powerpille ist aktiv
     */
    public static Suchszenario findDestination(int goalX, int goalY) {
        return Suchszenario.findDestination(true, false, goalX, goalY);
    }

    /**
     @param carefulMode - AccessibilityChecker: bei true: Feldern mit benachbarten Geister werden ignoriert
     @param ignorePowerpill - bei true: auch im powerpillMode wird nicht in geister expandiert
     */
    public static Suchszenario findDestination(boolean carefulMode, boolean ignorePowerpill, int goalX, int goalY) {
        return new Suchszenario(false, carefulMode ? Zugangsfilter.nonDangerousEnvironment(ignorePowerpill) :
                Zugangsfilter.nonDangerousField(ignorePowerpill), Zielfunktionen.reachedDestination(goalX, goalY),
                Heuristikfunktionen.manhattanToTarget(goalX, goalY));
    }

    public static Suchszenario locateDeadEndExit(byte[][] markedAsOneWays) {
        return new Suchszenario(false, Zugangsfilter.merge(Zugangsfilter.noWall(),
                (node, newPosX, newPosY) -> markedAsOneWays[newPosX][newPosY] == 0),
                Zielfunktionen.minimumNeighbours(2), null);
    }

    // region getter
    public IAccessibilityChecker getAccessCheck() {
        return accessCheck;
    }

    public IGoalPredicate getGoalPred() {
        return goalPred;
    }

    public IHeuristicFunction getHeuristicFunc() {
        return heuristicFunc;
    }

    public ICallbackFunction[] getCallbackFuncs() {
        return callbackFuncs;
    }

    public boolean isStateProblem() {
        return isStateProblem;
    }
    // endregion
}
