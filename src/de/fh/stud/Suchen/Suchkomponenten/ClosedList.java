package de.fh.stud.Suchen.Suchkomponenten;

import de.fh.pacman.enums.PacmanTileType;

public abstract class ClosedList {
    public static ClosedList buildClosedList(boolean isStateSearch, PacmanTileType[][] world) {
        return isStateSearch ? new StateClosedList() : new PathClosedList(world);
    }

    public abstract boolean contains(Knoten node);

    public abstract void add(Knoten child);

    public abstract int size();
}
