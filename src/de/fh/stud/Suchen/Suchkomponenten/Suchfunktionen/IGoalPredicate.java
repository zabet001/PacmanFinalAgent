package de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen;

import de.fh.stud.Suchen.Suchkomponenten.Knoten;

public interface IGoalPredicate {
    // TODO ? Klasse statt interface: Zur Klasse ein flag IS_STATE_GOAL einfuegen
    boolean isGoalNode(Knoten node);
}
