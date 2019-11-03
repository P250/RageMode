package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMGameCreateEvent;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.events.BungeeListener;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameSpawn;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class addgame {

	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}

		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.admin.addgame")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm addgame <gameName> [maxPlayers]"));
			return false;
		}

		String game = args[1];
		if (GameUtils.isGameWithNameExists(game)) {
			sendMessage(p, RageMode.getLang().get("setup.addgame.already-exists", "%game%", game));
			return false;
		}

		if (!GameUtils.checkName(p, game)) {
			return false;
		}

		int x = 4;
		if (args.length == 3) {
			try {
				x = Integer.parseInt(args[2]);
			} catch (NumberFormatException e) {
				sendMessage(p, RageMode.getLang().get("not-a-number", "%wrong-number%", args[2]));
				return false;
			}

			if (x < 2) {
				sendMessage(p, RageMode.getLang().get("setup.at-least-two"));
				return false;
			}
		}

		if (plugin.getConfiguration().getCV().isBungee())
			plugin.getServer().getPluginManager().registerEvents(new BungeeListener(game), plugin);

		Game g = new Game(game);
		plugin.getGames().add(g);

		plugin.getSpawns().add(new GameSpawn(g));

		Utils.callEvent(new RMGameCreateEvent(g, x));

		plugin.getConfiguration().getArenasCfg().set("arenas." + game + ".maxplayers", x);
		plugin.getConfiguration().getArenasCfg().set("arenas." + game + ".world", p.getWorld().getName());
		Configuration.saveFile(plugin.getConfiguration().getArenasCfg(), plugin.getConfiguration().getArenasFile());

		sendMessage(p, RageMode.getLang().get("setup.addgame.success-added", "%game%", game));
		return false;
	}
}
