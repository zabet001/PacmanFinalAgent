package de.fh.stud.Suchen.Suchkomponenten;

import de.fh.stud.Knoten;
import de.fh.stud.Suchen.Suche;
import de.fh.stud.interfaces.IHeuristicFunction;

import java.util.Comparator;
import java.util.PriorityQueue;

public class InformedOpenList extends OpenList {
    private final PriorityQueue<Knoten> openList;

    public InformedOpenList(Suche.SearchStrategy searchStrategy,IHeuristicFunction heuristicFunction) {
        super(searchStrategy);
        openList = new PriorityQueue<>(getInsertionCriteria(searchStrategy,heuristicFunction));
    }

    @Override
    public void add(Knoten child) {
        openList.add(child);
    }

    @Override
    public Knoten remove() {
        return openList.remove();
    }

    @Override
    public boolean isEmpty() {
        return openList.isEmpty();
    }

    @Override
    public int size() {
        return openList.size();
    }

    private static Comparator<Knoten> getInsertionCriteria(Suche.SearchStrategy strategy, IHeuristicFunction heuristicFunction) {
        return switch (strategy) {
            case GREEDY -> Comparator.comparingDouble(a->a.heuristicalValue(heuristicFunction));
            case UCS -> Comparator.comparingInt(Knoten::getCost);
            case A_STAR -> Comparator.comparingDouble(a -> a.getCost() + a.heuristicalValue(heuristicFunction));
            default -> null;
        };
    }
}
