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
     HINWEIS: Felder vor Geister werden nicht betreten, es sei denn die Powerpille ist aktiv
     */
    public static Suchszenario eatAllDots() {
        return Suchszenario.eatAllDots(false);
    }

    /**
     @param noEnemies - bei true: Geister werden wie leere Felder betrachtet
     */
    public static Suchszenario eatAllDots(boolean noEnemies) {
        return new Suchszenario(noEnemies ? Zugangsfilter.noWall() : Zugangsfilter.nonDangerousEnvironment(),
                Zielfunktionen.allDotsEaten(), Heuristikfunktionen.remainingDots());
    }

    /**
     HINWEIS: Felder vor Geister werden nicht betreten, es sei denn die Powerpille ist aktiv
     */
    public static Suchszenario eatNearestDot() {
        return Suchszenario.eatNearestDot(false);
    }

    /**
     @param noEnemies - bei true: Geister werden wie leere Felder betrachtet
     */
    public static Suchszenario eatNearestDot(boolean noEnemies) {
        return Suchszenario.eatNearestDot(true, noEnemies);
    }

    /**
     @param isStateProblem - Zusatzinformationen, wie verbleibende Anzahl Dots
     @param noEnemies - bei true: Geister werden wie leere Felder betrachtet
     */
    public static Suchszenario eatNearestDot(boolean isStateProblem, boolean noEnemies) {
        return new Suchszenario(isStateProblem, noEnemies ? Zugangsfilter.noWall() :
                Zugangsfilter.nonDangerousEnvironment(), Zielfunktionen.dotEaten(isStateProblem), null);
    }

    /**
     HINWEIS: Felder vor Geister werden nicht betreten, es sei denn die Powerpille ist aktiv
     */
    public static Suchszenario findDestination(int goalX, int goalY) {
        return Suchszenario.findDestination(false, goalX, goalY);
    }

    /**
     @param noEnemies - bei true: auch im powerpillMode wird nicht in geister expandiert
     */
    public static Suchszenario findDestination(boolean noEnemies, int goalX, int goalY) {
        return Suchszenario.findDestination(false, noEnemies, goalX, goalY);
    }

    /**
     @param isStateProblem - Zusatzinformationen, wie verbleibende Anzahl Dots
     @param noEnemies - bei true: Geister werden wie leere Felder betrachtet
     */
    public static Suchszenario findDestination(boolean isStateProblem, boolean noEnemies, int goalX, int goalY) {
        return new Suchszenario(isStateProblem, noEnemies ? Zugangsfilter.noWall() :
                Zugangsfilter.nonDangerousEnvironment(), Zielfunktionen.reachedDestination(goalX, goalY),
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
