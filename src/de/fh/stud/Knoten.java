package de.fh.stud;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.Suchen.Suche;
import de.fh.stud.interfaces.IAccessibilityChecker;
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

    private byte posX, posY;

    private final byte powerpillTimer; // != 0: unverwundbar
    private final short cost;

    private final short remainingDots;

    // TODO Idee: Zusatzinformationen fuer Knoten (dotsEaten, powerPillTimer etc.) in Extra-Objekt speichern
    // Problem: Wie den Code auslagern?

    public static Knoten generateRoot(PacmanTileType[][] world, int posX, int posY) {
        Knoten.STATIC_WORLD = world;
        PacmanTileType zw = world[posX][posY];
        world[posX][posY] = PacmanTileType.EMPTY;
        Knoten ret = new Knoten((byte) posX, (byte) posY);
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
            this.remainingDots = countDots();
            this.powerpillTimer = Suche.isStateSearch() ? GameStateObserver.powerpillTimer : 0;
        } else {
            // Kindknoten
            if (Suche.isStateSearch() && MyUtil.isPowerpillType(MyUtil.byteToTile(pred.view[posX][posY]))) {
                powerpillTimer = GameStateObserver.POWERPILL_DURATION;
            } else {
                powerpillTimer = (byte) (pred.powerpillTimer - (pred.powerpillTimer > 0 ? 1 : 0));
            }

            if (!Suche.isStateSearch() || pred.view[posX][posY] == MyUtil.tileToByte(PacmanTileType.EMPTY)) {
                this.view = pred.view;
            } else {
                this.view = MyUtil.copyView(pred.view);

                if (MyUtil.isGhostType(MyUtil.byteToTile(this.view[posX][posY])))
                    this.view[posX][posY] = MyUtil.tileToByte(PacmanTileType.GHOST);
                else
                    this.view[posX][posY] = MyUtil.tileToByte(PacmanTileType.EMPTY);
            }
            if (Suche.isStateSearch() && MyUtil.isDotType(MyUtil.byteToTile(pred.view[posX][posY])))
                this.remainingDots = (short) (pred.remainingDots - 1);
            else
                this.remainingDots = pred.remainingDots;
            this.cost = (short) (pred.cost + 1);
        }

    }

    // region Klassenmethoden

    public int nodeNeighbourCnt() {
        return MyUtil.adjacentFreeFieldsCnt(this,posX,posY);

/*        int neighbourCnt = 0;
        for (byte[] neighbour : NEIGHBOUR_POS) {
            if (node.isPassable((byte) (node.getPosX() + neighbour[0]), (byte) (node.getPosY() + neighbour[1]))) {
                neighbourCnt++;
            }
        }
        return neighbourCnt;*/
    }

    // endregion
    public boolean isPassable(IAccessibilityChecker accessibilityChecker,byte newPosX, byte newPosY) {
        return accessibilityChecker.isAccessible(this, newPosX, newPosY);
    }

    public List<Knoten> expand(boolean noWait) {
        // Macht es einen Unterschied, wenn NEIGHBOUR_POS pro expand aufruf neu erzeugt wird? Ja
        List<Knoten> children = new LinkedList<>();

        for (byte[] neighbour : MyUtil.NEIGHBOUR_POS) {
            if (cost < COST_LIMIT && isPassable((byte) (posX + neighbour[0]), (byte) (posY + neighbour[1]))) {
                children.add(new Knoten(this, (byte) (posX + neighbour[0]), (byte) (posY + neighbour[1])));
            }
        }
        if(!noWait) children.add(new Knoten(this, posX, posY));

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
        if (pred == null)
            return PacmanAction.WAIT;
        else {
            if (posX > pred.posX)
                return PacmanAction.GO_EAST;
            else if (posX < pred.posX)
                return PacmanAction.GO_WEST;
            else if (posY > pred.posY)
                return PacmanAction.GO_SOUTH;
            else if (posY < pred.posY)
                return PacmanAction.GO_NORTH;
            else
                return PacmanAction.WAIT;
        }
    }

    public List<PacmanAction> identifyActionSequence() {
        List<PacmanAction> ret = new LinkedList<>();
        Knoten it = this;
        while (it.pred != null) {
            ret.add(0, it.previousAction());
            it = it.pred;
        }
        if (ret.size() == 0)
            ret.add(PacmanAction.WAIT);
        return ret;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Knoten knoten = (Knoten) o;
        // TODO (?) gehoert der powerpillTimer mit in equals?
        return remainingDots == knoten.remainingDots && posX == knoten.posX && posY == knoten.posY
                && heuristicalValue() == knoten.heuristicalValue()
                && Arrays.deepEquals(view, knoten.view);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(posX, posY);
        result = 31 * result + Arrays.deepHashCode(view);
        return result;
    }

    // region Setup
    public short countDots() {
        short cnt = 0;
        for (byte[] rowVals : view) {
            for (int col = 0; col < view[0].length; col++) {
                if (rowVals[col] == MyUtil.tileToByte(PacmanTileType.DOT) || rowVals[col] == MyUtil.tileToByte(PacmanTileType.GHOST_AND_DOT))
                    cnt++;
            }
        }
        return cnt;
    }

    // endregion

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

    public void setPos(Vector2 pos) {
        posX = (byte) pos.x;
        posY = (byte) pos.y;
    }

    public byte getPowerpillTimer() {
        return powerpillTimer;
    }

    public int getRemainingDots() {
        return remainingDots;
    }

    public int getCost() {
        return cost;
    }

    public int heuristicalValue() {
        return Suche.getHeuristicFunc().calcHeuristic(this);
    }
    // endregion
}
