package de.fh.stud.Agenten;

import de.fh.kiServer.agents.Agent;
import de.fh.pacman.*;
import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanActionEffect;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.GameStateObserver;
import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Felddistanzen;
import de.fh.stud.Suchen.Sackgassen;
import de.fh.stud.Suchen.Suche;
import de.fh.stud.Suchen.Suchfunktionen.Zugangsfilter;
import de.fh.stud.Suchen.Suchkomponenten.Knoten;
import de.fh.stud.Suchen.Suchszenario;

public class MyAgent_P_Final extends PacmanAgent_2021 {

    public MyAgent_P_Final(String name) {
        super(name);
    }

    public static void main(String[] args) {
        MyAgent_P_Final agent = new MyAgent_P_Final("MyFinalAgent");
        Agent.start(agent, "127.0.0.1", 5000);

    }

    @Override
    protected void onGameStart(PacmanStartInfo startInfo) {
        PacmanTileType[][] world = startInfo
                .getPercept()
                .getView();

        GameStateObserver.reset();
        GameStateObserver
                .getGameState()
                .setRemainingDots(MyUtil.countDots(world));

        Sackgassen.initDeadEndDepth(world);
        Felddistanzen.initDistances(world);

        Sackgassen.printOneWayDepthMap(world);
//        Felddistanzen.printAllDistances(world);
    }

    @Override
    protected void onGameover(PacmanGameResult gameResult) {

    }

    /**
     @param percept - Aktuelle Wahrnehmung des Agenten, bspw. Position der Geister und Zustand aller Felder der Welt.
     @param actionEffect - Aktuelle Rückmeldung des Server auf die letzte übermittelte Aktion.
     */

    @Override
    public PacmanAction action(PacmanPercept percept, PacmanActionEffect actionEffect) {
        GameStateObserver.updateGameStateBeforeAction(percept, actionEffect);

        PacmanAction nextAction = null;
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
            suche = new Suche(Suchszenario.eatNearestDot(Zugangsfilter.AvoidMode.GHOSTS_THREATENS_FIELD));
            loesungsKnoten = suche.start(percept.getView(), percept.getPosX(), percept.getPosY(),
                                         Suche.SearchStrategy.A_STAR, false);
        }

        // Strategie 3: Sammle Powerpille ein, um potentielle Loesungen zu finden
        if (loesungsKnoten == null) {
            suche = new Suche(Suchszenario.eatNearestPowerpill(Zugangsfilter.AvoidMode.GHOSTS_THREATENS_FIELD));
            loesungsKnoten = suche.start(percept.getView(), percept.getPosX(), percept.getPosY(),
                                         Suche.SearchStrategy.A_STAR, false);
        }

        // Strategie 4: Weglaufen, dabei gefahrliche Felder meiden (notfalls warten)
        if (loesungsKnoten == null) {
            suche = new Suche(
                    Suchszenario.runAway(Zugangsfilter.AvoidMode.GHOSTS_THREATENS_FIELD, false, percept.getPosX(),
                                         percept.getPosY()));
            loesungsKnoten = suche.start(percept.getView(), percept.getPosX(), percept.getPosY(),
                                         Suche.SearchStrategy.A_STAR, false);

        }

        if (loesungsKnoten != null) {
            nextAction = loesungsKnoten
                    .identifyActionSequence()
                    .get(0);
        }

        // Wenn keine Loesung gefunden, vorerst abwarten
        if (nextAction == null) {
            System.out.println("Keine Loesung gefunden: WAIT");
            nextAction = PacmanAction.WAIT;

        }

        // Falls abwarten zu gefaehrlich: Strategie 5: Weglaufen und Risiken eingehen (ohne zu warten)
        if (nextAction == PacmanAction.WAIT && MyUtil.ghostNextToPos(percept.getPosX(), percept.getPosY(),
                                                                     GameStateObserver
                                                                             .getGameState()
                                                                             .getNewPercept()
                                                                             .getGhostInfos())) {
            System.err.println("WAIT bei Ghost neben Pacman bei " + percept.getPosition() + " ausgefuehrt!");
            for (GhostInfo ghosts : percept.getGhostInfos()) {
                if (MyUtil.isNeighbour(ghosts.getPos(), percept.getPosition()) && ghosts.getPillTimer() == 0) {
                    System.err.println("-> WAIT bei toedlichem Ghost neben Pacman ausgefuehrt!!!!");
                    System.err.println("-- Gehe Risiko ein !!!!");
                    // TODO: Gewichtung nach Geisttyp (eher zum Random, als zum Hunter gehen)
                    suche = new Suche(
                            Suchszenario.runAway(Zugangsfilter.AvoidMode.GHOSTS_ON_FIELD, true, percept.getPosX(),
                                                 percept.getPosY()));
                    loesungsKnoten = suche.start(percept.getView(), percept.getPosX(), percept.getPosY(),
                                                 Suche.SearchStrategy.A_STAR, false);
                    if (loesungsKnoten != null) {
                        nextAction = loesungsKnoten
                                .identifyActionSequence()
                                .get(0);
                    }
                    else {
                        System.err.println(".....Nowhere to run!");
                    }
                    break;
                }
            }

        }
        GameStateObserver.updateGameStateAfterAction(nextAction);
        return nextAction;
    }

}
