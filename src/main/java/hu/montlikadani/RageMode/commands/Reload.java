package hu.montlikadani.ragemode.commands;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.holder.HoloHolder;
import hu.montlikadani.ragemode.signs.SignConfiguration;
import hu.montlikadani.ragemode.signs.SignCreator;

public class Reload extends RmCommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender) {
		if (sender instanceof org.bukkit.entity.Player && !hasPerm(sender, "ragemode.admin.reload")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}

		for (String game : GetGames.getGameNames()) {
			if (game != null && PlayerList.isGameRunning(game))
				GameUtils.broadcastToGame(game, RageMode.getLang().get("game.game-stopped-for-reload"));
		}

		StopGame.stopAllGames();

		plugin.getConfiguration().loadConfig();
		RageMode.getLang().loadLanguage(plugin.getConfiguration().getCfg().getString("language"));

		if (plugin.getConfiguration().getCfg().getBoolean("signs.enable"))
			SignConfiguration.initSignConfiguration();

		plugin.loadListeners();

		for (String game : GetGames.getGameNames()) {
			if (game != null)
				SignCreator.updateAllSigns(game);
		}

		if (plugin.isHologramEnabled())
			HoloHolder.initHoloHolder();

		sendMessage(sender, RageMode.getLang().get("commands.reload.success"));
		return false;
	}
}