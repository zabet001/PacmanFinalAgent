package de.fh.stud.Suchen.Suchkomponenten;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.GameStateObserver;
import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.IAccessibilityChecker;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.ICallbackFunction;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.IGoalPredicate;
import de.fh.stud.Suchen.Suchkomponenten.Suchfunktionen.IHeuristicFunction;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Knoten {

	private static final short COST_LIMIT = 1000;
	private final Knoten pred;
	private byte[][] view;
	private final short cost;
	private short remainingDots;
	private final byte posX, posY;
	private final byte powerpillTimer; // != 0: unverwundbar

	public static Knoten generateRoot(boolean isStateSearch, byte[][] world, int posX, int posY) {
		// Das Spawnfeld fuer die Suche ignorieren
		byte zw = world[posX][posY];
		world[posX][posY] = MyUtil.tileToByte(PacmanTileType.EMPTY);

		Knoten ret = new Knoten(isStateSearch, (byte) posX, (byte) posY);
		ret.view = MyUtil.copyView(world);
		ret.remainingDots = MyUtil.countOccurrences(world, field -> MyUtil.isDotType(MyUtil.byteToTile(field)));

		// Spawnfeld wieder zuruecksetzen
		world[posX][posY] = zw;

		return ret;
	}

	public static Knoten generateRoot(boolean isStateSearch, PacmanTileType[][] world, int posX, int posY) {
		return generateRoot(isStateSearch, MyUtil.createByteView(world), posX, posY);
	}

	private Knoten(boolean isStateSearch, byte posX, byte posY) {
		this(isStateSearch, null, posX, posY);
	}

	private Knoten(boolean isStateSearch, Knoten pred, byte posX, byte posY) {
		this.pred = pred;
		this.posX = posX;
		this.posY = posY;

		if (pred == null) {
			// Wurzelknoten
			this.view = null;
			this.remainingDots = 0;
			this.cost = 0;
			this.powerpillTimer = isStateSearch ? GameStateObserver
					.getGameState()
					.getPowerpillTimer() : 0;
		}
		else {
			// Kindknoten
			this.cost = (short) (pred.cost + 1);
			this.view = manageNewView(isStateSearch, pred, posX, posY);
			this.remainingDots = calcRemainingDots(isStateSearch, pred, posX, posY);
			this.powerpillTimer = calcPowerpillTimer(isStateSearch, pred, posX, posY);
		}

	}
	// TODO Idee: Zusatzinformationen fuer Knoten (dotsEaten, powerPillTimer etc.) in Extra-Objekt speichernrn?
	//  (vllt. z.B. Objekt Zusatzinformationen -> Knoten (unidirektionale Assoziation)

	private short calcRemainingDots(boolean isStateSearch, Knoten pred, byte posX, byte posY) {
		if (isStateSearch && MyUtil.isDotType(MyUtil.byteToTile(pred.view[posX][posY]))) {
			return (short) (pred.remainingDots - 1);
		}
		else {
			return pred.remainingDots;
		}
	}

	private byte calcPowerpillTimer(boolean isStateSearch, Knoten pred, byte posX, byte posY) {

		if (isStateSearch) {
			if (MyUtil.isPowerpillType(MyUtil.byteToTile(pred.view[posX][posY]))) {
				return GameStateObserver.getPowerpillDuration();
			}
			// IDEE: Wenn ein Geist gefressen wurde, ist dieser beim Respawnen gefaehrlich -> Nur noch 1 Schritt lang
			// die Powerpille nutzen
			else if (pred.powerpillTimer > 1 && MyUtil.isGhostType(MyUtil.byteToTile(pred.view[posX][posY]))) {
				return 1;
			}
		}
		return (byte) (pred.powerpillTimer - (pred.powerpillTimer > 0 ? 1 : 0));

	}

	private byte[][] manageNewView(boolean isStateSearch, Knoten pred, byte posX, byte posY) {
		if (!isStateSearch || pred.view[posX][posY] == MyUtil.tileToByte(PacmanTileType.EMPTY)) {
			return pred.view;
		}
		byte[][] newView = MyUtil.copyView(pred.view);
		if (MyUtil.isGhostType(MyUtil.byteToTile(newView[posX][posY]))) {
			newView[posX][posY] = MyUtil.tileToByte(PacmanTileType.GHOST);
		}
		else {
			newView[posX][posY] = MyUtil.tileToByte(PacmanTileType.EMPTY);
		}

		return newView;
	}

	public int nodeNeighbourCnt(IAccessibilityChecker[] accessibilityCheckers) {
		int neighbourCnt = 0;
		for (byte[] neighbour : MyUtil.NEIGHBOUR_POS) {
			// if (view[posX + neighbour[0]][posY + neighbour[1]] != PacmanTileType.WALL) {
			if (isPassable((byte) (posX + neighbour[0]), (byte) (posY + neighbour[1]), accessibilityCheckers)) {
				neighbourCnt++;
			}
		}
		return neighbourCnt;
	}

	public boolean isPassable(byte newPosX, byte newPosY, IAccessibilityChecker[] accessibilityCheckers) {
		for (IAccessibilityChecker accessibilityChecker : accessibilityCheckers) {
			if (!accessibilityChecker.isAccessible(this, newPosX, newPosY)) {
				return false;
			}
		}
		return true;
	}

	public List<Knoten> expand(boolean isStateSearch, boolean withWait,
							   IAccessibilityChecker[] accessibilityCheckers) {
		// Macht es einen Unterschied, wenn NEIGHBOUR_POS pro expand aufruf neu erzeugt wird? Ja
		List<Knoten> children = new LinkedList<>();

		for (byte[] neighbour : MyUtil.NEIGHBOUR_POS) {
			if (cost < COST_LIMIT && isPassable((byte) (posX + neighbour[0]), (byte) (posY + neighbour[1]),
												accessibilityCheckers)) {
				children.add(
						new Knoten(isStateSearch, this, (byte) (posX + neighbour[0]), (byte) (posY + neighbour[1])));
			}
		}
		if (withWait && cost < COST_LIMIT && isPassable(posX, posY, accessibilityCheckers)) {
			children.add(new Knoten(isStateSearch, this, posX, posY));
		}

		return children;
	}

	public boolean isGoalNode(IGoalPredicate goalPredicate) {
		return goalPredicate.isGoalNode(this);
	}

	public void executeCallbacks(ICallbackFunction[] callbackFunctions) {
		if (callbackFunctions != null) {
			for (ICallbackFunction callbacks : callbackFunctions) {
				callbacks.callback(this);
			}
		}
	}

	public PacmanAction previousAction() {
		if (pred == null) {
			return PacmanAction.WAIT;
		}
		else {
			if (posX > pred.posX) {
				return PacmanAction.GO_EAST;
			}
			else if (posX < pred.posX) {
				return PacmanAction.GO_WEST;
			}
			else if (posY > pred.posY) {
				return PacmanAction.GO_SOUTH;
			}
			else if (posY < pred.posY) {
				return PacmanAction.GO_NORTH;
			}
			else {
				return PacmanAction.WAIT;
			}
		}
	}

	public List<PacmanAction> identifyActionSequence() {
		List<PacmanAction> ret = new LinkedList<>();
		Knoten it = this;
		while (it.pred != null) {
			ret.add(0, it.previousAction());
			it = it.pred;
		}
		if (ret.size() == 0) {
			ret.add(PacmanAction.WAIT);
		}
		return ret;
	}

	public float heuristicalValue(IHeuristicFunction heuristicFunction) {
		return heuristicFunction.calcHeuristic(this);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(remainingDots, posX, posY);
		result = 31 * result + Arrays.deepHashCode(view);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Knoten knoten = (Knoten) o;
		return remainingDots == knoten.remainingDots && posX == knoten.posX && posY == knoten.posY
				&& /*heuristicalValue() == ((Knoten) o).heuristicalValue() &&*/ Arrays.deepEquals(view, knoten.view);
	}

	// region Getter und Setter
	public byte[][] getView() {
		return view;
	}

	public Knoten getPred() {
		return pred;
	}

	public Vector2 getPosition() {
		return new Vector2(posX, posY);
	}

	public byte getPosX() {
		return posX;
	}

	public byte getPosY() {
		return posY;
	}

	public byte getPowerpillTimer() {
		return powerpillTimer;
	}

	public int getRemainingDots() {
		return remainingDots;
	}

	public short getCost() {
		return cost;
	}

	// endregion
}
