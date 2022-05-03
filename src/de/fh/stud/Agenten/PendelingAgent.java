package de.fh.stud.Agenten;

import de.fh.kiServer.agents.Agent;
import de.fh.pacman.*;
import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanActionEffect;
import de.fh.pacman.enums.PacmanTileType;

public class PendelingAgent extends PacmanAgent_2021 {

    /*
     * TODO Praktikum 1: Fügt gemäß der Aufgabenstellung neue Attribute hinzu, falls notwendig.
     */

    /**
     Die als nächstes auszuführende Aktion
     */
    private PacmanAction nextAction;

    public PendelingAgent(String name) {
        super(name);
    }

    public static void main(String[] args) {
        PendelingAgent agent = new PendelingAgent("MyAgent");
        Agent.start(agent, "127.0.0.1", 5000);
    }


    boolean goingLeft;

    /**
     @param percept - Aktuelle Wahrnehmung des Agenten, bspw. Position der Geister und Zustand aller Felder der Welt.
     @param actionEffect - Aktuelle Rückmeldung des Server auf die letzte übermittelte Aktion.
     */
    @Override
    public PacmanAction action(PacmanPercept percept, PacmanActionEffect actionEffect) {


        if (percept.getView()[percept.getPosX() + (goingLeft ? -1 : 1)][percept.getPosY()] == PacmanTileType.WALL)
            goingLeft = !goingLeft;

        return PacmanAction.values()[1 + (goingLeft ? 0 : 1)];
    }

    @Override
    protected void onGameStart(PacmanStartInfo startInfo) {

    }

    @Override
    protected void onGameover(PacmanGameResult gameResult) {

    }
}
