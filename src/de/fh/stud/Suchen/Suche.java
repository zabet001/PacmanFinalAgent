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

    public static final int MAX_SOLUTION_LIMIT = Integer.MAX_VALUE;
    private static final boolean SHOW_RESULTS = true;
    private static final boolean PRINT_AVG_RUNTIME = false;

    public static boolean searchRunning = false;
    public static List<Double> runTimes = new LinkedList<>();

    // TODO: Heuristiken, Kosten, Goal etc. in Suche teilen
    //  (z.B. Objekt Knoteninformationen -> Knoten (unidirektionale Assoziation)

    private static SearchArgs currentSearchArgs;

    private boolean noWaitAction;

    private static class SearchArgs {

        // TODO: Das ganze geht kaputt, wenn eine zweite Suche waehrend der ersten Suche stattfindet und nicht
        //  zwischengespeichert wird !!!
        private boolean stateSearch;
        private IAccessibilityChecker accessCheck;
        private IGoalPredicate goalPred;
        private IHeuristicFunction heuristicFunc;
        private ICallbackFunction[] callbackFuncs;

        private SearchArgs(boolean stateSearch, IAccessibilityChecker accessCheck, IGoalPredicate goalPred,
                           IHeuristicFunction heuristicFunc, ICallbackFunction[] callbackFuncs) {
            this.stateSearch = stateSearch;
            this.accessCheck = accessCheck;
            this.goalPred = goalPred;
            this.heuristicFunc = heuristicFunc;
            this.callbackFuncs = callbackFuncs;
        }

        private SearchArgs(SearchArgs origArgs) {
            this(origArgs.stateSearch, origArgs.accessCheck, origArgs.goalPred, origArgs.heuristicFunc,
                 origArgs.callbackFuncs);
        }
    }

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
        if (Suche.searchRunning) {
            System.err.println("WARNUNG: Eine Suche laeuft bereits!");
        }
        else {
            Suche.searchRunning = true;
        }

        this.noWaitAction = noWaitAction;
        Suche.currentSearchArgs = new SearchArgs(isStateSearch, accessCheck,
                                                 goalPred != null ? goalPred : node -> false,
                                                 heuristicFunc != null ? heuristicFunc : node -> 0, callbackFunctions
                                                         != null ? callbackFunctions :
                                                         new ICallbackFunction[]{expCand -> {}});
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
        Knoten rootNode = Knoten.generateRoot(world, posX, posY);

        long startTime = System.nanoTime();
        AbstractMap.SimpleEntry<List<Knoten>, Map<String, Double>> searchResult = beginSearch(rootNode,
                                                                                              OpenList.buildOpenList(
                                                                                                      strategy),
                                                                                              ClosedList.buildClosedList(
                                                                                                      isStateSearch(),
                                                                                                      world),
                                                                                              solutionLimit, startTime);

        if (PRINT_AVG_RUNTIME) {
            double elapsedTime = searchResult.getValue().get("Rechenzeit in ms.");
            runTimes.add(elapsedTime);
            MyUtil.println(String.format("Laufzeit fuer Durchlauf Nr. %d: %.2f ms.\n", runTimes.size(), elapsedTime));
            MyUtil.println(String.format("Durchschnittliche. Laufzeit: %.2f ms.",
                                         runTimes.stream().reduce(0.0, Double::sum) / runTimes.size()));
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
            if (expCand.isGoalNode()) {
                goalNodes.add(expCand);
                if (goalNodes.size() >= solutionLimit) {
                    break;
                }
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
                                                                       """, result.getKey().size() != 0 ? "" : "nicht ",
                                                               strategy,
                                                               isStateSearch() ? "Zustandssuche" : "Wegsuche"));
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

    public static SearchArgs backupSearchArgs() {
        return new SearchArgs(currentSearchArgs);
    }

    public static void saveSearchArgs(SearchArgs searchArgs) {
        currentSearchArgs = searchArgs;
    }

    public static boolean isStateSearch() {
        return currentSearchArgs.stateSearch;
    }

    public static IAccessibilityChecker getAccessCheck() {
        return currentSearchArgs.accessCheck;
    }

    public static void setAccessCheck(IAccessibilityChecker accessibilityChecker) {
        currentSearchArgs.accessCheck = accessibilityChecker;
    }

    public static IGoalPredicate getGoalPred() {
        return currentSearchArgs.goalPred;
    }

    public static IHeuristicFunction getHeuristicFunc() {
        return currentSearchArgs.heuristicFunc;
    }

    public static ICallbackFunction[] getCallbackFuncs() {
        return currentSearchArgs.callbackFuncs;
    }

    public boolean isNoWaitAction() {
        return noWaitAction;
    }

    public void setNoWaitAction(boolean noWaitAction) {
        this.noWaitAction = noWaitAction;
    }
}
