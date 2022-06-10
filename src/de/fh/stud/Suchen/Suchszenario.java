package de.fh.stud.Suchen;

import de.fh.stud.GameStateObserver;
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

	public Suchszenario(IAccessibilityChecker[] accessCheck, IGoalPredicate goalPred,
						IHeuristicFunction heuristicFunc) {
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
						IGoalPredicate goalPred, IHeuristicFunction heuristicFunc,
						ICallbackFunction... callbackFuncs) {
		this.stateProblem = stateProblem;
		this.accessCheck = accessCheck;
		this.withWaitAction = withWaitAction;
		this.goalPred = goalPred;
		this.heuristicFunc = heuristicFunc;
		this.callbackFuncs = callbackFuncs;
	}

	//endregion

	public static Suchszenario runAway(IAccessibilityChecker.AvoidMode avoidMode, int startPosX, int startPosY) {
		return new SuchszenarioBuilder()
				.setAccessCheck(IAccessibilityChecker.avoidThese(avoidMode))
				.setGoalPred(node -> IGoalPredicate.didAnAction(node, startPosX, startPosY))
				.setHeuristicFunc(node -> IHeuristicFunction.distanceToCloserGhosts(node, GameStateObserver
						.getGameState()
						.getNewPercept()
						.getGhostInfos()))
				.build();
	}

	public static Suchszenario eatAllDots(IAccessibilityChecker.AvoidMode avoidMode) {
		return new SuchszenarioBuilder()
				.setAccessCheck(IAccessibilityChecker.avoidThese(avoidMode))
				.setGoalPred(IGoalPredicate::allDotsEaten)
				.setHeuristicFunc(IHeuristicFunction::remainingDots)
				.build();
	}

	public static Suchszenario eatNearestPowerpill(IAccessibilityChecker.AvoidMode avoidMode) {
		return new SuchszenarioBuilder()
				.setAccessCheck(IAccessibilityChecker.avoidThese(avoidMode))
				.setGoalPred(IGoalPredicate::powerpillEaten)
				.build();
	}

	public static Suchszenario eatNearestDot(IAccessibilityChecker.AvoidMode avoidMode) {
		return new SuchszenarioBuilder()
				.setGoalPred(IGoalPredicate::dotEaten)
				.setAccessCheck(IAccessibilityChecker.avoidThese(avoidMode))
				// .setHeuristicFunc(Heuristikfunktionen.isDeadEndField())
				.build();
	}

	public static Suchszenario eatUpToNDots(int amount, int currentDotAmount,
											IAccessibilityChecker.AvoidMode avoidMode) {
		return new SuchszenarioBuilder()
				.setAccessCheck(IAccessibilityChecker.avoidThese(avoidMode))
				.setGoalPred(node -> IGoalPredicate.amountOfDotsEaten(node, amount, currentDotAmount))
				.setHeuristicFunc(IHeuristicFunction::remainingDots)
				.build();
	}

	public static Suchszenario reachDestination(IAccessibilityChecker.AvoidMode avoidMode, int goalX, int goalY) {
		return new SuchszenarioBuilder()
				.setAccessCheck(IAccessibilityChecker.avoidThese(avoidMode))
				.setGoalPred(node -> IGoalPredicate.reachedDestination(node, goalX, goalY))
				.setHeuristicFunc(node -> IHeuristicFunction.manhattanToTarget(node, goalX, goalY))
				.setStateProblem(false)
				.setWithWaitAction(false)
				.build();
	}

	public static Suchszenario locateDeadEndExit() {
		return new SuchszenarioBuilder()
				.setStateProblem(false)
				.setWithWaitAction(false)
				.setAccessCheck(IAccessibilityChecker::noWall)
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
