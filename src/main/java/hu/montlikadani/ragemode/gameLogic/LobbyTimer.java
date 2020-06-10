package hu.montlikadani.ragemode.gameLogic;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.managers.PlayerManager;

public class LobbyTimer extends TimerTask {

	private Game game;
	private int time;

	public LobbyTimer(Game game, int time) {
		this.game = game;
		this.time = time;
	}

	public Game getGame() {
		return game;
	}

	public void setLobbyTime(int time) {
		this.time = time;
	}

	public void addLobbyTime(int newTime) {
		time += newTime;
	}

	public int getLobbyTime() {
		return time;
	}

	public void loadTimer() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(this, 0, 60 * 20L);
	}

	@Override
	public void run() {
		List<PlayerManager> list = game.getPlayersFromList();
		if (game.isGameRunning()) {
			// Set level counter back to 0
			list.forEach(p -> p.getPlayer().setLevel(0));
			cancel();
			return;
		}

		if (game.getPlayers().size() < GetGames.getMinPlayers(game.getName())) {
			game.setStatus(GameStatus.WAITING);
			list.forEach(p -> p.getPlayer().setLevel(0));
			cancel();
			return;
		}

		List<String> values = RageMode.getInstance().getConfiguration().getCfg()
				.getStringList("lobby.values-to-send-start-message");
		for (String val : values) {
			if (time == Integer.parseInt(val)) {
				GameUtils.broadcastToGame(game, RageMode.getLang().get("game.lobby.start-message", "%time%", val));
				break;
			}
		}

		if (ConfigValues.isPlayerLevelAsTimeCounter()) {
			for (PlayerManager pm : list) {
				Player player = pm.getPlayer();
				player.setLevel(time);
			}
		}

		if (ConfigValues.isLobbyTitle()) {
			List<String> titleValues = RageMode.getInstance().getConfiguration().getCfg()
					.getStringList("titles.lobby-waiting.values-to-send-start-message");
			for (String val : titleValues) {
				if (time == Integer.parseInt(val)) {
					String title = ConfigValues.getLobbyTitle(),
							sTitle = ConfigValues.getLobbySubTitle(),
							times = ConfigValues.getLobbyTitleTime();

					title = title.replace("%time%", val);
					title = title.replace("%game%", game.getName());

					sTitle = sTitle.replace("%time%", val);
					sTitle = sTitle.replace("%game%", game.getName());

					for (PlayerManager pm : list) {
						GameUtils.sendTitleMessages(pm.getPlayer(), title, sTitle, times);
					}

					break;
				}
			}
		}

		if (time == 0) {
			cancel();
			list.forEach(pl -> pl.getPlayer().setLevel(0));

			GameLoader loder = new GameLoader(game);
			loder.startGame();
			return;
		}

		time--;
	}
}
