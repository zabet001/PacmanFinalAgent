package de.fh.stud;

import de.fh.kiServer.agents.Agent;
import de.fh.pacman.PacmanAgent_2021;
import de.fh.pacman.PacmanGameResult;
import de.fh.pacman.PacmanPercept;
import de.fh.pacman.PacmanStartInfo;
import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanActionEffect;
import de.fh.stud.Suchen.Sackgassen;
import de.fh.stud.Suchen.Suche;
import de.fh.stud.Suchen.Suchszenario;

import java.util.List;

public class MyAgent_P3 extends PacmanAgent_2021 {

    private List<PacmanAction> actionSequence;
    private Knoten loesungsKnoten;

    public MyAgent_P3(String name) {
        super(name);
    }

    public static void main(String[] args) {
        MyAgent_P3 agent = new MyAgent_P3("MyAgent");
        Agent.start(agent, "127.0.0.1", 5000);
    }

    /**
     @param percept - Aktuelle Wahrnehmung des Agenten, bspw. Position der Geister und Zustand aller Felder der Welt.
     @param actionEffect - Aktuelle Rückmeldung des Server auf die letzte übermittelte Aktion.
     */
    @Override
    public PacmanAction action(PacmanPercept percept, PacmanActionEffect actionEffect) {
        //Wenn noch keine Lösung gefunden wurde, dann starte die Suche
        if (loesungsKnoten == null) {
            int goalx = 1;
            int goaly = 1;

            Suche suche = new Suche(Suchszenario.eatAllDots());
            loesungsKnoten = suche.start(percept.getView(), percept.getPosX(), percept.getPosY(),
                    Suche.SearchStrategy.A_STAR);
            if (loesungsKnoten != null)
                actionSequence = loesungsKnoten.identifyActionSequence();
        }

        //Wenn die Suche eine Lösung gefunden hat, dann ermittle die als nächstes auszuführende Aktion
        if (actionSequence != null && actionSequence.size() != 0) {
            return actionSequence.remove(0);
        } else {
            //Ansonsten wurde keine Lösung gefunden und der Pacman kann das Spiel aufgeben
            return PacmanAction.QUIT_GAME;
        }

    }

    @Override
    protected void onGameStart(PacmanStartInfo startInfo) {
        Sackgassen.initDeadEndDepth(startInfo.getPercept().getView());
    }

    @Override
    protected void onGameover(PacmanGameResult gameResult) {

    }

}
