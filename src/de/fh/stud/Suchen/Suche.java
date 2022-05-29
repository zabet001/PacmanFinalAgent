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
    public static boolean searchExisting = false;
    public static List<Double> runTimes = new LinkedList<>();

    public enum SearchStrategy {
        DEPTH_FIRST, BREADTH_FIRST, GREEDY, UCS, A_STAR
    }

    private final boolean displayResults;
    private final boolean printResults;

    private final boolean stateSearch;
    private final boolean withWaitAction;
    private final IAccessibilityChecker accessCheck;
    private final IGoalPredicate goalPred;
    private final IHeuristicFunction heuristicFunc;
    private final ICallbackFunction[] callbackFuncs;

    public static final class SucheBuilder {
        private boolean displayResults = false;
        private boolean printResults = false;
        private boolean stateSearch = true;
        private boolean withWaitAction = true;
        private IAccessibilityChecker accessCheck;
        private IGoalPredicate goalPred;
        private IHeuristicFunction heuristicFunc;
        private ICallbackFunction[] callbackFuncs;

        public SucheBuilder() {}

        public SucheBuilder(Suchszenario scenario) {
            this.stateSearch = scenario.isStateProblem();
            this.accessCheck = scenario.getAccessCheck();
            this.goalPred = scenario.getGoalPred();
            this.heuristicFunc = scenario.getHeuristicFunc();
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

        public SucheBuilder setWithWaitAction(boolean withWaitAction) {
            this.withWaitAction = withWaitAction;
            return this;

        }

        public SucheBuilder setAccessCheck(IAccessibilityChecker accessCheck) {
            this.accessCheck = accessCheck;
            return this;

        }

        public SucheBuilder setGoalPred(IGoalPredicate goalPred) {
            this.goalPred = goalPred;
            return this;

        }

        public SucheBuilder setHeuristicFunc(IHeuristicFunction heuristicFunc) {
            this.heuristicFunc = heuristicFunc;
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

        public Suche createSuche() {
            if (accessCheck == null) {
                throw new IllegalArgumentException("Missing " + IAccessibilityChecker.class.getSimpleName());
            }
            if (goalPred == null) {
                goalPred = node -> false;
            }
            if (heuristicFunc == null) {
                heuristicFunc = node -> 0;
            }
            if (callbackFuncs == null) {
                callbackFuncs = new ICallbackFunction[]{expCand -> {}};
            }

            return new Suche(this);
        }
    }

    public Suche(SucheBuilder b) {
        if (Suche.searchExisting) {
            System.err.println("WARNUNG: Eine Suche laeuft bereits!");
        }
        else {
            Suche.searchExisting = true;
        }
        this.displayResults = b.displayResults;
        this.printResults = b.printResults;

        this.stateSearch = b.stateSearch;
        this.withWaitAction = b.withWaitAction;

        this.accessCheck = b.accessCheck;
        this.goalPred = b.goalPred;
        this.heuristicFunc = b.heuristicFunc;
        this.callbackFuncs = b.callbackFuncs;
    }


    public Knoten start(PacmanTileType[][] world, int posX, int posY, SearchStrategy strategy) {
        List<Knoten> ret = start(world, posX, posY, strategy, 1);
        return ret.size() > 0 ? ret.get(0) : null;
    }

    public List<Knoten> start(PacmanTileType[][] world, int posX, int posY, SearchStrategy strategy, int solutionLimit) {
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

        if (printResults) {
            System.out.println(debugInfos(strategy, searchResult));
        }
        if (displayResults) {
            JFrame jf = new JFrame();
            jf.setAlwaysOnTop(true);
            JOptionPane.showMessageDialog(jf, debugInfos(strategy, searchResult));

        }

        Suche.searchExisting = false;
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
                        .expand(stateSearch, withWaitAction, accessCheck)
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

    private String debugInfos(SearchStrategy strategy,
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
        return report.toString();
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

    public boolean isWithWaitAction() {
        return withWaitAction;
    }

}
