package de.fh.stud.Suchen;

import de.fh.stud.GameStateObserver;
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
    private boolean noWait;

    // region Konstruktoren
    public Suchszenario(IAccessibilityChecker accessCheck, IGoalPredicate goalPred, IHeuristicFunction heuristicFunc) {
        this(true, accessCheck, false, goalPred, heuristicFunc, (ICallbackFunction[]) null);
    }

    public Suchszenario(boolean isStateProblem, IAccessibilityChecker accessCheck, IGoalPredicate goalPred,
                        IHeuristicFunction heuristicFunc) {
        this(isStateProblem, accessCheck, false, goalPred, heuristicFunc, (ICallbackFunction[]) null);
    }

    public Suchszenario(boolean isStateProblem, IAccessibilityChecker accessCheck, boolean noWait,
                        IGoalPredicate goalPred, IHeuristicFunction heuristicFunc) {
        this(isStateProblem, accessCheck, noWait, goalPred, heuristicFunc, (ICallbackFunction[]) null);

    }

    public Suchszenario(boolean isStateProblem, IAccessibilityChecker accessCheck, boolean noWait,
                        IGoalPredicate goalPred, IHeuristicFunction heuristicFunc, ICallbackFunction... callbackFuncs) {
        this.isStateProblem = isStateProblem;
        this.accessCheck = accessCheck;
        this.noWait = noWait;
        this.goalPred = goalPred;
        this.heuristicFunc = heuristicFunc;
        this.callbackFuncs = callbackFuncs;
    }

    //endregion
    public static Suchszenario runAway(Zugangsfilter.AvoidMode avoidMode, boolean noWait, int startPosX,
                                       int startPosY) {
        return runAway(true,avoidMode,noWait, startPosX, startPosY);
    }

    public static Suchszenario runAway(boolean isStateProblem, Zugangsfilter.AvoidMode avoidMode, boolean noWait,
                                       int startPosX, int startPosY) {
        return new Suchszenario(isStateProblem, Zugangsfilter.avoidThese(avoidMode), noWait,
                                Zielfunktionen.notOnPosition(startPosX, startPosY),
                                Heuristikfunktionen.distanceToCloserGhosts(GameStateObserver.getGameState().getNewPercept().getGhostInfos()));
    }

    /**
     HINWEIS: Felder vor Geister werden nicht betreten, es sei denn die Powerpille ist aktiv
     */
    public static Suchszenario eatAllDots() {
        return Suchszenario.eatAllDots(Zugangsfilter.AvoidMode.GHOSTS_THREATENS_FIELD);
    }

    public static Suchszenario eatNearestPowerpill(Zugangsfilter.AvoidMode avoidMode) {
        return Suchszenario.eatNearestPowerpill(true, avoidMode);
    }

    public static Suchszenario eatNearestPowerpill(boolean isStateProblem, Zugangsfilter.AvoidMode avoidMode) {
        return new Suchszenario(isStateProblem, Zugangsfilter.avoidThese(avoidMode), Zielfunktionen.powerpillEaten(),
                                null);
    }

    /**
     @param avoidMode - Filtern nach "Waenden" oder zusaetzlich "Geistern auf Feld" oder zusaetzlich "+ "Geister neben
     dem Feld"
     */
    public static Suchszenario eatNearestDot(Zugangsfilter.AvoidMode avoidMode) {
        return Suchszenario.eatNearestDot(true, avoidMode);
    }

    /**
     @param isStateProblem - Zusatzinformationen, wie verbleibende Anzahl Dots
     @param avoidMode - Filtern nach "Waenden" oder zusaetzlich "Geistern auf Feld" oder zusaetzlich "+ "Geister neben
     dem Feld"
     */
    public static Suchszenario eatNearestDot(boolean isStateProblem, Zugangsfilter.AvoidMode avoidMode) {
        return new Suchszenario(isStateProblem, Zugangsfilter.avoidThese(avoidMode),
                                Zielfunktionen.dotEaten(isStateProblem), Heuristikfunktionen.isDeadEndField());
    }

    public static Suchszenario eatUpToNDots(int amount,int startingCnt, Zugangsfilter.AvoidMode avoidMode) {
        return new Suchszenario(true, Zugangsfilter.avoidThese(avoidMode), true, Zielfunktionen.amountOfDotsEaten(amount,startingCnt),
                                Heuristikfunktionen.combine(Heuristikfunktionen.remainingDots(),Heuristikfunktionen.isDeadEndField()));
    }


    /**
     @param avoidMode - Filtern nach "Waenden" oder zusaetzlich "Geistern auf Feld" oder zusaetzlich "+ "Geister neben
     dem Feld"
     */
    public static Suchszenario eatAllDots(Zugangsfilter.AvoidMode avoidMode) {
        return new Suchszenario(true, Zugangsfilter.avoidThese(avoidMode), true, Zielfunktionen.allDotsEaten(),
                                Heuristikfunktionen.remainingDots());
    }

    /**
     HINWEIS: Felder vor Geister werden nicht betreten, es sei denn die Powerpille ist aktiv
     */
    public static Suchszenario findDestination(int goalX, int goalY) {
        return Suchszenario.findDestination(Zugangsfilter.AvoidMode.GHOSTS_THREATENS_FIELD, goalX, goalY);
    }

    /**
     @param avoidMode - Filtern nach "Waenden" oder zusaetzlich "Geistern auf Feld" oder zusaetzlich "+ "Geister neben
     dem Feld"
     */
    public static Suchszenario findDestination(Zugangsfilter.AvoidMode avoidMode, int goalX, int goalY) {
        return Suchszenario.findDestination(true, avoidMode, goalX, goalY);
    }

    /**
     @param isStateProblem - Zusatzinformationen, wie verbleibende Anzahl Dots
     @param avoidMode - Filtern nach "Waenden" oder zusaetzlich "Geistern auf Feld" oder zusaetzlich "+ "Geister neben
     dem Feld"
     */
    public static Suchszenario findDestination(boolean isStateProblem, Zugangsfilter.AvoidMode avoidMode, int goalX,
                                               int goalY) {
        return new Suchszenario(isStateProblem, Zugangsfilter.avoidThese(avoidMode),
                                Zielfunktionen.reachedDestination(goalX, goalY),
                                Heuristikfunktionen.manhattanToTarget(goalX, goalY));
    }

    public static Suchszenario locateDeadEndExit(byte[][] markedAsOneWays) {
        return new Suchszenario(false, Zugangsfilter.merge(Zugangsfilter.noWall(),
                                                           (node, newPosX, newPosY) -> markedAsOneWays[newPosX][newPosY]
                                                                   == 0), true, Zielfunktionen.minimumNeighbours(2),
                                null);
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

    public boolean isNoWait() {
        return noWait;
    }

    public void setNoWait(boolean noWait) {
        this.noWait = noWait;
    }
    // endregion
}
