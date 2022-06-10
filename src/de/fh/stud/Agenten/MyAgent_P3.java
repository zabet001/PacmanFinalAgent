package de.fh.stud.Agenten;

import de.fh.kiServer.agents.Agent;
import de.fh.pacman.PacmanAgent_2021;
import de.fh.pacman.PacmanGameResult;
import de.fh.pacman.PacmanPercept;
import de.fh.pacman.PacmanStartInfo;
import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanActionEffect;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.GameStateObserver;
import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Suche;
import de.fh.stud.Suchen.Suchkomponenten.Knoten;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.IAccessibilityChecker;
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
		GameStateObserver.updateGameStateBeforeAction(percept, actionEffect);
		//Wenn noch keine Lösung gefunden wurde, dann starte die Suche
		if (loesungsKnoten == null) {
			int goalx = 13;
			int goaly = 1;

			Suche suche = new Suche.SucheBuilder(Suchszenario.eatAllDots(IAccessibilityChecker.AvoidMode.ONLY_WALLS))
					.setWithWaitAction(false)
					.setPrintResults(true)
					.setDisplayResults(true)
					.createSuche();
			loesungsKnoten = suche.start(percept.getView(), percept.getPosX(), percept.getPosY(),
										 Suche.SearchStrategy.A_STAR);
			if (loesungsKnoten != null) {
				actionSequence = loesungsKnoten.identifyActionSequence();
			}
		}

		//Wenn die Suche eine Lösung gefunden hat, dann ermittle die als nächstes auszuführende Aktion
		if (actionSequence != null && actionSequence.size() != 0) {
			GameStateObserver.updateGameStateAfterAction(actionSequence.get(0));
			return actionSequence.remove(0);
		}
		else {
			//Ansonsten wurde keine Lösung gefunden und der Pacman kann das Spiel aufgeben
			return PacmanAction.QUIT_GAME;
		}

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

//        Sackgassen.initDeadEndDepth(world);
//        Felddistanzen.initDistances(world);
//
//        Sackgassen.printOneWayDepthMap(world);
//        Felddistanzen.printAllDistances(world);
	}

	@Override
	protected void onGameover(PacmanGameResult gameResult) {

	}
}
