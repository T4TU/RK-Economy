package me.t4tu.rkeconomy;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkeconomy.shops.Shop;

public class EconomyCommand implements CommandExecutor {
	
	private Economy economy;
	
	public EconomyCommand(Economy economy) {
		this.economy = economy;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		
		String usage = CoreUtils.getUsageString();
		String noPermission = CoreUtils.getNoPermissionString();
		String playersOnly = CoreUtils.getPlayersOnlyString();
		
		if (cmd.getName().equalsIgnoreCase("rahat") || cmd.getName().equalsIgnoreCase("raha") || cmd.getName().equalsIgnoreCase("money") || 
				cmd.getName().equalsIgnoreCase("balance") || cmd.getName().equalsIgnoreCase("bal")) {
			if (args.length >= 2 && CoreUtils.hasRank(sender, "ylläpitäjä")) {
				if (args.length >= 3) {
					new BukkitRunnable() {
						public void run() {
							try {
								int amount = Economy.moneyAsInt(Double.parseDouble(args[2].replace(",", ".")));
								if (Economy.hasAccount(args[0])) {
									if (args[1].equalsIgnoreCase("set")) {
										String sign = "+";
										int difference = amount - Economy.getMoney(args[0]);
										if (difference < 0) {
											sign = "";
										}
										Economy.setMoney(args[0], amount);
										sender.sendMessage(tc2 + "Asetettiin pelaajan " + tc1 + args[0] + tc2 + " rahamääräksi " + tc1 + Economy.moneyAsString(amount) + 
												tc2 + "! (Muutos " + tc1 + sign + Economy.moneyAsString(difference) + tc2 + ")");
										Economy.setStateMoney(Economy.getStateMoney() - difference);
									}
									else if (args[1].equalsIgnoreCase("give")) {
										int money = Economy.getMoney(args[0]);
										Economy.setMoney(args[0], money + amount);
										sender.sendMessage(tc2 + "Lisättiin pelaajan " + tc1 + args[0] + tc2 + " tilille " + tc1 + Economy.moneyAsString(amount) + tc2 + "!"
												+ " (Nyt yhteensä " + tc1 + Economy.moneyAsString(money + amount) + tc2 + ")");
										Economy.setStateMoney(Economy.getStateMoney() - amount);
									}
									else if (args[1].equalsIgnoreCase("take")) {
										int money = Economy.getMoney(args[0]);
										Economy.setMoney(args[0], money - amount);
										sender.sendMessage(tc2 + "Otettiin pelaajan " + tc1 + args[0] + tc2 + " tililtä " + tc1 + Economy.moneyAsString(amount) + tc2 + "!"
												+ " (Nyt yhteensä " + tc1 + Economy.moneyAsString(money - amount) + tc2 + ")");
										Economy.setStateMoney(Economy.getStateMoney() + amount);
									}
									else {
										sender.sendMessage(usage + "/money <pelaaja> [set/give/take <määrä>]");
									}
								}
								else {
									sender.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
								}
							}
							catch (NumberFormatException e) {
								sender.sendMessage(tc3 + "Virheellinen rahamäärä!");
							}
						}
					}.runTaskAsynchronously(economy);
				}
				else {
					sender.sendMessage(usage + "/money <pelaaja> [set/give/take <määrä>]");
				}
			}
			else if (args.length >= 1 && CoreUtils.hasRank(sender, "ylläpitäjä")) {
				new BukkitRunnable() {
					public void run() {
						if (Economy.hasAccount(args[0])) {
							sender.sendMessage(tc2 + "Pelaajan " + tc1 + args[0] + tc2 + " tilillä on rahaa yhteensä " + tc1 + Economy.moneyAsString(Economy.getMoney(args[0])) + tc2 + "!");
						}
						else {
							sender.sendMessage(tc3 + "Ei löydetty pelaajaa antamallasi nimellä!");
						}
					}
				}.runTaskAsynchronously(economy);
			}
			else {
				if (sender instanceof Player) {
					Player player = (Player) sender;
					player.sendMessage("");
					player.sendMessage(tc2 + "§m----------" + tc1 + " Rahat " + tc2 + "§m----------");
					player.sendMessage("");
					player.sendMessage(tc2 + " Tavaraluettelossasi on " + tc1 + Economy.moneyAsString(economy.getGoldCoins(player) * 10 + economy.getSilverCoins(player)) + tc2 + " arvosta käteistä.");
					player.sendMessage(tc2 + " Pankkitililläsi on rahaa yhteensä " + tc1 + Economy.moneyAsString(Economy.getMoney(player)) + tc2 + ".");
					player.sendMessage("");
				}
				else {
					sender.sendMessage(playersOnly);
				}
			}
			return true;
		}
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(tc3 + playersOnly);
			return true;
		}
		Player player = (Player) sender;
		
		if (cmd.getName().equalsIgnoreCase("kultakolikko")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				int amount = 1;
				if (args.length >= 1) {
					try {
						amount = Integer.parseInt(args[0]);
						if (amount < 0) {
							player.sendMessage(tc3 + "Virheellinen kolikkojen määrä!");
							return true;
						}
					}
					catch (NumberFormatException e) {
						player.sendMessage(tc3 + "Virheellinen kolikkojen määrä!");
						return true;
					}
				}
				player.getInventory().addItem(economy.getGoldCoin(amount));
				player.sendMessage(tc2 + "Annettiin " + tc1 + amount + " kultakolikkoa" + tc2 + "!");
				Economy.setStateMoney(Economy.getStateMoney() - amount * 10);
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("hopeakolikko")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				int amount = 1;
				if (args.length >= 1) {
					try {
						amount = Integer.parseInt(args[0]);
						if (amount < 0) {
							player.sendMessage(tc3 + "Virheellinen kolikkojen määrä!");
							return true;
						}
					}
					catch (NumberFormatException e) {
						player.sendMessage(tc3 + "Virheellinen kolikkojen määrä!");
						return true;
					}
				}
				player.getInventory().addItem(economy.getSilverCoin(amount));
				player.sendMessage(tc2 + "Annettiin " + tc1 + amount + " hopeakolikkoa" + tc2 + "!");
				Economy.setStateMoney(Economy.getStateMoney() - amount);
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("shekki") || cmd.getName().equalsIgnoreCase("sekki")) {
			if (args.length >= 2) {
				ItemStack i = player.getInventory().getItemInMainHand();
				if (CoreUtils.isNotAir(i) && i.getType() == Material.PAPER) {
					if (i.getAmount() == 1) {
						if (!i.hasItemMeta()) {
							new BukkitRunnable() {
								public void run() {
									String receiver = CoreUtils.nameToName(args[0]);
									if (receiver != null) {
										try {
											int amount = Economy.moneyAsInt(Double.parseDouble(args[1].replace(",", ".")));
											if (amount >= 10 && amount <= 100000) {
												ItemMeta m = i.getItemMeta();
												m.setDisplayName("§fShekki");
												m.setLore(Arrays.asList("§8Summa: §o" + Economy.moneyAsString(amount), "§8Saaja: §o" + receiver, "§8Allekirjoitus: §o" + player.getName()));
												i.setItemMeta(m);
												player.updateInventory();
												player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
												player.sendMessage(tc2 + "Kirjoitit shekin, jonka summa on " + tc1 + Economy.moneyAsString(amount) + tc2 + ", pelaajalle " + tc1 + receiver + tc2 + "!");
											}
											else {
												player.sendMessage(tc3 + "Rahamäärän täytyy olla välillä 1 - 10 000!");
											}
										}
										catch (NumberFormatException e) {
											player.sendMessage(tc3 + "Rahamäärän täytyy olla välillä 1 - 10 000!");
										}
									}
									else {
										player.sendMessage(tc3 + "Tuon nimistä pelaajaa ei ole olemassa!");
									}
								}
							}.runTaskAsynchronously(economy);
						}
						else {
							player.sendMessage(tc3 + "Voit kirjoittaa shekin vain puhtaalle paperille!");
						}
					}
					else {
						player.sendMessage(tc3 + "Voit kirjoittaa vain yhden shekin kerrallaan!");
					}
				}
				else {
					player.sendMessage(tc3 + "Voidaksesi kirjoittaa shekin sinulla täytyy olla paperinpala kädessäsi!");
				}
			}
			else {
				player.sendMessage(usage + "/shekki <saaja> <määrä>");
			}
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("hintamuutos")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				if (args.length >= 2) {
					try {
						int category = Integer.parseInt(args[0]);
						double multiplier = Double.parseDouble(args[1]);
						if (category >= 1 && category <= 6) {
							economy.getConfig().set("multipliers." + category, multiplier);
							economy.saveConfig();
							for (Shop shop : economy.getShopManager().getShops()) {
								shop.save();
							}
							player.sendMessage(tc2 + "Asetettiin hintakategorian " + tc1 + category + tc2 + " kertoimeksi " + tc1 + multiplier + tc2 + "!");
						}
						else {
							player.sendMessage(tc3 + "Virheellinen kategoria tai kerroin!");
						}
					}
					catch (NumberFormatException e) {
						player.sendMessage(tc3 + "Virheellinen kategoria tai kerroin!");
					}
				}
				else {
					double multiplier1 = economy.getConfig().getDouble("multipliers.1");
					double multiplier2 = economy.getConfig().getDouble("multipliers.2");
					double multiplier3 = economy.getConfig().getDouble("multipliers.3");
					double multiplier4 = economy.getConfig().getDouble("multipliers.4");
					double multiplier5 = economy.getConfig().getDouble("multipliers.5");
					double multiplier6 = economy.getConfig().getDouble("multipliers.6");
					player.sendMessage("");
					player.sendMessage(tc2 + "§m----------" + tc1 + " Hintakertoimet " + tc2 + "§m----------");
					player.sendMessage("");
					player.sendMessage(tc1 + " Kategoria #1: " + tc2 + "<5кк kerroin " + multiplier1);
					player.sendMessage(tc1 + " Kategoria #2: " + tc2 + "5-20кк kerroin " + multiplier2);
					player.sendMessage(tc1 + " Kategoria #3: " + tc2 + "20-60кк kerroin " + multiplier3);
					player.sendMessage(tc1 + " Kategoria #4: " + tc2 + "60-115кк kerroin " + multiplier4);
					player.sendMessage(tc1 + " Kategoria #5: " + tc2 + "115-505кк kerroin " + multiplier5);
					player.sendMessage(tc1 + " Kategoria #6: " + tc2 + ">505кк kerroin " + multiplier6);
					player.sendMessage("");
					player.sendMessage(tc2 + " Muuta kertoimia: " + tc1 + "/hintamuutos <kategoria> <kerroin>");
				}
			}
			else {
				player.sendMessage(noPermission);
			}
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("valtiontili")) {
			if (CoreUtils.hasRank(player, "ylläpitäjä")) {
				player.sendMessage(tc2 + "Valtion tilillä on rahaa: " + tc1 + Economy.moneyAsString(Economy.getStateMoney()));
			}
			else {
				player.sendMessage(noPermission);
			}
		}
		return true;
	}
}