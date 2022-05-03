package de.fh.stud;

import de.fh.pacman.PacmanPercept;
import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanActionEffect;
import de.fh.pacman.enums.PacmanTileType;

public class GameStateObserver {
    static final byte POWERPILL_DURATION = 9; // 1 mehr, da bei 0 Effekt aufhoert
    public static PacmanPercept newPercept;
    public static PacmanTileType[][] currentWorld;
    public static PacmanPercept lastPercept;
    public static PacmanAction lastAction;
    public static PacmanActionEffect lastActionEffect;
    public static byte powerpillTimer;

    public static void updateGameStateBeforeAction(PacmanPercept percept, PacmanActionEffect actionEffect) {
        newPercept = percept;
        currentWorld = percept.getView();
        currentWorld[percept.getPosX()][percept.getPosY()] = PacmanTileType.EMPTY;

        lastActionEffect = actionEffect;
        if (lastActionEffect == PacmanActionEffect.ATE_POWERPILL)
            powerpillTimer = POWERPILL_DURATION;
        else if (powerpillTimer > 0)
            powerpillTimer--;
    }

    public static void updateGameStateAfterAction(PacmanAction nextAction) {
        lastAction = nextAction;
        lastPercept = newPercept;
    }
}
