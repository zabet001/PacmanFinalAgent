package de.fh.stud.Suchen.Suchkomponenten;

import de.fh.stud.Knoten;

import java.util.HashSet;

public class StateClosedList extends ClosedList {
    private HashSet<Knoten> closedList;

    public StateClosedList() {
        closedList = new HashSet<>();
    }

    @Override
    public boolean contains(Knoten node) {
        return closedList.contains(node);
    }

    @Override
    public void add(Knoten child) {
        closedList.add(child);
    }

    @Override
    public int size() {
        return closedList.size();
    }
}
