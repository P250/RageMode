package hu.montlikadani.ragemode.gameLogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.commands.StopGame;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameUtils.ScoreBoard;
import hu.montlikadani.ragemode.gameUtils.ScoreTeam;
import hu.montlikadani.ragemode.gameUtils.TabTitles;

public class GameTimer extends TimerTask {

	private List<String> listPlayers = new ArrayList<>();
	private List<Player> le = new ArrayList<>();

	private TabTitles gameTab = null;
	private ScoreBoard gameBoard = null;
	private ScoreTeam scoreTeam = null;

	private String gameName;
	private int time;

	public GameTimer(String gameName, int time) {
		this.gameName = gameName;
		this.time = time;
	}

	public String getGame() {
		return gameName;
	}

	public int getGameTime() {
		return time;
	}

	public TabTitles getTabTitles() {
		return gameTab;
	}

	public ScoreBoard getScoreboard() {
		return gameBoard;
	}

	public ScoreTeam getScoreTeam() {
		return scoreTeam;
	}

	public void loadModules() {
		listPlayers = Arrays.asList(PlayerList.getPlayersInGame(gameName));
		Configuration conf = RageMode.getInstance().getConfiguration();

		if (conf.getCfg().getBoolean("game.global.scoreboard.enable")) {
			gameBoard = new ScoreBoard(listPlayers, false);
			gameBoard.addToScoreBoards(gameName, true);
		}

		if (conf.getCfg().getBoolean("game.global.tablist.list.enable")) {
			gameTab = new TabTitles(listPlayers);
			gameTab.addToTabList(gameName, true);
		}

		if (conf.getCfg().getBoolean("game.global.tablist.player-format.enable")) {
			scoreTeam = new ScoreTeam(listPlayers);
			scoreTeam.addToTeam(gameName, true);
		}
	}

	@Override
	public void run() {
		if (!PlayerList.isGameRunning(gameName)) {
			cancel();
			return;
		}
		if (PlayerList.getPlayersInGame(gameName).length < 2) {
			cancel();
			return;
		}
		time--;

		for (String playerUUID : listPlayers) {
			Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
			String tFormat = Utils.getFormattedTime(time);
			Configuration conf = RageMode.getInstance().getConfiguration();

			if (player == null || !player.isOnline())
				return;

			if (conf.getCfg().getBoolean("game.global.show-name-above-player-when-look")) {
				// making the game more difficult
				for (Entity e : player.getNearbyEntities(Math.round(10), Math.round(10), Math.round(10))) {
					if (e instanceof Player) {
						Player p = (Player) e;

						if (!le.contains(p))
							le.add(p);

						p.setCustomNameVisible(false);

						// TODO fix issue when the player looks through the block and the name appears within the 10 radius
						if (GameLoader.getLookingAt(player, p))
							p.setCustomNameVisible(true);
					}
				}
			}

			if (gameTab != null) {
				List<String> tabHeader = conf.getCfg().getStringList("game.global.tablist.list.header");
				List<String> tabFooter = conf.getCfg().getStringList("game.global.tablist.list.footer");

				String he = "";
				String fo = "";
				int s = 0;
				for (String line : tabHeader) {
					s++;
					if (s > 1)
						he = he + "\n\u00a7r";

					he = he + line;
				}
				s = 0;
				for (String line : tabFooter) {
					s++;
					if (s > 1)
						fo = fo + "\n\u00a7r";

					fo = fo + line;
				}

				he = Utils.setPlaceholders(he, player);
				he = he.replace("%game-time%", tFormat);

				fo = Utils.setPlaceholders(fo, player);
				fo = fo.replace("%game-time%", tFormat);

				gameTab.sendTabTitle(he, fo);
			}

			if (scoreTeam != null) {
				String prefix = conf.getCfg().getString("game.global.tablist.player-format.prefix");
				String suffix = conf.getCfg().getString("game.global.tablist.player-format.suffix");

				prefix = Utils.setPlaceholders(prefix, player);
				suffix = Utils.setPlaceholders(suffix, player);

				scoreTeam.setTeam(prefix, suffix);
			}

			if (gameBoard != null) {
				String boardTitle = conf.getCfg().getString("game.global.scoreboard.title");
				if (boardTitle != null && !boardTitle.equals(""))
					gameBoard.setTitle(RageMode.getLang().colors(boardTitle));

				List<String> rows = conf.getCfg().getStringList("game.global.scoreboard.content");
				if (rows != null && !rows.isEmpty()) {
					int rowMax = rows.size();

					for (String row : rows) {
						if (row.trim().equals("")) {
							for (int i = 0; i <= rowMax; i++) {
								row = row + " ";
							}
						}

						row = Utils.setPlaceholders(row, player);
						row = row.replace("%game-time%", tFormat);

						gameBoard.setLine(row, rowMax);
						rowMax--;

						gameBoard.setScoreBoard();
					}
				}
			}
		}

		if (time == 0) {
			if (le != null && !le.isEmpty()) {
				for (int i = 0; i < le.size(); i++) {
					if (le.get(i).isCustomNameVisible())
						le.get(i).setCustomNameVisible(true);

					le.clear();
				}
			}

			cancel();
			RageMode.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RageMode.getInstance(),
					() -> StopGame.stopGame(gameName));
		}
	}
}
