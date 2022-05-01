package de.fh.stud.Suchen;

import de.fh.kiServer.util.Util;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.Knoten;
import de.fh.stud.MyUtil;
import de.fh.stud.interfaces.IAccessibilityChecker;
import de.fh.stud.interfaces.ICallbackFunction;
import de.fh.stud.interfaces.IGoalPredicate;
import de.fh.stud.interfaces.IHeuristicFunction;

import javax.swing.*;
import java.util.*;

public class Suche {

    public static List<Double> RUN_TIMES = new LinkedList<>();

    private static boolean SHOW_RESULTS = true;
    private static boolean PRINT_AVG_RUNTIME = false;

    // TODO: Heuristiken, Kosten, Goal etc. in Suche teilen
    private static boolean STATE_SEARCH = true;
    private static IAccessibilityChecker ACCESS_CHECK;


    private static IGoalPredicate GOAL_PRED;
    private static IHeuristicFunction HEURISTIC_FUNC;
    private static ICallbackFunction[] CALLBACK_FUNCS;

    public Suche(boolean isStateSearch, IAccessibilityChecker accessCheck, IGoalPredicate goalPred,
                 IHeuristicFunction heuristicFunc, ICallbackFunction... callbackFunctions) {
        Suche.ACCESS_CHECK = accessCheck;
        Suche.GOAL_PRED = goalPred != null ? goalPred : node -> false;
        Suche.HEURISTIC_FUNC = heuristicFunc != null ? heuristicFunc : node -> 0;
        Suche.CALLBACK_FUNCS = callbackFunctions != null ? callbackFunctions : new ICallbackFunction[]{expCand -> {}};
        Suche.STATE_SEARCH = isStateSearch;
    }

    public Suche(Suchszenario searchScenario) {
        this(searchScenario.isStateProblem(), searchScenario.getAccessCheck(), searchScenario.getGoalPred(),
                searchScenario.getHeuristicFunc(), searchScenario.getCallbackFuncs());
    }

    public Suche(Suchszenario searchScenario, ICallbackFunction... callbackFunctions) {
        this(searchScenario.isStateProblem(), searchScenario.getAccessCheck(), searchScenario.getGoalPred(),
                searchScenario.getHeuristicFunc(), MyUtil.mergeArrays(searchScenario.getCallbackFuncs(),
                        callbackFunctions));
    }

    public enum SearchStrategy {
        DEPTH_FIRST, BREADTH_FIRST, GREEDY, UCS, A_STAR
    }

    public Knoten start(PacmanTileType[][] world, int posX, int posY, SearchStrategy strategy, boolean showResults) {
        Knoten rootNode = Knoten.generateRoot(world,posX,posY);

        long startTime = System.nanoTime();
        AbstractMap.SimpleEntry<Knoten, Map<String, Double>> searchResult = switch (strategy) {
            case DEPTH_FIRST, BREADTH_FIRST -> uninformedSearch(rootNode, strategy, startTime);
            default -> informedSearch(rootNode, strategy, startTime);
        };

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

    public Knoten start(PacmanTileType[][] world, int posX, int posY, SearchStrategy strategy) {
        return start(world,posX,posY, strategy, SHOW_RESULTS);
    }

    // TODO: Eine Loesung finden, um NICHT doppelten Code zu schreiben!
    private AbstractMap.SimpleEntry<Knoten, Map<String, Double>> informedSearch(Knoten startNode,
                                                                                SearchStrategy strategy,
                                                                                long startTime) {
        HashSet<Knoten> closedList = new HashSet<>();
        PriorityQueue<Knoten> openList = new PriorityQueue<>(getInsertionCriteria(strategy));
        openList.add(startNode);
        Knoten expCand;
        Knoten goalNode = null;

        while (!openList.isEmpty()) {
            expCand = openList.remove();
            if (expCand.isGoalNode()) {
                goalNode = expCand;
                break;
            }
            if (!closedList.contains(expCand)) {
                closedList.add(expCand);
                expCand.executeCallbacks();
                openList.addAll(expCand.expand());
            }
        }

        return new AbstractMap.SimpleEntry<>(goalNode, searchResultInfos(startTime, openList.size(),
                closedList.size()));
    }

    private AbstractMap.SimpleEntry<Knoten, Map<String, Double>> uninformedSearch(Knoten startNode,
                                                                                  SearchStrategy strategy,
                                                                                  long startTime) {
        HashSet<Knoten> closedList = new HashSet<>();
        List<Knoten> openList = new LinkedList<>();
        openList.add(startNode);
        Knoten expCand;
        Knoten goalNode = null;

        while (!openList.isEmpty()) {
            expCand = openList.remove(0);
            if (expCand.isGoalNode()) {
                goalNode = expCand;
                break;
            }
            if (!closedList.contains(expCand)) {

                closedList.add(expCand);
                expCand.executeCallbacks();
                expCand.expand().forEach(child -> addToOpenList(strategy, openList, child));
            }
        }
        return new AbstractMap.SimpleEntry<>(goalNode, searchResultInfos(startTime, openList.size(),
                closedList.size()));
    }

    private Map<String, Double> searchResultInfos(long startingTime, int openListSize, int closedListSize) {
        return new LinkedHashMap<>() {{
            put("Rechenzeit in ms.", Util.timeSince(startingTime));
            put("Groesse der openList", (double) openListSize);
            put("Groesse der closedList", (double) closedListSize);
        }};
    }

    private void printDebugInfos(SearchStrategy strategy, AbstractMap.SimpleEntry<Knoten, Map<String, Double>> result) {
        StringBuilder report = new StringBuilder(String.format("""
                Ziel wurde %sgefunden
                Suchalgorithmus: %s
                Suchart: %s
                """, result.getKey() != null ? "" : "nicht ", strategy, STATE_SEARCH ? "Zustandssuche" : "Wegsuche"
        ));
        for (Map.Entry<String, Double> info_value : result.getValue().entrySet()) {
            report.append(String.format("%s: %,.3f\n", info_value.getKey(), info_value.getValue()));
        }
        MyUtil.println(report.toString());
        JFrame jf = new JFrame();
        jf.setAlwaysOnTop(true);
        JOptionPane.showMessageDialog(jf, report.toString());
    }

    private Comparator<Knoten> getInsertionCriteria(SearchStrategy strategy) {
        return switch (strategy) {
            case GREEDY -> Comparator.comparingInt(Knoten::getHeuristic);
            case UCS -> Comparator.comparingInt(Knoten::getCost);
            case A_STAR -> Comparator.comparingInt(a -> a.getCost() + a.getHeuristic());
            default -> null;
        };
    }

    private void addToOpenList(SearchStrategy strategy, List<Knoten> openList, Knoten child) {
        switch (strategy) {
            case DEPTH_FIRST -> openList.add(0, child);
            case BREADTH_FIRST -> openList.add(child);
        }
    }

    public static boolean isStateSearch() {
        return STATE_SEARCH;
    }

    public static IAccessibilityChecker getAccessCheck() {
        return ACCESS_CHECK;
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
}
