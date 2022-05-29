package de.fh.stud.Suchen.Suchkomponenten;

public class PathClosedList extends ClosedList {
    int size;
    boolean[][] closedList;

    public PathClosedList(byte[][] world) {
        closedList = new boolean[world.length][world[0].length];
    }

    @Override
    public boolean contains(Knoten node) {
        // Ueberlegung: Das gehoert vllt. nicht hierhin, da es damit abhaengig vom zustand ist
        // aber damit gelingt es z.B. Wege zu finden, die erst durch fressen der Pille geoeffnet werden
        if (node.getPowerpillTimer() > 0) {
            return false;
        }
        return closedList[node.getPosX()][node.getPosY()];
    }

    @Override
    public void add(Knoten expCand) {
        closedList[expCand.getPosX()][expCand.getPosY()] = true;
        size++;
    }

    @Override
    public int size() {
        return size;
    }
}
