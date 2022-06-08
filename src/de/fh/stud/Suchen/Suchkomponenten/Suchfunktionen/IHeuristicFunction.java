package de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen;

import de.fh.stud.Suchen.Suchkomponenten.Knoten;

public interface IHeuristicFunction {
    float calcHeuristic(Knoten node);
}
