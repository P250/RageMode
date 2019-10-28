package hu.montlikadani.ragemode.API.event;

import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameStatus;

public class GameStatusChangeEvent extends BaseEvent {

	private Game game;
	private GameStatus status;

	public GameStatusChangeEvent(Game game, GameStatus status) {
		this.game = game;
		this.status = status;
	}

	public Game getGame() {
		return game;
	}

	public GameStatus getStatus() {
		return status;
	}
}
