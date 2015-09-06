package org.kwstudios.play.ragemode.gameLogic;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.kwstudios.play.ragemode.scores.RageScores;
import org.kwstudios.play.ragemode.toolbox.ConstantHolder;
import org.kwstudios.play.ragemode.toolbox.GetGames;
import org.kwstudios.play.ragemode.toolbox.TableList;

public class PlayerList {
	private static FileConfiguration fileConfiguration;
	private static String[] list = new String[1]; // [Gamemane,Playername x overallMaxPlayers,Gamename,...]
	public static TableList<Player, Location> oldLocations = new TableList<Player, Location>();
	public static TableList<Player, ItemStack[]> oldInventories = new TableList<Player, ItemStack[]>();
	public static TableList<Player, ItemStack[]> oldArmor = new TableList<Player, ItemStack[]>();
	public static TableList<Player, Double> oldHealth = new TableList<Player, Double>();
	public static TableList<Player, Integer> oldHunger = new TableList<Player, Integer>();
	public static TableList<Player, GameMode> oldGameMode = new TableList<Player, GameMode>();
	
	private static String[] runningGames = new String[1];

	public PlayerList(FileConfiguration fileConfiguration) {
		int i = 0;
		int imax = GetGames.getConfigGamesCount(fileConfiguration);
		String[] games = GetGames.getGameNames(fileConfiguration);
		PlayerList.fileConfiguration = fileConfiguration;
		list = Arrays
				.copyOf(list,
						GetGames.getConfigGamesCount(fileConfiguration)
								* (GetGames
										.getOverallMaxPlayers(fileConfiguration) + 1));
		while(i < imax) {
			list[i*GetGames.getOverallMaxPlayers(fileConfiguration)] = games[i];
			i++;
		}
		runningGames = Arrays.copyOf(runningGames,
				GetGames.getConfigGamesCount(fileConfiguration));
	}

	public static String[] getPlayersInGame(String game) {
		int maxPlayers = GetGames.getMaxPlayers(game, fileConfiguration);
		if(maxPlayers == -1)
			return null;
		String[] players = new String[maxPlayers];

		int i = 0;
		int n;
		int imax = GetGames.getConfigGamesCount(fileConfiguration)
				* (GetGames.getOverallMaxPlayers(fileConfiguration) + 1);
		int playersPerGame = GetGames.getOverallMaxPlayers(fileConfiguration);
		while (i <= imax) {
			if(list[i] != null) {
				if (list[i].equals(game)) {
					n = i;
					int x = 0;
					while (n < GetGames.getMaxPlayers(game, fileConfiguration) + i - 1) {
						if(list[n + 1] == null)
							n++;
						else {
							players[x] = list[n + 1];
							n++;
							x++;
						}
					}
					players = Arrays.copyOf(players, x);
				}
			}
		    i = i + playersPerGame;		
		}
		return players;
	}

	public static boolean addPlayer(Player player, String game, FileConfiguration fileConfiguration) {
		if (isGameRunning(game)) {
			player.sendMessage(ConstantHolder.RAGEMODE_PREFIX + "This Game is already running.");
			return false;
		}

		int i, n;
		i = 0;
		n = 0;
		int kickposition;
		int imax = GetGames.getConfigGamesCount(fileConfiguration)
				* (GetGames.getOverallMaxPlayers(fileConfiguration) + 1);
		int playersPerGame = GetGames.getOverallMaxPlayers(fileConfiguration);
		while (i < imax) {
			if(list[i] != null) {
				if (player.getUniqueId().toString().equals(list[i])) {
					player.sendMessage(ConstantHolder.RAGEMODE_PREFIX + "You are already in a game. You can leave it by typing /rm leave");
					return false;
				}
			}

			i++;
		}
		i = 0;
		while (i < imax) {
			if (list[i] != null) {
				if (list[i].equals(game)) {
					n = i;
					while (n <= GetGames.getMaxPlayers(game, fileConfiguration)) {
						if (list[n] == null) {
							list[n] = player.getUniqueId().toString();
							player.sendMessage(ConstantHolder.RAGEMODE_PREFIX + "You joined "
									+ ChatColor.DARK_AQUA + game
									+ ChatColor.WHITE + ".");

							if (getPlayersInGame(game).length == 2) {
								new LobbyTimer(game, fileConfiguration);
							}
							return true;
						}
						n++;
					}
				}
				if (player.hasPermission("ragemode.vip") && hasRoomForVIP(game)) {
					Random random = new Random();
					boolean isVIP = false;
					Player playerToKick;
					
					do {
						kickposition = random.nextInt(GetGames.getMaxPlayers(game,
								fileConfiguration) - 1);
						kickposition = kickposition + 1 + i;
						n = 0;
						playerToKick = Bukkit.getPlayer(UUID
								.fromString(list[kickposition]));	
						isVIP = playerToKick.hasPermission("rm.vip");
					} while (isVIP);
						
					while (n < oldLocations.getFirstLength()) {						//Get him back to his old location.
						if (oldLocations.getFromFirstObject(n) == playerToKick) {
							playerToKick.teleport(oldLocations
									.getFromSecondObject(n));
							oldLocations.removeFromBoth(n);
						}
						n++;
					}
					
					n = 0;
					
					while (n < oldInventories.getFirstLength()) {						//Give him his inventory back.
						if (oldInventories.getFromFirstObject(n) == playerToKick) {
							playerToKick.getInventory().clear();
							playerToKick.getInventory().setContents(oldInventories.getFromSecondObject(n));
							oldInventories.removeFromBoth(n);
						}
						n++;
					}
					
					n = 0;
					
					while (n < oldArmor.getFirstLength()) {							//Give him his armor back.
						if (oldArmor.getFromFirstObject(n) == playerToKick) {
							playerToKick.getInventory().setArmorContents(oldArmor.getFromSecondObject(n));
							oldArmor.removeFromBoth(n);
						}
						n++;
					}
					
					n = 0;
					
					while (n < oldHealth.getFirstLength()) {							//Give him his health back.
						if (oldHealth.getFromFirstObject(n) == playerToKick) {
							playerToKick.setHealth(oldHealth.getFromSecondObject(n));
							oldHealth.removeFromBoth(n);
						}
						n++;
					}
					
					n = 0;
					
					while (n < oldHunger.getFirstLength()) {							//Give him his hunger back.
						if (oldHunger.getFromFirstObject(n) == playerToKick) {
							playerToKick.setFoodLevel(oldHunger.getFromSecondObject(n));
							oldHunger.removeFromBoth(n);
						}
						n++;
					}
					
					n = 0;
					
					while (n < oldGameMode.getFirstLength()) {							//Give him his gamemode back.
						if (oldGameMode.getFromFirstObject(n) == playerToKick) {
							playerToKick.setGameMode(oldGameMode.getFromSecondObject(n));
							oldGameMode.removeFromBoth(n);
						}
						n++;
					}
					
					list[kickposition] = player.getUniqueId().toString();
					playerToKick
							.sendMessage(ConstantHolder.RAGEMODE_PREFIX + "You were kicked out of the Game to make room for a VIP.");

					if (getPlayersInGame(game).length == 2) {
						new LobbyTimer(game, fileConfiguration);
					}
					player.sendMessage(ConstantHolder.RAGEMODE_PREFIX + "You joined " + ChatColor.DARK_AQUA + game + ChatColor.WHITE + ".");
					return true;
				} else {
					player.sendMessage(ConstantHolder.RAGEMODE_PREFIX + "This Game is already full!");
					return false;
				}

			}
			i = i + playersPerGame;
		}

		player.sendMessage(ConstantHolder.RAGEMODE_PREFIX + "The game you wish to join wasn't found.");
		return false;
	}

	public static boolean removePlayer(Player player) {
		int i = 0;
		int n = 0;
		int imax = GetGames.getConfigGamesCount(fileConfiguration)
				* (GetGames.getOverallMaxPlayers(fileConfiguration) + 1);

		while (i < imax) {
			if(list[i] != null) {
				if (list[i].equals(player.getUniqueId().toString())) {
//					TabGuiUpdater.removeTabForPlayer(player);
					
//					org.mcsg.double0negative.tabapi.TabAPI.disableTabForPlayer(player);
//					org.mcsg.double0negative.tabapi.TabAPI.updatePlayer(player);
					
					RageScores.removePointsForPlayers(new String[] {player.getUniqueId().toString()});
					
					player.getInventory().clear();
					player.sendMessage(ConstantHolder.RAGEMODE_PREFIX + "You left your current Game.");

					while (n < oldLocations.getFirstLength()) {					//Bring him back to his old location
						if (oldLocations.getFromFirstObject(n) == player) {
							player.teleport(oldLocations.getFromSecondObject(n));
							oldLocations.removeFromBoth(n);
						}
						n++;
					}
					
					n = 0;

					while (n < oldInventories.getFirstLength()) {					//Give him his inventory back
						if (oldInventories.getFromFirstObject(n) == player) {
							player.getInventory().clear();							
							player.getInventory().setContents(oldInventories.getFromSecondObject(n));
							oldInventories.removeFromBoth(n);
						}
						n++;
					}	
					
					n = 0;
					
					while (n < oldArmor.getFirstLength()) {
						if (oldArmor.getFromFirstObject(n) == player) {					//Give him his armor back
							player.getInventory().setArmorContents(oldArmor.getFromSecondObject(n));
							oldArmor.removeFromBoth(n);
						}
						n++;
					}
					
					n = 0;
					
					while (n < oldHealth.getFirstLength()) {							//Give him his health back.
						if (oldHealth.getFromFirstObject(n) == player) {
							player.setHealth(oldHealth.getFromSecondObject(n));
							oldHealth.removeFromBoth(n);
						}
						n++;
					}
					
					n = 0;
					
					while (n < oldHunger.getFirstLength()) {							//Give him his hunger back.
						if (oldHunger.getFromFirstObject(n) == player) {
							player.setFoodLevel(oldHunger.getFromSecondObject(n));
							oldHunger.removeFromBoth(n);
						}
						n++;
					}
					
					n = 0;
					
					while (n < oldGameMode.getFirstLength()) {							//Give him his gamemode back.
						if (oldGameMode.getFromFirstObject(n) == player) {
							player.setGameMode(oldGameMode.getFromSecondObject(n));
							oldGameMode.removeFromBoth(n);
						}
						n++;
					}
					
					
					list[i] = null;
					return true;
				}
			}
			i++;
		}
		player.sendMessage(ConstantHolder.RAGEMODE_PREFIX + "The fact that you are not in a game caused a Problem while trying to remove you from that game.");
		return false;
	}

	public static boolean isGameRunning(String game) {
		int i = 0;
		int imax = runningGames.length;
		while (i < imax) {
			if (runningGames[i] != null) {
				if (runningGames[i].equals(game)) {
					return true;
				}
			}
			i++;
		}
		return false;
	}

	public static boolean setGameRunning(String game) {
		if (!GetGames.isGameExistent(game, fileConfiguration))
			return false;
		int i = 0;
		int imax = runningGames.length;
		while (i < imax) {
			if(runningGames[i] != null) {
				if (runningGames[i].equals(game))
					return false;	
			}
			i++;
		}
		i = 0;
		while (i < imax) {
			if (runningGames[i] == null) {
				runningGames[i] = game;
			}
			i++;
		}
		return false;
	}
	
	public static boolean setGameNotRunning(String game) {
		if (!GetGames.isGameExistent(game, fileConfiguration))
			return false;
		
		int i = 0;
		int imax = GetGames.getConfigGamesCount(fileConfiguration);
		
		while (i < imax) {    //TODO Fix ArrayIndexOutOfBoundsException if there are many games and just one is running
			if (runningGames[i] != null) {
				if (runningGames[i].equals(game)) {
					runningGames[i] = null;
					return true;			
				}				
			}
			i++;
		}
		return false;				
	}

	public static boolean isPlayerPlaying(String player) {
		if(player != null) {
			int i = 0;
			int imax = list.length;
			
			while(i < imax) {
				if(list[i] != null) {
					if(list[i].equals(player)) {
						return true;
					}				
				}
				i++;			
			}		
		}

		return false;
	}
	
	public static boolean hasRoomForVIP(String game) {
		String[] players = getPlayersInGame(game);
		int i = 0;
		int imax = players.length;
		int vipsInGame = 0;
		
		while(i < imax) {
			if(players[i] != null) {
				if(Bukkit.getPlayer(UUID.fromString(players[i])).hasPermission("rm.vip")) {
					vipsInGame++;
				}
			}
			i++;
		}
		
		if(vipsInGame == players.length) {
			return false;
		}
	return true;
	}
	
	public static void addGameToList(String game, int maxPlayers) {
		if(GetGames.getOverallMaxPlayers(fileConfiguration) < maxPlayers) {
				String[] oldList = list;
				list = Arrays.copyOf(list, (GetGames.getConfigGamesCount(fileConfiguration) + 1) * (maxPlayers + 1));
				int i = 0;
				int imax = oldList.length;
				int n = 0;
				int nmax = (GetGames.getOverallMaxPlayers(fileConfiguration) + 1);
				
				while(i < imax) {
					while(n < nmax + i) {
						list[n + i] = oldList[n + i];
						n++;
					}
					i = i + maxPlayers + 1;
					n = i;
				}
				
				list[i] = game;
		}
		else {
			String[] oldList = list;
			list = Arrays.copyOf(list, (GetGames.getConfigGamesCount(fileConfiguration) + 1) * (GetGames.getOverallMaxPlayers(fileConfiguration) + 1));
			int i = 0;
			int imax = oldList.length;
			
			while(i < imax) {
				list[i] = oldList[i];
				i++;
			}
			
			list[i] = game;
		}
		
		String[] oldRunningGames = runningGames;
		runningGames = Arrays.copyOf(runningGames, (runningGames.length + 1));
		int i = 0;
		int imax = runningGames.length - 1;
		
		while(i < imax) {
			runningGames[i] = oldRunningGames[i];
			i++;
		}	
	}
	
	public static void deleteGameFromList(String game) {
		String[] playersInGame = getPlayersInGame(game);
		if(playersInGame != null) {
			int i = 0;
			int imax = playersInGame.length;
			while(i < imax) {
				if(playersInGame[i] != null)
					removePlayer(Bukkit.getPlayer(UUID.fromString(playersInGame[i])));
				i++;
			}	
		}
		int i = 0;
		int imax = list.length;
		int gamePos = imax;
		int nextGamePos = imax;
		
		while(i < imax) {
			if(list[i] != null) {
				if(list[i].equals(game)) {
					gamePos = i;
					int n = 0;
					int nmax = GetGames.getOverallMaxPlayers(fileConfiguration) + 1;
					
					while(n < nmax) {
						list[n + i] = null;
						n++;
					}
					nextGamePos = i + nmax;
				}
			}
			i++;
		}
		i = nextGamePos;
		
		while(i < imax) {
			list[gamePos] = list[i];
			list[i] = null;
			i++;
			gamePos++;
		}
		String[] oldList = new String[(GetGames.getConfigGamesCount(fileConfiguration) - 1) * (GetGames.getOverallMaxPlayers(fileConfiguration) + 1)];
		int g = 0;
		int gmax = oldList.length;
		
		while(g < gmax) {
			oldList[g] = list[g];
			g++;
		}
		
		list = Arrays.copyOf(list, oldList.length);
		
		g = 0;
		
		while(g < gmax) {
			list[g] = oldList[g];
			g++;
		}
	}
	
	public static String getPlayersGame(Player player) {
		String sPlayer = player.getUniqueId().toString();
		String game = null;
		
		int i = 0;
		int imax = list.length;
		int playersPerGame = GetGames.getOverallMaxPlayers(fileConfiguration);
		
		while(i < imax) {
			if(list[i] != null) {
				if((i % (playersPerGame + 1)) == 0) {
					game = list[i];
				}
				if(list[i].equals(sPlayer)) {
					return game;
				}
			}
		i++;
		}
		return null;
	}
}
