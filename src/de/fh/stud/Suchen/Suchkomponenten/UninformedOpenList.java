package de.fh.stud.Suchen.Suchkomponenten;

import de.fh.stud.Suchen.Suche;

import java.util.LinkedList;
import java.util.List;

public class UninformedOpenList extends OpenList {
	private final List<Knoten> openList;

	public UninformedOpenList(Suche.SearchStrategy searchStrategy) {
		super(searchStrategy);
		openList = new LinkedList<>();
	}

	@Override
	public void add(Knoten child) {
		switch (this.searchStrategy) {
			case DEPTH_FIRST -> openList.add(0, child);
			case BREADTH_FIRST -> openList.add(child);
		}
	}

	@Override
	public Knoten remove() {
		return openList.remove(0);
	}

	@Override
	public boolean isEmpty() {
		return openList.isEmpty();
	}

	@Override
	public int size() {
		return openList.size();
	}
}
