package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMGameCreateEvent;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameSpawn;
import hu.montlikadani.ragemode.gameLogic.GameZombieSpawn;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public class addgame implements ICommand {

	@Override
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
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%",
					"/rm addgame <gameName> [maxPlayers] [minPlayers] [gameType]"));
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

		int m = 2;
		if (args.length == 4) {
			try {
				m = Integer.parseInt(args[3]);
			} catch (NumberFormatException e) {
				sendMessage(p, RageMode.getLang().get("not-a-number", "%wrong-number%", args[3]));
				return false;
			}

			if (m < 2) {
				sendMessage(p, RageMode.getLang().get("setup.at-least-two"));
				return false;
			}
		}

		GameType type = GameType.NORMAL;
		if (args.length == 5) {
			type = GameType.getByName(args[4]);

			if (type == null) {
				type = GameType.NORMAL;
			}
		}

		Game g = new Game(game, type);
		plugin.getGames().add(g);

		Utils.callEvent(new RMGameCreateEvent(g, x, m));

		FileConfiguration c = plugin.getConfiguration().getArenasCfg();
		c.set("arenas." + game + ".maxplayers", x);
		c.set("arenas." + game + ".minplayers", m);
		c.set("arenas." + game + ".world", p.getWorld().getName());
		c.set("arenas." + game + ".gametype", type.getName());
		Configuration.saveFile(c, plugin.getConfiguration().getArenasFile());

		if (type == GameType.APOCALYPSE) {
			plugin.getSpawns().add(new GameZombieSpawn(g));
		}

		plugin.getSpawns().add(new GameSpawn(g));

		sendMessage(p, RageMode.getLang().get("setup.addgame.success-added", "%game%", game));
		return true;
	}
}
