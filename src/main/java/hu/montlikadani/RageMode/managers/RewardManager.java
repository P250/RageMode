package hu.montlikadani.ragemode.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import hu.montlikadani.ragemode.Debug;
import hu.montlikadani.ragemode.ServerVersion.Version;
import hu.montlikadani.ragemode.NMS;
import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;

public class RewardManager {

	private String game;
	private FileConfiguration conf;

	public RewardManager(String game) {
		this.game = game;

		conf = RageMode.getInstance().getConfiguration().getRewardsCfg();
	}

	public String getGame() {
		return game;
	}

	public void rewardForWinner(Player winner) {
		List<String> cmds = conf.getStringList("rewards.end-game.winner.commands");
		List<String> msgs = conf.getStringList("rewards.end-game.winner.messages");
		double cash = conf.getDouble("rewards.end-game.winner.cash");

		if (cmds != null) {
			for (String path : cmds) {
				String[] arg = path.split(": ");
				String cmd = path;
				if (arg.length < 2) {
					arg[0] = "console";
				} else {
					cmd = arg[1];
				}

				cmd = replacePlaceholders(cmd, winner, true);

				if (arg[0].equals("console"))
					Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
				else if (arg[0].equals("player"))
					winner.performCommand(cmd);
			}
		}

		if (msgs != null) {
			for (String path : msgs) {
				path = replacePlaceholders(path, winner, true);
				winner.sendMessage(path);
			}
		}

		if (cash > 0D && RageMode.getInstance().isVaultEnabled())
			RageMode.getInstance().getEconomy().depositPlayer(winner, cash);

		addItems("winner", winner);
	}

	public void rewardForPlayers(Player winner, Player pls) {
		if (winner != null && pls == winner)
			return;

		List<String> cmds = conf.getStringList("rewards.end-game.players.commands");
		List<String> msgs = conf.getStringList("rewards.end-game.players.messages");
		double cash = conf.getDouble("rewards.end-game.players.cash", 0D);

		if (cmds != null) {
			for (String path : cmds) {
				String[] arg = path.split(": ");
				String cmd = path;
				if (arg.length < 2) {
					arg[0] = "console";
				} else {
					cmd = arg[1];
				}

				cmd = replacePlaceholders(cmd, pls, false);

				if (arg[0].equals("console"))
					Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
				else if (arg[0].equals("player"))
					pls.performCommand(cmd);
			}
		}

		if (msgs != null && !msgs.isEmpty()) {
			msgs.forEach(path -> pls.sendMessage(replacePlaceholders(path, pls, false)));
		}

		if (cash > 0D && RageMode.getInstance().isVaultEnabled())
			RageMode.getInstance().getEconomy().depositPlayer(pls, cash);

		addItems("players", pls);
	}

	private String replacePlaceholders(String path, Player p, boolean winner) {
		double cash = winner ? conf.getDouble("rewards.end-game.winner.cash", 0D)
				: conf.getDouble("rewards.end-game.players.cash", 0D);

		path = path.replace("%game%", game);
		path = path.replace("%player%", p.getName());
		path = path.replace("%reward%", cash > 0D ? Double.toString(cash) : "");
		path = Utils.setPlaceholders(path, p);
		return Utils.colors(path);
	}

	@SuppressWarnings("deprecation")
	private void addItems(String path, Player p) {
		if (!conf.contains("rewards.end-game." + path + ".items")) {
			return;
		}

		for (String num : conf.getConfigurationSection("rewards.end-game." + path + ".items").getKeys(false)) {
			String type = conf.getString("rewards.end-game." + path + ".items." + num + ".type");
			if (type == null) {
				continue;
			}

			try {
				Material mat = Material.valueOf(type.toUpperCase());
				if (mat == null) {
					Debug.logConsole(Level.WARNING, "Unknown item name: " + type);
					Debug.logConsole("Find and double check item names using this page:");
					Debug.logConsole("https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html");
					continue;
				}

				if (mat.equals(Material.AIR)) {
					Debug.logConsole("AIR is not supported.");
					continue;
				}

				ItemStack itemStack = new ItemStack(mat);
				itemStack.setAmount(conf.getInt("rewards.end-game." + path + ".items." + num + ".amount", 1));

				if (conf.contains("rewards.end-game." + path + ".items." + num + ".durability"))
					NMS.setDurability(itemStack,
							(short) conf.getDouble("rewards.end-game." + path + ".items." + num + ".durability"));

				if (conf.getBoolean("rewards.end-game." + path + ".items." + num + ".meta")) {
					ItemMeta itemMeta = itemStack.getItemMeta();
					String name = conf.getString("rewards.end-game." + path + ".items." + num + ".name", "");
					if (!name.isEmpty())
						itemMeta.setDisplayName(name.replaceAll("&", "\u00a7"));

					List<String> loreList = conf.getStringList("rewards.end-game." + path + ".items." + num + ".lore");
					if (loreList != null && !loreList.isEmpty())
						itemMeta.setLore(Utils.colorList(loreList));

					if (type.startsWith("LEATHER_")) {
						String color = conf.getString("rewards.end-game." + path + ".items." + num + ".color", "");
						if (!color.isEmpty() && itemMeta instanceof LeatherArmorMeta) {
							((LeatherArmorMeta) itemMeta).setColor(Utils.getColorFromString(color));
						}
					}

					String bannerColor = conf.getString("rewards.end-game." + path + ".items." + num + ".banner.color",
							"");
					String bannerType = conf.getString("rewards.end-game." + path + ".items." + num + ".banner.type",
							"");
					if (!bannerColor.isEmpty() && !bannerType.isEmpty()) {
						if (Version.isCurrentEqualOrLower(Version.v1_12_R1)) {
							if (mat.equals(Material.valueOf("BANNER"))) {
								if (itemMeta instanceof BannerMeta) {
									List<Pattern> patterns = new ArrayList<>();
									patterns.add(new Pattern(DyeColor.valueOf(bannerColor),
											PatternType.valueOf(bannerType)));

									((BannerMeta) itemMeta).setBaseColor(DyeColor.valueOf(bannerColor));
									((BannerMeta) itemMeta).setPatterns(patterns);
								}
							}
						} else if (type.endsWith("_BANNER")) {
							if (itemMeta instanceof BannerMeta) {
								List<Pattern> patterns = new ArrayList<>();
								patterns.add(
										new Pattern(DyeColor.valueOf(bannerColor), PatternType.valueOf(bannerType)));

								((Banner) itemStack).setBaseColor(DyeColor.valueOf(bannerColor));
								((BannerMeta) itemMeta).setPatterns(patterns);
							}
						}
					}

					itemStack.setItemMeta(itemMeta);

					List<String> enchantList = conf
							.getStringList("rewards.end-game." + path + ".items." + num + ".enchants");
					if (enchantList != null) {
						for (String enchant : enchantList) {
							String[] split = enchant.split(":");
							try {
								if (itemStack.getItemMeta() instanceof EnchantmentStorageMeta) {
									EnchantmentStorageMeta enchMeta = (EnchantmentStorageMeta) itemStack.getItemMeta();
									enchMeta.addStoredEnchant(NMS.getEnchant(split[0]),
											(split.length > 2 ? Integer.parseInt(split[1]) : 1), true);
									itemStack.setItemMeta(enchMeta);
								} else
									itemStack.addUnsafeEnchantment(NMS.getEnchant(split[0]),
											Integer.parseInt(split[1]));
							} catch (IllegalArgumentException b) {
								Debug.logConsole(Level.WARNING, "Bad enchantment name: " + split[0]);
								continue;
							}
						}
					}
				}
				try {
					if (conf.contains("rewards.end-game." + path + ".items." + num + ".slot"))
						p.getInventory().setItem(conf.getInt("rewards.end-game." + path + ".items." + num + ".slot"),
								itemStack);
					else
						p.getInventory().addItem(itemStack);
				} catch (IllegalArgumentException i) {
					Debug.logConsole(Level.WARNING, "Slot is not between 0 and 8 inclusive.");
				}
			} catch (Exception e) {
				Debug.logConsole(Level.WARNING, "Problem occured with your item: " + e.getMessage());
			}
		}
	}
}