package de.fh.stud.Suchen;

import de.fh.stud.GameStateObserver;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.Heuristikfunktionen;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.Zielfunktionen;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.Zugangsfilter;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.IAccessibilityChecker;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.ICallbackFunction;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.IGoalPredicate;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.IHeuristicFunction;

public class Suchszenario {
    private final IAccessibilityChecker[] accessCheck;
    private final IGoalPredicate goalPred;
    private final IHeuristicFunction heuristicFunc;
    private final ICallbackFunction[] callbackFuncs;
    private final boolean stateProblem;
    private final boolean withWaitAction;

    public static final class SuchszenarioBuilder {
        private IAccessibilityChecker[] accessCheck;
        private IGoalPredicate goalPred;
        private IHeuristicFunction heuristicFunc;
        private ICallbackFunction[] callbackFuncs;
        private boolean stateProblem = true;
        private boolean withWaitAction = true;

        private SuchszenarioBuilder setAccessCheck(IAccessibilityChecker... accessCheck) {
            this.accessCheck = accessCheck;
            return this;
        }

        private SuchszenarioBuilder setGoalPred(IGoalPredicate goalPred) {
            this.goalPred = goalPred;
            return this;
        }

        private SuchszenarioBuilder setHeuristicFunc(IHeuristicFunction heuristicFunc) {
            this.heuristicFunc = heuristicFunc;
            return this;
        }

        private SuchszenarioBuilder setCallbackFuncs(ICallbackFunction... callbackFuncs) {
            this.callbackFuncs = callbackFuncs;
            return this;
        }

        private SuchszenarioBuilder setStateProblem(boolean stateProblem) {
            this.stateProblem = stateProblem;
            return this;
        }

        private SuchszenarioBuilder setWithWaitAction(boolean withWaitAction) {
            this.withWaitAction = withWaitAction;
            return this;
        }

        public Suchszenario build() {
            if (accessCheck == null) {
                throw new IllegalArgumentException("Missing " + IAccessibilityChecker.class.getSimpleName());
            }
            return new Suchszenario(this);
        }
    }

    // region Konstruktoren
    private Suchszenario(SuchszenarioBuilder b) {
        this.accessCheck = b.accessCheck;
        this.goalPred = b.goalPred;
        this.heuristicFunc = b.heuristicFunc;
        this.callbackFuncs = b.callbackFuncs;

        this.stateProblem = b.stateProblem;
        this.withWaitAction = b.withWaitAction;
    }

    public Suchszenario(IAccessibilityChecker[] accessCheck, IGoalPredicate goalPred, IHeuristicFunction heuristicFunc) {
        this(true, accessCheck, true, goalPred, heuristicFunc, (ICallbackFunction[]) null);
    }

    public Suchszenario(boolean stateProblem, IAccessibilityChecker[] accessCheck, IGoalPredicate goalPred,
                        IHeuristicFunction heuristicFunc) {
        this(stateProblem, accessCheck, true, goalPred, heuristicFunc, (ICallbackFunction[]) null);
    }

    public Suchszenario(boolean stateProblem, IAccessibilityChecker[] accessCheck, boolean withWaitAction,
                        IGoalPredicate goalPred, IHeuristicFunction heuristicFunc) {
        this(stateProblem, accessCheck, withWaitAction, goalPred, heuristicFunc, (ICallbackFunction[]) null);

    }

    public Suchszenario(boolean stateProblem, IAccessibilityChecker[] accessCheck, boolean withWaitAction,
                        IGoalPredicate goalPred, IHeuristicFunction heuristicFunc, ICallbackFunction... callbackFuncs) {
        this.stateProblem = stateProblem;
        this.accessCheck = accessCheck;
        this.withWaitAction = withWaitAction;
        this.goalPred = goalPred;
        this.heuristicFunc = heuristicFunc;
        this.callbackFuncs = callbackFuncs;
    }

    //endregion

    public static Suchszenario runAway(Zugangsfilter.AvoidMode avoidMode, int startPosX, int startPosY) {
        return new SuchszenarioBuilder()
                .setAccessCheck(Zugangsfilter.avoidThese(avoidMode))
                .setGoalPred(Zielfunktionen.didAnAction(startPosX, startPosY))
                .setHeuristicFunc(Heuristikfunktionen.distanceToCloserGhosts(GameStateObserver
                                                                                     .getGameState()
                                                                                     .getNewPercept()
                                                                                     .getGhostInfos()))
                .build();
    }

    public static Suchszenario eatAllDots(Zugangsfilter.AvoidMode avoidMode) {
        return new SuchszenarioBuilder()
                .setAccessCheck(Zugangsfilter.avoidThese(avoidMode))
                .setGoalPred(Zielfunktionen.allDotsEaten())
                .setHeuristicFunc(Heuristikfunktionen.remainingDots())
                .build();
    }


    public static Suchszenario eatNearestPowerpill(Zugangsfilter.AvoidMode avoidMode) {
        return new SuchszenarioBuilder()
                .setAccessCheck(Zugangsfilter.avoidThese(avoidMode))
                .setGoalPred(Zielfunktionen.powerpillEaten())
                .build();
    }

    public static Suchszenario eatNearestDot(Zugangsfilter.AvoidMode avoidMode) {
        return new SuchszenarioBuilder()
                .setGoalPred(Zielfunktionen.dotEaten())
                .setAccessCheck(Zugangsfilter.avoidThese(avoidMode))
                // .setHeuristicFunc(Heuristikfunktionen.isDeadEndField())
                .build();
    }

    public static Suchszenario eatUpToNDots(int amount, int currentDotAmount, Zugangsfilter.AvoidMode avoidMode) {
        return new SuchszenarioBuilder()
                .setAccessCheck(Zugangsfilter.avoidThese(avoidMode))
                .setGoalPred(Zielfunktionen.amountOfDotsEaten(amount, currentDotAmount))
                .setHeuristicFunc(Heuristikfunktionen.remainingDots())
                .build();
    }

    public static Suchszenario reachDestination(Zugangsfilter.AvoidMode avoidMode, int goalX, int goalY) {
        return new SuchszenarioBuilder()
                .setAccessCheck(Zugangsfilter.avoidThese(avoidMode))
                .setGoalPred(Zielfunktionen.reachedDestination(goalX, goalY))
                .setHeuristicFunc(Heuristikfunktionen.manhattanToTarget(goalX, goalY))
                .setStateProblem(false)
                .setWithWaitAction(false)
                .build();
    }

    public static Suchszenario locateDeadEndExit() {
        return new SuchszenarioBuilder()
                .setStateProblem(false)
                .setWithWaitAction(false)
                .setAccessCheck(Zugangsfilter.noWall())
                .setGoalPred(node -> Sackgassen.deadEndDepth[node.getPosX()][node.getPosY()] == 0)
                .build();
    }
    // region getter

    public IAccessibilityChecker[] getAccessCheck() {
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
        return stateProblem;
    }

    public boolean isWithWaitAction() {
        return withWaitAction;
    }

    // endregion
}
