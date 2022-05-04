package de.fh.stud.Suchen;

import de.fh.kiServer.util.Util;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.Knoten;
import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Suchkomponenten.ClosedList;
import de.fh.stud.Suchen.Suchkomponenten.OpenList;
import de.fh.stud.interfaces.IAccessibilityChecker;
import de.fh.stud.interfaces.ICallbackFunction;
import de.fh.stud.interfaces.IGoalPredicate;
import de.fh.stud.interfaces.IHeuristicFunction;

import javax.swing.*;
import java.util.*;

public class Suche {

    public static int MAX_SOLUTION_LIMIT = Integer.MAX_VALUE;
    public static List<Double> RUN_TIMES = new LinkedList<>();

    private static final boolean SHOW_RESULTS = true;
    private static final boolean PRINT_AVG_RUNTIME = false;

    // TODO: Heuristiken, Kosten, Goal etc. in Suche teilen
    private static boolean STATE_SEARCH = true;
    private boolean noWaitAction;


    // TODO: Das ganze geht kaputt, wenn eine zweite Suche waehrend der ersten Suche stattfindet!!!
    private static IAccessibilityChecker ACCESS_CHECK;
    private static IGoalPredicate GOAL_PRED;
    private static IHeuristicFunction HEURISTIC_FUNC;
    private static ICallbackFunction[] CALLBACK_FUNCS;

    public enum SearchStrategy {
        DEPTH_FIRST, BREADTH_FIRST, GREEDY, UCS, A_STAR
    }

    public Suche(Suchszenario searchScenario) {
        this(searchScenario.isStateProblem(), searchScenario.getAccessCheck(), searchScenario.isNoWait(),
                searchScenario.getGoalPred(), searchScenario.getHeuristicFunc(), searchScenario.getCallbackFuncs());
    }

    public Suche(Suchszenario searchScenario, ICallbackFunction... callbackFunctions) {
        this(searchScenario.isStateProblem(), searchScenario.getAccessCheck(), searchScenario.isNoWait(),
                searchScenario.getGoalPred(), searchScenario.getHeuristicFunc(),
                MyUtil.mergeArrays(searchScenario.getCallbackFuncs(), callbackFunctions));
    }

    public Suche(boolean isStateSearch, IAccessibilityChecker accessCheck, boolean noWaitAction,
                 IGoalPredicate goalPred, IHeuristicFunction heuristicFunc, ICallbackFunction... callbackFunctions) {
        Suche.ACCESS_CHECK = accessCheck;
        Suche.GOAL_PRED = goalPred != null ? goalPred : node -> false;
        Suche.HEURISTIC_FUNC = heuristicFunc != null ? heuristicFunc : node -> 0;
        Suche.CALLBACK_FUNCS = callbackFunctions != null ? callbackFunctions : new ICallbackFunction[]{expCand -> {}};
        Suche.STATE_SEARCH = isStateSearch;
        this.noWaitAction = noWaitAction;
    }

    public Knoten start(PacmanTileType[][] world, int posX, int posY, SearchStrategy strategy) {
        return start(world, posX, posY, strategy, SHOW_RESULTS);
    }

    public Knoten start(PacmanTileType[][] world, int posX, int posY, SearchStrategy strategy, boolean showResults) {
        List<Knoten> ret = start(world, posX, posY, strategy, 1, showResults);
        return ret.size() > 0 ? ret.get(0) : null;
    }

    public List<Knoten> start(PacmanTileType[][] world, int posX, int posY, SearchStrategy strategy,
                              int solutionLimit) {
        return start(world, posX, posY, strategy, solutionLimit, SHOW_RESULTS);
    }

    public List<Knoten> start(PacmanTileType[][] world, int posX, int posY, SearchStrategy strategy,
                              int solutionLimit, boolean showResults) {
        Knoten rootNode = Knoten.generateRoot(world, posX, posY);

        long startTime = System.nanoTime();
        AbstractMap.SimpleEntry<List<Knoten>, Map<String, Double>> searchResult = beginSearch(rootNode,
                OpenList.buildOpenList(strategy), ClosedList.buildClosedList(STATE_SEARCH, world), solutionLimit,
                startTime);

        if (PRINT_AVG_RUNTIME) {
            double elapsedTime = searchResult.getValue().get("Rechenzeit in ms.");
            RUN_TIMES.add(elapsedTime);
            MyUtil.println(String.format("Laufzeit fuer Durchlauf Nr. %d: %.2f ms.\n", RUN_TIMES.size(), elapsedTime));
            MyUtil.println(String.format("Durchschnittliche. Laufzeit: %.2f ms.", RUN_TIMES.stream().reduce(0.0,
                    Double::sum) / RUN_TIMES.size()));
            MyUtil.println("...");
        }
        if (showResults)
            printDebugInfos(strategy, searchResult);

        return searchResult.getKey();
    }

    private AbstractMap.SimpleEntry<List<Knoten>, Map<String, Double>> beginSearch(Knoten startNode,
                                                                                   OpenList openList,
                                                                                   ClosedList closedList,
                                                                                   int solutionLimit, long startTime) {
        openList.add(startNode);
        Knoten expCand;
        List<Knoten> goalNodes = new LinkedList<>();

        while (!openList.isEmpty()) {
            expCand = openList.remove();
            if (expCand.isGoalNode()) {
                goalNodes.add(expCand);
                if (goalNodes.size() >= solutionLimit)
                    break;
            }
            if (!closedList.contains(expCand)) {
                closedList.add(expCand);
                expCand.executeCallbacks();
                expCand.expand(noWaitAction).forEach(openList::add);
            }
        }

        return new AbstractMap.SimpleEntry<>(goalNodes, searchResultInfos(startTime, goalNodes.size(), openList.size(),
                closedList.size()));
    }

    private Map<String, Double> searchResultInfos(long startingTime, int goalListSize,int openListSize, int closedListSize) {
        return new LinkedHashMap<>() {{
            put("Rechenzeit in ms.", Util.timeSince(startingTime));
            put("Anzahl gefundener Loesungen", (double) goalListSize);
            put("Groesse der openList", (double) openListSize);
            put("Groesse der closedList", (double) closedListSize);

        }};
    }

    private void printDebugInfos(SearchStrategy strategy,
                                 AbstractMap.SimpleEntry<List<Knoten>, Map<String, Double>> result) {
        StringBuilder report = new StringBuilder(String.format("""
                Ziel wurde %sgefunden
                Suchalgorithmus: %s
                Suchart: %s
                """, result.getKey().size() != 0 ? "" : "nicht ", strategy, STATE_SEARCH ? "Zustandssuche" : "Wegsuche"));
        for (Map.Entry<String, Double> info_value : result.getValue().entrySet()) {
            // Anhaengende nullen nach dem Komma entfernen
            String val = String.format("%,.3f", info_value.getValue());
            int i = val.length() - 1;
            while (val.charAt(i) == '0')
                i--;
            val = val.substring(0, (val.charAt(i) == ',' ? i : i + 1));

            report.append(String.format("%s: %s\n", info_value.getKey(), val));
        }
        MyUtil.println(report.toString());
        JFrame jf = new JFrame();
        jf.setAlwaysOnTop(true);
        JOptionPane.showMessageDialog(jf, report.toString());
    }

    public static boolean isStateSearch() {
        return STATE_SEARCH;
    }

    public static IAccessibilityChecker getAccessCheck() {
        return ACCESS_CHECK;
    }

    public static void setAccessCheck(IAccessibilityChecker accessibilityChecker) {
        ACCESS_CHECK = accessibilityChecker;
    }

    public static IGoalPredicate getGoalPred() {
        return GOAL_PRED;
    }

    public static IHeuristicFunction getHeuristicFunc() {
        return HEURISTIC_FUNC;
    }

    public static ICallbackFunction[] getCallbackFuncs() {
        return CALLBACK_FUNCS;
    }

    public boolean isNoWaitAction() {
        return noWaitAction;
    }

    public void setNoWaitAction(boolean noWaitAction) {
        this.noWaitAction = noWaitAction;
    }
}
