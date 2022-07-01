package de.fh.stud.Suchen.Suchkomponenten;

import de.fh.stud.Suchen.Suche;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.IHeuristicFunction;

import java.util.Comparator;
import java.util.PriorityQueue;

public class InformedOpenList extends OpenList {
	private final PriorityQueue<Knoten> openList;

	public InformedOpenList(Suche.SearchStrategy searchStrategy, IHeuristicFunction[] heuristicFunction) {
		super(searchStrategy);
		openList = new PriorityQueue<>(getInsertionCriteria(searchStrategy, heuristicFunction));
	}

	private static Comparator<Knoten> getInsertionCriteria(Suche.SearchStrategy strategy,
														   IHeuristicFunction[] heuristicFuncs) {
		return switch (strategy) {
			case GREEDY -> (o1, o2) -> {
				int ret;
				for (int i = 0; i < heuristicFuncs.length; i++) {
					if ((ret = Double.compare(o1.heuristicalValue(heuristicFuncs, i),
											  o2.heuristicalValue(heuristicFuncs, i))) != 0) {
						return ret;
					}
				}
				return 0;
			};
			case UCS -> Comparator
					.comparingInt(Knoten::getCost)
					.thenComparingInt(Knoten::getCost);
			case A_STAR -> (o1, o2) -> {
				int ret;
				int i;
				for (i = 0; i < heuristicFuncs.length; i++) {
					ret = Double.compare(o1.getCost() + o1.heuristicalValue(heuristicFuncs, i),
										 o2.getCost() + o2.heuristicalValue(heuristicFuncs, i));
					if (ret != 0) {
						return ret;
					}
				}
				return 0;
			};
			default -> null;
		};
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
}
