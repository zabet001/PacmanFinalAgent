package de.fh.stud.Suchen;

import de.fh.kiServer.util.Util;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Suchkomponenten.ClosedList;
import de.fh.stud.Suchen.Suchkomponenten.Knoten;
import de.fh.stud.Suchen.Suchkomponenten.OpenList;
import de.fh.stud.interfaces.IAccessibilityChecker;
import de.fh.stud.interfaces.ICallbackFunction;
import de.fh.stud.interfaces.IGoalPredicate;
import de.fh.stud.interfaces.IHeuristicFunction;

import javax.swing.*;
import java.util.*;

public class Suche {

    public static final int MAX_SOLUTION_LIMIT = Integer.MAX_VALUE;
    private static final boolean SHOW_RESULTS = false;
    private static final boolean PRINT_AVG_RUNTIME = false;

    public static boolean searchRunning = false;
    public static List<Double> runTimes = new LinkedList<>();

    // TODO: Heuristiken, Kosten, Goal etc. in Suche teilen
    //  (z.B. Objekt Knoteninformationen -> Knoten (unidirektionale Assoziation)
    private final boolean stateSearch;
    private final IAccessibilityChecker accessCheck;
    private final IGoalPredicate goalPred;
    private final IHeuristicFunction heuristicFunc;
    private final ICallbackFunction[] callbackFuncs;
    private boolean noWaitAction;

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
        if (Suche.searchRunning) {
            System.err.println("WARNUNG: Eine Suche laeuft bereits!");
        }
        else {
            Suche.searchRunning = true;
        }

        this.noWaitAction = noWaitAction;
        this.stateSearch = isStateSearch;
        this.accessCheck = accessCheck;
        this.goalPred = goalPred != null ? goalPred : node -> false;
        this.heuristicFunc = heuristicFunc != null ? heuristicFunc : node -> 0;
        this.callbackFuncs = callbackFunctions;
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

    public List<Knoten> start(PacmanTileType[][] world, int posX, int posY, SearchStrategy strategy, int solutionLimit,
                              boolean showResults) {
        Knoten rootNode = Knoten.generateRoot(stateSearch, world, posX, posY);

        long startTime = System.nanoTime();
        AbstractMap.SimpleEntry<List<Knoten>, Map<String, Double>> searchResult = beginSearch(rootNode,
                                                                                              OpenList.buildOpenList(
                                                                                                      strategy,
                                                                                                      heuristicFunc),
                                                                                              ClosedList.buildClosedList(
                                                                                                      isStateSearch(),
                                                                                                      world),
                                                                                              solutionLimit, startTime);

        if (PRINT_AVG_RUNTIME) {
            double elapsedTime = searchResult
                    .getValue()
                    .get("Rechenzeit in ms.");
            runTimes.add(elapsedTime);
            MyUtil.println(String.format("Laufzeit fuer Durchlauf Nr. %d: %.2f ms.\n", runTimes.size(), elapsedTime));
            MyUtil.println(String.format("Durchschnittliche. Laufzeit: %.2f ms.", runTimes
                    .stream()
                    .reduce(0.0, Double::sum) / runTimes.size()));
            MyUtil.println("...");
        }
        if (showResults) {
            printDebugInfos(strategy, searchResult);
        }

        Suche.searchRunning = false;
        return searchResult.getKey();
    }

    private AbstractMap.SimpleEntry<List<Knoten>, Map<String, Double>> beginSearch(Knoten startNode, OpenList openList,
                                                                                   ClosedList closedList,
                                                                                   int solutionLimit, long startTime) {
        openList.add(startNode);
        Knoten expCand;
        List<Knoten> goalNodes = new LinkedList<>();

        while (!openList.isEmpty()) {
            expCand = openList.remove();

            if (expCand.isGoalNode(goalPred)) {
                goalNodes.add(expCand);
                if (goalNodes.size() >= solutionLimit) {
                    break;
                }
            }
            if (!closedList.contains(expCand)) {
                closedList.add(expCand);
                expCand.executeCallbacks(callbackFuncs);
                expCand
                        .expand(stateSearch, noWaitAction, accessCheck)
                        .forEach(openList::add);//(openList::add);
            }
        }

        return new AbstractMap.SimpleEntry<>(goalNodes, searchResultInfos(startTime, goalNodes.size(), openList.size(),
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

    private void printDebugInfos(SearchStrategy strategy,
                                 AbstractMap.SimpleEntry<List<Knoten>, Map<String, Double>> result) {
        StringBuilder report = new StringBuilder(String.format("""
                                                                       Ziel wurde %sgefunden
                                                                       Suchalgorithmus: %s
                                                                       Suchart: %s
                                                                       """, result
                                                                       .getKey()
                                                                       .size() != 0 ? "" : "nicht ", strategy,
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
        MyUtil.println(report.toString());
        JFrame jf = new JFrame();
        jf.setAlwaysOnTop(true);
        JOptionPane.showMessageDialog(jf, report.toString());
    }

    public boolean isStateSearch() {
        return stateSearch;
    }

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

    public boolean isNoWaitAction() {
        return noWaitAction;
    }

    public void setNoWaitAction(boolean noWaitAction) {
        this.noWaitAction = noWaitAction;
    }

    public enum SearchStrategy {
        DEPTH_FIRST, BREADTH_FIRST, GREEDY, UCS, A_STAR
    }
}
