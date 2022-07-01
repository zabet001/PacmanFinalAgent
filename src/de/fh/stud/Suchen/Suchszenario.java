package de.fh.stud.Suchen;

import de.fh.stud.GameStateObserver;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.IAccessibilityChecker;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.ICallbackFunction;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.IGoalPredicate;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.IHeuristicFunction;

public class Suchszenario {
	private final IAccessibilityChecker[] accessChecks;
	private final IGoalPredicate goalPred;
	private final IHeuristicFunction[] heuristicFuncs;
	private final ICallbackFunction[] callbackFuncs;
	private final boolean stateProblem;
	private final boolean withWaitAction;

	public static final class SuchszenarioBuilder {
		private IAccessibilityChecker[] accessChecks;
		private IGoalPredicate goalPred;
		private IHeuristicFunction[] heuristicFuncs;
		private ICallbackFunction[] callbackFuncs;
		private boolean stateProblem = true;
		private boolean withWaitAction = true;

		private SuchszenarioBuilder setAccessChecks(IAccessibilityChecker... accessChecks) {
			this.accessChecks = accessChecks;
			return this;
		}

		private SuchszenarioBuilder setGoalPred(IGoalPredicate goalPred) {
			this.goalPred = goalPred;
			return this;
		}

		private SuchszenarioBuilder setHeuristicFuncs(IHeuristicFunction... heuristicFuncs) {
			this.heuristicFuncs = heuristicFuncs;
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
			if (accessChecks == null) {
				throw new IllegalArgumentException("Missing " + IAccessibilityChecker.class.getSimpleName());
			}
			return new Suchszenario(this);
		}
	}

	private Suchszenario(SuchszenarioBuilder b) {
		this.accessChecks = b.accessChecks;
		this.goalPred = b.goalPred;
		this.heuristicFuncs = b.heuristicFuncs;
		this.callbackFuncs = b.callbackFuncs;

		this.stateProblem = b.stateProblem;
		this.withWaitAction = b.withWaitAction;
	}

	public static Suchszenario runAway(IAccessibilityChecker.AvoidMode avoidMode, int startPosX, int startPosY) {
		return new SuchszenarioBuilder()
				.setAccessChecks(IAccessibilityChecker.avoidThese(avoidMode))
				.setGoalPred(node -> IGoalPredicate.didAnAction(node, startPosX, startPosY))
				.setHeuristicFuncs(IHeuristicFunction::deadEndDepth,
								   node -> IHeuristicFunction.invGhostsDistsAsc(node,
																				GameStateObserver
																						.getGameState()
																						.getNewPercept()
																						.getGhostInfos()),
								   IHeuristicFunction::isDot)
				.build();

	}

	public static Suchszenario eatAllDots(IAccessibilityChecker.AvoidMode avoidMode) {
		return new SuchszenarioBuilder()
				.setAccessChecks(IAccessibilityChecker.avoidThese(avoidMode))
				.setGoalPred(IGoalPredicate::allDotsEaten)
				.setHeuristicFuncs(IHeuristicFunction::remainingDots)
				.build();
	}

	public static Suchszenario eatNearestPowerpill(IAccessibilityChecker.AvoidMode avoidMode) {
		return new SuchszenarioBuilder()
				.setAccessChecks(IAccessibilityChecker.avoidThese(avoidMode))
				.setGoalPred(IGoalPredicate::powerpillEaten)
				.setHeuristicFuncs(IHeuristicFunction::normedInvGhostsDistsAsc)
				.build();
	}

	public static Suchszenario eatNearestDot(IAccessibilityChecker.AvoidMode avoidMode) {
		return new SuchszenarioBuilder()
				.setGoalPred(IGoalPredicate::dotEaten)
				.setAccessChecks(IAccessibilityChecker.avoidThese(avoidMode))
				.setHeuristicFuncs(
						// Sackgassen bevorzugen, wenn sie komplett abgefressen werden koennen
						node -> IHeuristicFunction.canClearDeadEnd(node,
																   GameStateObserver
																		   .getGameState()
																		   .getNewPercept()
																		   .getGhostInfos()),
						// Powerpillen nicht grundlos fressen
						node -> 1 - IHeuristicFunction.isPowerpill(node),
						// Abstand zu Geistern vergroessern
						IHeuristicFunction::normedInvGhostsDistsAsc)
				.build();
	}

	public static Suchszenario eatUpToNDots(int amount, int currentDotAmount,
											IAccessibilityChecker.AvoidMode avoidMode) {
		return new SuchszenarioBuilder()
				.setAccessChecks(IAccessibilityChecker.avoidThese(avoidMode))
				.setGoalPred(node -> IGoalPredicate.amountOfDotsEaten(node, amount, currentDotAmount))

				.setHeuristicFuncs(node -> IHeuristicFunction.normedRemainingDots(node, currentDotAmount),
								   IHeuristicFunction::normedInvGhostsDistsAsc,
								   IHeuristicFunction::deadEndDepth)
				.build();
	}

	public static Suchszenario reachDestination(IAccessibilityChecker.AvoidMode avoidMode, int goalX, int goalY) {
		return new SuchszenarioBuilder()
				.setAccessChecks(IAccessibilityChecker.avoidThese(avoidMode))
				.setGoalPred(node -> IGoalPredicate.reachedDestination(node, goalX, goalY))
				.setHeuristicFuncs(node -> IHeuristicFunction.manhattanDist(node, goalX, goalY))
				.setStateProblem(false)
				.setWithWaitAction(false)
				.build();
	}

	// region getter

	public IAccessibilityChecker[] getAccessChecks() {
		return accessChecks;
	}

	public IGoalPredicate getGoalPred() {
		return goalPred;
	}

	public IHeuristicFunction[] getHeuristicFuncs() {
		return heuristicFuncs;
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
