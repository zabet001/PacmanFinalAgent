package de.fh.stud.Suchen.Suchkomponenten;

import de.fh.stud.Knoten;
import de.fh.stud.Suchen.Suche;

import java.util.Comparator;
import java.util.PriorityQueue;

public class InformedOpenList extends OpenList {
    private final PriorityQueue<Knoten> openList;

    public InformedOpenList(Suche.SearchStrategy searchStrategy) {
        super(searchStrategy);
        openList = new PriorityQueue<>(getInsertionCriteria(searchStrategy));
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

    private static Comparator<Knoten> getInsertionCriteria(Suche.SearchStrategy strategy) {
        return switch (strategy) {
            case GREEDY -> Comparator.comparingDouble(Knoten::heuristicalValue);
            case UCS -> Comparator.comparingInt(Knoten::getCost);
            case A_STAR -> Comparator.comparingDouble(a -> a.getCost() + a.heuristicalValue());
            default -> null;
        };
    }
}
