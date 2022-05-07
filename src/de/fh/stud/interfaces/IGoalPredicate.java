package de.fh.stud.interfaces;

import de.fh.stud.Knoten;

public interface IGoalPredicate {
    // TODO ? Klasse statt interface: Zur Klasse ein flag IS_STATE_GOAL einfuegen
    boolean isGoalNode(Knoten node);
}
