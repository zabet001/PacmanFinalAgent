package de.fh.stud.Suchen.Suchkomponenten;

public abstract class ClosedList {
	public static ClosedList buildClosedList(boolean isStateSearch, byte[][] world) {
		return isStateSearch ? new StateClosedList() : new PathClosedList(world);
	}

	public abstract boolean contains(Knoten node);

	public abstract void add(Knoten child);

	public abstract int size();
}
