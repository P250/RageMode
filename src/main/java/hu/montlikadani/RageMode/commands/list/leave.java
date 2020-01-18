package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMGameLeaveAttemptEvent;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public class leave {

	public boolean run(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}

		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.leave")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}

		// Make sure the meta removed
		p.removeMetadata("killedWith", RageMode.getInstance());

		Game game = GameUtils.getGameByPlayer(p);
		if (game == null) {
			p.sendMessage(RageMode.getLang().get("game.player-not-ingame"));
			return false;
		}

		GameUtils.runCommands(p, game.getName(), "leave");
		GameUtils.sendActionBarMessages(p, game.getName(), "leave");
		RMGameLeaveAttemptEvent gameLeaveEvent = new RMGameLeaveAttemptEvent(game, p);
		Utils.callEvent(gameLeaveEvent);
		if (!gameLeaveEvent.isCancelled()) {
			game.removePlayer(p);
		}

		game.removeSpectatorPlayer(p);

		return true;
	}
}