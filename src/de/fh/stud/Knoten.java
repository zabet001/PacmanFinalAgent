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

    public static final byte[][] NEIGHBOUR_POS = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
    private static PacmanTileType[][] STATIC_WORLD;
    private static final short COST_LIMIT = 1000;
/*
    private static final byte POWERPILL_DURATION = 9 ; // 1 mehr, da bei 0 Effekt aufhoert
*/

    //region BUG
    // TODO: FIX
    /*Debug: Erzeugt Bug bei PW_easy_01_powerpill_(2) (BUG)*/
    private static final byte POWERPILL_DURATION = 10; // Damit auch ohne Powerpill-Effekt auf einem Geist gegangen wird
    // endregion

    private final Knoten pred;
    private final byte[][] view;

    private byte posX, posY;

    private byte powerpillTimer; // != 0: unverwundbar
    private final short cost;

    private final int heuristic;

    // TODO Idee: Zusatzinformationen fuer Knoten (dotsEaten, powerPillTimer etc.) in Extra-Objekt speichern
    // Problem: Wie den Code auslagern?
    public static Knoten generateRoot(PacmanTileType[][] world, int posX, int posY) {
        Knoten.STATIC_WORLD = world;
        return new Knoten((byte) posX, (byte) posY);
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
            this.view[posX][posY] = MyUtil.tileToByte(PacmanTileType.EMPTY);
        } else {
            // Kindknoten
            if (!Suche.isStateSearch() || pred.view[posX][posY] == MyUtil.tileToByte(PacmanTileType.EMPTY)) {
                this.view = pred.view;
            } else {
                this.view = MyUtil.copyView(pred.view);
                if (MyUtil.isGhostType(MyUtil.byteToTile(this.view[posX][posY]))) {
                    if (MyUtil.byteToTile(this.view[posX][posY]) == PacmanTileType.GHOST_AND_DOT || MyUtil.byteToTile(this.view[posX][posY]) == PacmanTileType.GHOST_AND_POWERPILL)
                        this.view[posX][posY] = MyUtil.tileToByte(PacmanTileType.GHOST);
                } else
                    this.view[posX][posY] = MyUtil.tileToByte(PacmanTileType.EMPTY);
            }
            if (pred.view[posX][posY] == MyUtil.tileToByte(PacmanTileType.POWERPILL) || pred.view[posX][posY] == MyUtil.tileToByte(PacmanTileType.GHOST_AND_POWERPILL)) {
                powerpillTimer = POWERPILL_DURATION;
            } else {
                powerpillTimer = pred.powerpillTimer;
                if (powerpillTimer > 0) // tritt das wirklich nie ein?
                    powerpillTimer--;
            }
        }
        this.cost = pred == null ? 0 : (short) (pred.cost + 1);
        this.heuristic = Suche.getHeuristicFunc().calcHeuristic(this);
    }

    // region Klassenmethoden

    public static int nodeNeighbourCnt(Knoten node) {
        int neighbourCnt = 0;
        for (byte[] neighbour : NEIGHBOUR_POS) {
            if (node.isPassable((byte) (node.getPosX() + neighbour[0]), (byte) (node.getPosY() + neighbour[1]))) {
                neighbourCnt++;
            }
        }
        return neighbourCnt;
    }

    // endregion
    public boolean isPassable(byte newPosX, byte newPosY) {
        return Suche.getAccessCheck().isAccessible(this, newPosX, newPosY);
    }

    public List<Knoten> expand() {
        // Macht es einen Unterschied, wenn NEIGHBOUR_POS pro expand aufruf neu erzeugt wird? Ja
        List<Knoten> children = new LinkedList<>();

        for (byte[] neighbour : NEIGHBOUR_POS) {
            if (cost < COST_LIMIT && isPassable((byte) (posX + neighbour[0]), (byte) (posY + neighbour[1]))) {
                children.add(new Knoten(this, (byte) (posX + neighbour[0]), (byte) (posY + neighbour[1])));
            }
        }
        children.add(new Knoten(this, posX, posY));

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
        return heuristic == knoten.heuristic && posX == knoten.posX && posY == knoten.posY && Arrays.deepEquals(view,
                knoten.view);

    }

    @Override
    public int hashCode() {
        int result = Objects.hash(posX, posY);
        result = 31 * result + Arrays.deepHashCode(view);
        return result;
    }

    // region Setup
    public int countDots() {
        int cnt = 0;
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

    public int getCost() {
        return cost;
    }

    public int getHeuristic() {
        return heuristic;
    }

    public byte getPowerpillTimer() {
        return powerpillTimer;
    }

    // endregion
}
