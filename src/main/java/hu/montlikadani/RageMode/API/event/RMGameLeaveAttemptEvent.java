package hu.montlikadani.ragemode.API.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import hu.montlikadani.ragemode.gameLogic.Game;

public class RMGameLeaveAttemptEvent extends BaseEvent implements Cancellable {

	private Game game;
	private Player player;
	private boolean cancelled = false;

	public RMGameLeaveAttemptEvent(Game game, Player player) {
		this.game = game;
		this.player = player;
	}

	public Game getGame() {
		return game;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}