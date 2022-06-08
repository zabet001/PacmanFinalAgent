package de.fh.stud.Agenten;

import de.fh.kiServer.agents.Agent;
import de.fh.kiServer.util.Vector2;
import de.fh.pacman.*;
import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanActionEffect;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.GameStateObserver;
import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Felddistanzen;
import de.fh.stud.Suchen.Sackgassen;
import de.fh.stud.Suchen.Suche;
import de.fh.stud.Suchen.Suchkomponenten.Knoten;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.Zugangsfilter;
import de.fh.stud.Suchen.Suchszenario;

import java.util.List;

public class MyAgent_P_Final extends PacmanAgent_2021 {

    private static final int RUN_AWAY_DISTANCE = 5;

    public MyAgent_P_Final(String name) {
        super(name);
    }

    public static void main(String[] args) {
        MyAgent_P_Final agent = new MyAgent_P_Final("MyFinalAgent");
        Agent.start(agent, "127.0.0.1", 5000);
    }

    /**
     @param percept - Aktuelle Wahrnehmung des Agenten, bspw. Position der Geister und Zustand aller Felder der Welt.
     @param actionEffect - Aktuelle Rückmeldung des Server auf die letzte übermittelte Aktion.
     */
    @Override
    public PacmanAction action(PacmanPercept percept, PacmanActionEffect actionEffect) {
        GameStateObserver.updateGameStateBeforeAction(percept, actionEffect);

        PacmanAction nextAction;
        Suche suche;
        Knoten loesungsKnoten = null;

        /*
        // Strategie 1: Suche nach bis zu N essbaren Dots
        // Warum auch immer ist damit die Winrate schlechter
        final int EATING_GOAL = 5;
        suche = new Suche(Suchszenario.eatUpToNDots(EATING_GOAL, GameStateObserver.remainingDots,
                                                    Zugangsfilter.AvoidMode.GHOSTS_THREATENS_FIELD));
        loesungsKnoten = suche.start(percept.getView(), percept.getPosX(), percept.getPosY(),
                                     Suche.SearchStrategy.A_STAR, false);
        */

        // Strategie 2: Suche nach essbaren Dots
        if (loesungsKnoten == null) {
            suche = new Suche.SucheBuilder(
                    Suchszenario.eatNearestDot(Zugangsfilter.AvoidMode.GHOSTS_THREATENS_FIELD)).createSuche();
            loesungsKnoten = suche.start(percept.getView(), percept.getPosX(), percept.getPosY(),
                                         Suche.SearchStrategy.A_STAR);
        }

        // Strategie 3: Sammle Powerpille ein, um potentielle Loesungen zu finden
        if (loesungsKnoten == null) {
            suche = new Suche.SucheBuilder(
                    Suchszenario.eatNearestPowerpill(Zugangsfilter.AvoidMode.GHOSTS_THREATENS_FIELD)).createSuche();
            loesungsKnoten = suche.start(percept.getView(), percept.getPosX(), percept.getPosY(),
                                         Suche.SearchStrategy.A_STAR);
        }

        // Strategie 4: Abwarten und beobachten, falls Geister weit genug weg
        if (loesungsKnoten == null) {
            // Strategie 5: Weglaufen, falls Geister nah dabei gefahrliche Felder meiden (warten evtl. moeglich)
            if (Felddistanzen.Geisterdistanz.minimumGhostDistance(percept.getPosX(), percept.getPosY(),
                                                                  percept.getGhostInfos()) <= RUN_AWAY_DISTANCE) {
                suche = new Suche.SucheBuilder(
                        Suchszenario.runAway(Zugangsfilter.AvoidMode.GHOSTS_THREATENS_FIELD, percept.getPosX(),
                                             percept.getPosY())).createSuche();
                loesungsKnoten = suche.start(percept.getView(), percept.getPosX(), percept.getPosY(),
                                             Suche.SearchStrategy.A_STAR);
            }
            else {
                // System.out.println("Keine Loesung gefunden: Vorerst WAIT");
            }

        }

        // Loesung gefunden: Actions bestimmen
        if (loesungsKnoten != null) {
            nextAction = loesungsKnoten
                    .identifyActionSequence()
                    .get(0);
        }
        // Wenn keine Loesung gefunden, vorerst abwarten
        else {
            nextAction = PacmanAction.WAIT;
        }

        if (nextAction == PacmanAction.WAIT && tooDangerousToWait(percept.getPosition(), percept.getGhostInfos())) {
            // System.err.println("WAIT bei Ghost neben Pacman bei " + percept.getPosition() + " ausgefuehrt!");
            // Falls abwarten zu gefaehrlich: Strategie 6: Weglaufen und Risiken eingehen (ohne zu warten)
//            System.err.println("-> WAIT bei toedlichem Ghost neben Pacman ausgefuehrt!!!!");
//            System.err.println("-- Gehe Risiko ein !!!!");
            // TODO: Gewichtung nach Geisttyp (eher zum Random, als zum Hunter gehen)
            suche = new Suche.SucheBuilder(
                    Suchszenario.runAway(Zugangsfilter.AvoidMode.GHOSTS_ON_FIELD, percept.getPosX(), percept.getPosY()))
                    .setWithWaitAction(false)
                    .createSuche();
            loesungsKnoten = suche.start(percept.getView(), percept.getPosX(), percept.getPosY(),
                                         Suche.SearchStrategy.A_STAR);
            if (loesungsKnoten != null) {
                nextAction = loesungsKnoten
                        .identifyActionSequence()
                        .get(0);
            }
            else {
//                System.err.println(".....Nowhere to run!");
            }
        }

        GameStateObserver.updateGameStateAfterAction(nextAction);
        return nextAction;
    }

    private static boolean tooDangerousToWait(Vector2 currentPos, List<GhostInfo> ghosts) {
        if (Sackgassen.deadEndDepth[currentPos.x][currentPos.y] > 0 &&
                Felddistanzen.Geisterdistanz.minimumGhostDistance(Sackgassen.deadEndEntry[currentPos.x][currentPos.y].x,
                                                                  Sackgassen.deadEndEntry[currentPos.x][currentPos.y].y,
                                                                  ghosts)
                        <= 1 + Sackgassen.deadEndDepth[currentPos.x][currentPos.y]) {
            return true;
        }
        for (GhostInfo ghost : ghosts) {
            if (ghost.getPillTimer() == 0 && MyUtil.isNeighbour(ghost.getPos(), currentPos)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onGameStart(PacmanStartInfo startInfo) {
        PacmanTileType[][] world = startInfo
                .getPercept()
                .getView();

        GameStateObserver.reset();
        GameStateObserver
                .getGameState()
                .setRemainingDots(MyUtil.countOccurrences(world, MyUtil::isDotType));

        Sackgassen.initDeadEndDepth(world);
        Felddistanzen.initDistances(world);

//        Sackgassen.printOneWayDepthMap(world);
//        Felddistanzen.printAllDistances(world);
    }

    @Override
    protected void onGameover(PacmanGameResult gameResult) {
    }

}
