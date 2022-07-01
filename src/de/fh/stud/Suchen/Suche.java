package de.fh.stud.Suchen;

import de.fh.kiServer.util.Util;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Suchkomponenten.ClosedList;
import de.fh.stud.Suchen.Suchkomponenten.Knoten;
import de.fh.stud.Suchen.Suchkomponenten.OpenList;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.IAccessibilityChecker;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.ICallbackFunction;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.IGoalPredicate;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.IHeuristicFunction;

import javax.swing.*;
import java.util.*;

public class Suche {

	public static final int MAX_SOLUTION_LIMIT = Integer.MAX_VALUE;
	public static List<Double> runTimes = new LinkedList<>();

	public enum SearchStrategy {
		DEPTH_FIRST, BREADTH_FIRST, GREEDY, UCS, A_STAR
	}

	private final boolean displayResults;
	private final boolean printResults;

	private final boolean stateSearch;
	private final boolean withWaitAction;
	private final int solutionLimit;

	private final IAccessibilityChecker[] accessChecks;
	private final IGoalPredicate goalPred;
	private final IHeuristicFunction[] heuristicFuncs;
	private final ICallbackFunction[] callbackFuncs;

	public static final class SucheBuilder {
		private boolean displayResults = false;
		private boolean printResults = false;
		private boolean stateSearch = true;
		private boolean withWaitAction = true;
		private int solutionLimit = 1;

		private IAccessibilityChecker[] accessChecks;
		private IGoalPredicate goalPred;
		private IHeuristicFunction[] heuristicFuncs;
		private ICallbackFunction[] callbackFuncs;

		public SucheBuilder() {}

		public SucheBuilder(Suchszenario scenario) {
			this.stateSearch = scenario.isStateProblem();
			this.accessChecks = scenario.getAccessChecks();
			this.goalPred = scenario.getGoalPred();
			this.heuristicFuncs = scenario.getHeuristicFuncs();
			this.callbackFuncs = scenario.getCallbackFuncs();
		}

		public SucheBuilder setDisplayResults(boolean displayResults) {
			this.displayResults = displayResults;
			return this;
		}

		public SucheBuilder setPrintResults(boolean printResults) {
			this.printResults = printResults;
			return this;

		}

		public SucheBuilder setStateSearch(boolean stateSearch) {
			this.stateSearch = stateSearch;
			return this;

		}

		public SucheBuilder setAccessChecks(IAccessibilityChecker... accessChecks) {
			this.accessChecks = accessChecks;
			return this;
		}

		public SucheBuilder additionalAccessChecks(IAccessibilityChecker... accessChecks) {
			this.accessChecks = MyUtil.mergeArrays(this.accessChecks, accessChecks);
			return this;
		}

		public SucheBuilder setGoalPred(IGoalPredicate goalPred) {
			this.goalPred = goalPred;
			return this;

		}

		public SucheBuilder setHeuristicFuncs(IHeuristicFunction... heuristicFuncs) {
			this.heuristicFuncs = heuristicFuncs;
			return this;
		}

		public SucheBuilder additionalHeuristicFuncs(IHeuristicFunction... heuristicFuncs) {
			this.heuristicFuncs = MyUtil.mergeArrays(this.heuristicFuncs, heuristicFuncs);
			return this;
		}

		public SucheBuilder setCallbackFuncs(ICallbackFunction... callbackFuncs) {
			this.callbackFuncs = callbackFuncs;
			return this;

		}

		public SucheBuilder additionalCallbackFuncs(ICallbackFunction... callbackFuncs) {
			this.callbackFuncs = MyUtil.mergeArrays(this.callbackFuncs, callbackFuncs);
			return this;
		}

		public SucheBuilder setSolutionLimit(int solutionLimit) {
			this.solutionLimit = solutionLimit;
			return this;
		}

		public SucheBuilder noWaitAction() {
			this.withWaitAction = false;
			return this;

		}

		public SucheBuilder noSolutionLimit() {
			this.solutionLimit = 0;
			return this;
		}

		public Suche createSuche() {
			if (accessChecks == null) {
				throw new IllegalArgumentException("Missing " + IAccessibilityChecker.class.getSimpleName());
			}
			if (solutionLimit < 0) {
				throw new IllegalArgumentException("Search Limit cannot be lower than 0");
			}
			if (goalPred == null) {
				goalPred = node -> false;
			}
			if (heuristicFuncs == null) {
				heuristicFuncs = new IHeuristicFunction[]{node -> 0};
			}
			if (callbackFuncs == null) {
				callbackFuncs = new ICallbackFunction[]{expCand -> {}};
			}

			return new Suche(this);
		}
	}

	private Suche(SucheBuilder b) {
		this.displayResults = b.displayResults;
		this.printResults = b.printResults;

		this.solutionLimit = b.solutionLimit;
		this.stateSearch = b.stateSearch;
		this.withWaitAction = b.withWaitAction;

		this.accessChecks = b.accessChecks;
		this.goalPred = b.goalPred;
		this.heuristicFuncs = b.heuristicFuncs;
		this.callbackFuncs = b.callbackFuncs;
	}

	public Knoten startFirstSolution(PacmanTileType[][] world, int posX, int posY, SearchStrategy strategy) {
		List<Knoten> ret = start(world, posX, posY, strategy);
		return ret.size() > 0 ? ret.get(0) : null;
	}

	public Knoten startFirstSolution(byte[][] world, int posX, int posY, SearchStrategy strategy) {
		List<Knoten> ret = start(world, posX, posY, strategy);
		return ret.size() > 0 ? ret.get(0) : null;
	}

	public List<Knoten> start(PacmanTileType[][] world, int posX, int posY, SearchStrategy strategy) {
		return start(MyUtil.createByteView(world), posX, posY, strategy);
	}

	public List<Knoten> start(byte[][] world, int posX, int posY, SearchStrategy strategy) {
		Knoten rootNode = Knoten.generateRoot(stateSearch, world, posX, posY);

		long startTime = System.nanoTime();
		AbstractMap.SimpleEntry<List<Knoten>, Map<String, Double>> searchResult = beginSearch(rootNode,
																							  OpenList.buildOpenList(
																									  strategy,
																									  heuristicFuncs),
																							  ClosedList.buildClosedList(
																									  isStateSearch(),
																									  world),
																							  startTime);

		if (printResults) {
			System.out.println(debugInfos(strategy, searchResult));
		}
		if (displayResults) {
			JFrame jf = new JFrame();
			jf.setAlwaysOnTop(true);
			JOptionPane.showMessageDialog(jf, debugInfos(strategy, searchResult));

		}
		return searchResult.getKey();
	}

	private AbstractMap.SimpleEntry<List<Knoten>, Map<String, Double>> beginSearch(Knoten startNode, OpenList openList,
																				   ClosedList closedList,
																				   long startTime) {
		openList.add(startNode);
		Knoten expCand;
		List<Knoten> goalNodes = new LinkedList<>();

		while (!openList.isEmpty()) {
			expCand = openList.remove();
			if (expCand.isGoalNode(goalPred)) {
				goalNodes.add(expCand);
				if (solutionLimit != 0 && goalNodes.size() >= solutionLimit) {
					break;
				}
			}
			if (!closedList.contains(expCand)) {
				closedList.add(expCand);
				expCand.executeCallbacks(callbackFuncs);
				expCand
						.expand(stateSearch, withWaitAction, accessChecks)
						.forEach(openList::add);//(openList::add);
			}
		}

		return new AbstractMap.SimpleEntry<>(goalNodes,
											 searchResultInfos(startTime,
															   goalNodes.size(),
															   openList.size(),
															   closedList.size()));
	}

	private Map<String, Double> searchResultInfos(long startingTime, int goalListSize, int openListSize,
												  int closedListSize) {
		return new LinkedHashMap<>() {{
			put("Rechenzeit in ms.", Util.timeSince(startingTime));
			put("Anzahl gefundener Loesungen", (double) goalListSize);
			put("Groesse der openList", (double) openListSize);
			put("Groesse der closedList", (double) closedListSize);

		}};
	}

	private String debugInfos(SearchStrategy strategy,
							  AbstractMap.SimpleEntry<List<Knoten>, Map<String, Double>> result) {
		StringBuilder report = new StringBuilder(String.format("""
																	   Ziel wurde %sgefunden
																	   Suchalgorithmus: %s
																	   Suchart: %s
																	   """,
															   result
																	   .getKey()
																	   .size() != 0 ? "" : "nicht ",
															   strategy,
															   isStateSearch() ? "Zustandssuche" : "Wegsuche"));
		for (Map.Entry<String, Double> info_value : result
				.getValue()
				.entrySet()) {
			// Anhaengende nullen nach dem Komma entfernen
			String val = String.format("%,.3f", info_value.getValue());
			int i = val.length() - 1;
			while (val.charAt(i) == '0')
				i--;
			val = val.substring(0, (val.charAt(i) == ',' ? i : i + 1));

			report.append(String.format("%s: %s\n", info_value.getKey(), val));
		}
		return report.toString();
	}

	public boolean isStateSearch() {
		return stateSearch;
	}

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

	public boolean isWithWaitAction() {
		return withWaitAction;
	}

}
