package de.fh.stud.Agenten;

import de.fh.kiServer.agents.Agent;
import de.fh.pacman.*;
import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanActionEffect;
import de.fh.stud.GameStateObserver;
import de.fh.stud.Knoten;
import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Sackgassen;
import de.fh.stud.Suchen.Suche;
import de.fh.stud.Suchen.Suchfunktionen.Zugangsfilter;
import de.fh.stud.Suchen.Suchszenario;

import java.util.List;

public class MyAgent_PFinal extends PacmanAgent_2021 {

    private List<PacmanAction> actionSequence;
    private Knoten loesungsKnoten;
    private PacmanAction nextAction;

    public MyAgent_PFinal(String name) {
        super(name);
    }

    public static void main(String[] args) {
        MyAgent_PFinal agent = new MyAgent_PFinal("MyFinalAgent");
        Agent.start(agent, "127.0.0.1", 5000);
    }

    /**
     @param percept - Aktuelle Wahrnehmung des Agenten, bspw. Position der Geister und Zustand aller Felder der Welt.
     @param actionEffect - Aktuelle Rückmeldung des Server auf die letzte übermittelte Aktion.
     */
    @Override
    public PacmanAction action(PacmanPercept percept, PacmanActionEffect actionEffect) {
        GameStateObserver.updateGameStateBeforeAction(percept, actionEffect);

        //Wenn noch keine Lösung gefunden wurde, dann starte die Suche

        int goalx = 1;
        int goaly = 1;

        Suche suche = new Suche(Suchszenario.eatNearestDot(Zugangsfilter.AvoidMode.GHOSTS_THREATENS_FIELD));
        loesungsKnoten = suche.start(percept.getView(), percept.getPosX(), percept.getPosY(),
                Suche.SearchStrategy.A_STAR);
        if (loesungsKnoten == null) {
            suche = new Suche(Suchszenario.runAway(percept.getGhostInfos(), percept.getPosX(), percept.getPosY()));
            loesungsKnoten = suche.start(percept.getView(), percept.getPosX(), percept.getPosY(),
                    Suche.SearchStrategy.A_STAR, false);
        }

        if (loesungsKnoten != null)
            nextAction = loesungsKnoten.identifyActionSequence().get(0);

        //Wenn die Suche eine Lösung gefunden hat, dann ermittle die als nächstes auszuführende Aktion
        if (nextAction != null) {

            // TODO: PacmanAction.WAIT ist toedlich, wenn neben Pacman ein Geist mit powerpillTimer = 0 ist ->
            //  handlen (z.B. dann noWait() fuer diese Suche!
            if (nextAction == PacmanAction.WAIT && MyUtil.ghostNextToPos(GameStateObserver.currentWorld,
                    percept.getPosX(), percept.getPosY())) {
                System.out.println("WAIT bei Ghost neben Pacman bei " + percept.getPosition() + " ausgefuehrt!");
                for (GhostInfo ghosts : percept.getGhostInfos()) {
                    if (MyUtil.isNeighbour(ghosts.getPos(), percept.getPosition()) && ghosts.getPillTimer()==0)
                        System.out.println("-> WAIT bei toedlichem Ghost neben Pacman ausgefuehrt!!!!");
                }
            }
            GameStateObserver.updateGameStateAfterAction(nextAction);
            return nextAction;
        }
        //Ansonsten wurde keine Lösung gefunden und der Pacman kann das Spiel aufgeben
        System.out.println("Keine Loesungen gefunden");
        return PacmanAction.WAIT;
    }

    @Override
    protected void onGameStart(PacmanStartInfo startInfo) {
        Sackgassen.initDeadEndDepth(startInfo.getPercept().getView());
    }

    @Override
    protected void onGameover(PacmanGameResult gameResult) {

    }

}
