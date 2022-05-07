package de.fh.stud;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.Suchen.Suche;
import de.fh.stud.interfaces.ICallbackFunction;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Knoten {

    private static PacmanTileType[][] STATIC_WORLD;
    private static final short COST_LIMIT = 1000;

    private final Knoten pred;
    private final byte[][] view;

    private byte powerpillTimer; // != 0: unverwundbar
    private final short cost;

    private final short remainingDots;

    private final byte posX, posY;

    // TODO Idee: Zusatzinformationen fuer Knoten (dotsEaten, powerPillTimer etc.) in Extra-Objekt speichernrn?
    //  (vllt. z.B. Objekt Zusatzinformationen -> Knoten (unidirektionale Assoziation)
    public static Knoten generateRoot(PacmanTileType[][] world, int posX, int posY) {
        Knoten.STATIC_WORLD = world;
        // Das Spawnfeld fuer die Suche ignorieren
        PacmanTileType zw = world[posX][posY];
        world[posX][posY] = PacmanTileType.EMPTY;
        Knoten ret = new Knoten((byte) posX, (byte) posY);
        // Spawnfeld wieder zuruecksetzen
        world[posX][posY] = zw;
        return ret;
    }

    private Knoten(byte posX, byte posY) {
        this(null, posX, posY);
    }

    private Knoten(Knoten pred, byte posX, byte posY) {
        this.pred = pred;
        this.posX = posX;
        this.posY = posY;

        if (pred == null) {
            // Wurzelknoten
            this.view = MyUtil.createByteView(STATIC_WORLD);

            this.cost = 0;
            this.remainingDots = MyUtil.countDots(STATIC_WORLD);
            this.powerpillTimer = Suche.isStateSearch() ? GameStateObserver.getGameState().getPowerpillTimer() : 0;
        }
        else {
            // Kindknoten
            if (Suche.isStateSearch() && MyUtil.isPowerpillType(MyUtil.byteToTile(pred.view[posX][posY]))) {
                powerpillTimer = GameStateObserver.getPowerpillDuration();
            }
            else {
                powerpillTimer = (byte) (pred.powerpillTimer - (pred.powerpillTimer > 0 ? 1 : 0));
            }
            if (Suche.isStateSearch() && MyUtil.isDotType(MyUtil.byteToTile(pred.view[posX][posY]))) {
                this.remainingDots = (short) (pred.remainingDots - 1);
            }
            else {
                this.remainingDots = pred.remainingDots;
            }

            if (!Suche.isStateSearch() || pred.view[posX][posY] == MyUtil.tileToByte(PacmanTileType.EMPTY)) {
                this.view = pred.view;
            }
            else {
                this.view = MyUtil.copyView(pred.view);

                if (MyUtil.isGhostType(MyUtil.byteToTile(this.view[posX][posY]))) {
                    this.view[posX][posY] = MyUtil.tileToByte(PacmanTileType.GHOST);
                    // IDEE: Wenn Geist gefressen: powerpillTimer auf 1 setzen, da Gefahr ab naechsten Respawn
                    this.powerpillTimer = 1; // Wenn Gefahr durch respawnten Geist vermeiden
                }
                else {
                    this.view[posX][posY] = MyUtil.tileToByte(PacmanTileType.EMPTY);
                }
            }

            this.cost = (short) (pred.cost + 1);
        }

    }

    public int nodeNeighbourCnt() {
        return MyUtil.adjacentFreeFieldsCnt(this, posX, posY);
    }

    public boolean isPassable(byte newPosX, byte newPosY) {
        return Suche.getAccessCheck().isAccessible(this, newPosX, newPosY);
    }

    public List<Knoten> expand(boolean noWait) {
        // Macht es einen Unterschied, wenn NEIGHBOUR_POS pro expand aufruf neu erzeugt wird? Ja
        List<Knoten> children = new LinkedList<>();

        for (byte[] neighbour : MyUtil.NEIGHBOUR_POS) {
            if (cost < COST_LIMIT && isPassable((byte) (posX + neighbour[0]), (byte) (posY + neighbour[1]))) {
                children.add(new Knoten(this, (byte) (posX + neighbour[0]), (byte) (posY + neighbour[1])));
            }
        }
        if (!noWait && cost < COST_LIMIT && isPassable(posX, posY)) {
            children.add(new Knoten(this, posX, posY));
        }

        return children;
    }

    public boolean isGoalNode() {
        return Suche.getGoalPred().isGoalNode(this);
    }

    public void executeCallbacks() {
        for (ICallbackFunction callbacks : Suche.getCallbackFuncs()) {
            callbacks.callback(this);
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

    public float heuristicalValue() {
        return Suche.getHeuristicFunc().calcHeuristic(this);
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

    @Override
    public int hashCode() {
        int result = Objects.hash(remainingDots, posX, posY);
        result = 31 * result + Arrays.deepHashCode(view);
        return result;
    }

    // region Getter und Setter
    public static PacmanTileType[][] getStaticWorld() {
        return STATIC_WORLD;
    }

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
