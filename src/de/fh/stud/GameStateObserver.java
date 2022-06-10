package de.fh.stud;

import de.fh.pacman.PacmanPercept;
import de.fh.pacman.enums.PacmanAction;
import de.fh.pacman.enums.PacmanActionEffect;
import de.fh.pacman.enums.PacmanTileType;

public class GameStateObserver {

	// Standardwert: 9 (tatsaechliche Laenge)
	// IDEE: Wenn Geist gefressen: powerpillTimer auf 1 setzen, da Gefahr ab naechsten Respawn
	// (in Knoten Konstruktor und in GameState.updateBeforeAction)
	private static final byte POWERPILL_DURATION = 9; // 1 mehr, da bei 0 Effekt aufhoert
	public static GameState gameState;

	public static void updateGameStateBeforeAction(PacmanPercept percept, PacmanActionEffect actionEffect) {
		gameState.setNewPercept(percept);
		gameState.setCurrentWorld(percept.getView());
		gameState.getCurrentWorld()[percept.getPosX()][percept.getPosY()] = PacmanTileType.EMPTY;

		gameState.setNewActionEffect(actionEffect);
		if (gameState.getNewActionEffect() == PacmanActionEffect.ATE_POWERPILL) {
			gameState.setPowerpillTimer(getPowerpillDuration());
		}
		// IDEE: Wenn Geist gefressen: powerpillTimer auf 1 setzen, da Gefahr ab naechsten Respawn
		else if (gameState.getNewActionEffect() == PacmanActionEffect.ATE_GHOST
				|| gameState.getNewActionEffect() == PacmanActionEffect.ATE_GHOST_AND_DOT
				|| gameState.getNewActionEffect() == PacmanActionEffect.ATE_GHOST_AND_POWERPILL) {
			gameState.setPowerpillTimer((byte) 1);
		}
		else if (gameState.getPowerpillTimer() > 0) {
			gameState.setPowerpillTimer((byte) (gameState.getPowerpillTimer() - 1));
		}
		if (gameState.getNewActionEffect() == PacmanActionEffect.ATE_DOT
				|| gameState.getNewActionEffect() == PacmanActionEffect.ATE_GHOST_AND_DOT) {
			gameState.setRemainingDots((short) (gameState.getRemainingDots() - 1));
		}
	}

	public static void updateGameStateAfterAction(PacmanAction nextAction) {
		gameState.setLastAction(nextAction);
		gameState.setLastPercept(gameState.getNewPercept());
	}

	public static void reset() {
		gameState = new GameState();

	}

	public static GameState getGameState() {
		return gameState;
	}

	public static byte getPowerpillDuration() {
		return POWERPILL_DURATION;
	}

	public static class GameState {
		private PacmanPercept lastPercept;
		private PacmanAction lastAction;
		private PacmanActionEffect newActionEffect;
		private PacmanTileType[][] currentWorld;
		private byte powerpillTimer;
		private PacmanPercept newPercept;
		private short remainingDots;

		public PacmanPercept getLastPercept() {
			return lastPercept;
		}

		private void setLastPercept(PacmanPercept lastPercept) {
			this.lastPercept = lastPercept;
		}

		public PacmanAction getLastAction() {
			return lastAction;
		}

		private void setLastAction(PacmanAction lastAction) {
			this.lastAction = lastAction;
		}

		public PacmanActionEffect getNewActionEffect() {
			return newActionEffect;
		}

		private void setNewActionEffect(PacmanActionEffect newActionEffect) {
			this.newActionEffect = newActionEffect;
		}

		public PacmanTileType[][] getCurrentWorld() {
			return currentWorld;
		}

		private void setCurrentWorld(PacmanTileType[][] currentWorld) {
			this.currentWorld = currentWorld;
		}

		public byte getPowerpillTimer() {
			return powerpillTimer;
		}

		private void setPowerpillTimer(byte powerpillTimer) {
			this.powerpillTimer = powerpillTimer;
		}

		public PacmanPercept getNewPercept() {
			return newPercept;
		}

		private void setNewPercept(PacmanPercept newPercept) {
			this.newPercept = newPercept;
		}

		public short getRemainingDots() {
			return remainingDots;
		}

		public void setRemainingDots(short remainingDots) {
			this.remainingDots = remainingDots;
		}
	}

}
