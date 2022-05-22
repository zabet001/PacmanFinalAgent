package de.fh.stud.Suchen.Suchkomponenten;

import de.fh.stud.Knoten;
import de.fh.stud.Suchen.Suche;
import de.fh.stud.interfaces.IHeuristicFunction;

public abstract class OpenList {
    protected Suche.SearchStrategy searchStrategy;


    public static OpenList buildOpenList(Suche.SearchStrategy strategy, IHeuristicFunction heuristicFunction) {
        return switch (strategy) {
            case DEPTH_FIRST, BREADTH_FIRST -> new UninformedOpenList(strategy);
            default -> new InformedOpenList(strategy, heuristicFunction);
        };
    }

    public OpenList(Suche.SearchStrategy searchStrategy) {
        this.searchStrategy = searchStrategy;
    }

    public abstract void add(Knoten child);

    public abstract Knoten remove();

    public abstract boolean isEmpty();

    public abstract int size();
}
