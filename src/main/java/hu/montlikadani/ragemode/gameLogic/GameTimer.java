package hu.montlikadani.ragemode.gameLogic;

import java.util.TimerTask;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.gameUtils.ActionMessengers;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class GameTimer extends TimerTask {

	private Game game;
	private int time;

	public GameTimer(Game game, int time) {
		this.game = game;
		this.time = time;
	}

	public Game getGame() {
		return game;
	}

	public int getGameTime() {
		return time;
	}

	@Override
	public void run() {
		try { // Stop the game if something wrong or missing
			if (!game.isGameRunning()) {
				cancel();
				return;
			}

			if (game.getPlayers().size() < 2) {
				GameUtils.stopGame(game.getName());
				cancel();
				return;
			}

			// Broadcast time message should be in this place, before counting
			for (String val : RageMode.getInstance().getConfiguration().getCfg()
					.getStringList("game.values-to-send-game-end-broadcast")) {
				if (time == Integer.parseInt(val)) {
					GameUtils.broadcastToGame(game,
							RageMode.getLang().get("game.broadcast.game-end", "%time%", Utils.getFormattedTime(time)));
					break;
				}
			}

			for (ActionMessengers ac : game.getActionMessengers()) {
				ac.setScoreboard(time);
				ac.setTabList(time);
				ac.setTeam();
			}

			if (time == 0) {
				cancel();
				GameUtils.stopGame(game.getName());
				return;
			}

			time--;
		} catch (Exception e) {
			e.printStackTrace();
			cancel();
			GameUtils.forceStopGame(game);
			return;
		}
	}
}
