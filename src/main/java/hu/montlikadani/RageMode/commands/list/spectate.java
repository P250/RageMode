package hu.montlikadani.ragemode.commands.list;

import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.items.LeaveGame;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class spectate {

	public boolean run(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}

		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.spectate")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
			return false;
		}

		String map = args[1];
		if (!GameUtils.isGameWithNameExists(map)) {
			sendMessage(p, RageMode.getLang().get("invalid-game"));
			return false;
		}

		if (!GameUtils.getGame(map).isGameRunning(map)) {
			sendMessage(p, RageMode.getLang().get("game.not-running"));
			return false;
		}

		if (GameUtils.isPlayerPlaying(p)) {
			sendMessage(p, RageMode.getLang().get("game.player-not-switch-spectate"));
			return false;
		}

		if (GameUtils.getGame(map).addSpectatorPlayer(p, map)) {
			p.teleport(GameUtils.getGameSpawn(map).getRandomSpawn());

			p.setAllowFlight(true);
			p.setFlying(true);
			p.setGameMode(GameMode.SPECTATOR);
			if (RageMode.getInstance().getConfiguration().getCfg().contains("items.leavegameitem"))
				p.getInventory().setItem(RageMode.getInstance().getConfiguration().getCfg().getInt("items.leavegameitem.slot"),
						LeaveGame.getItem());
		}

		return true;
	}
}
