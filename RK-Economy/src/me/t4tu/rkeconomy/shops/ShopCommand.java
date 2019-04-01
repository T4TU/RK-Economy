package me.t4tu.rkeconomy.shops;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkcore.utils.ReflectionUtils;
import me.t4tu.rkeconomy.Economy;
import net.md_5.bungee.api.ChatMessageType;

public class ShopCommand implements CommandExecutor {
	
	private Economy economy;
	
	public ShopCommand(Economy economy) {
		this.economy = economy;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		
		String usage = CoreUtils.getUsageString();
		String noPermission = CoreUtils.getNoPermissionString();
		String playersOnly = CoreUtils.getPlayersOnlyString();
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(tc3 + playersOnly);
			return true;
		}
		Player p = (Player) sender;
		
		if (cmd.getName().equalsIgnoreCase("shop")) {
			if (CoreUtils.hasRank(p, "ylläpitäjä")) {
				if (args.length >= 1) {
					if (args[0].equalsIgnoreCase("list")) {
						p.sendMessage("");
						p.sendMessage(tc2 + "§m----------" + tc1 + " Kaupat " + tc2 + "§m----------");
						p.sendMessage("");
						if (economy.getShopManager().getShops().isEmpty()) {
							p.sendMessage(tc3 + " Ei kauppoja!");
						}
						else {
							for (Shop shop : economy.getShopManager().getShops()) {
								ReflectionUtils.sendChatPacket(p, "[\"\",{\"text\":\"" + tc2 + " - \"},{\"text\":\"" + tc1 + "#" + shop.getId() + ": '" + shop.getName() + 
										"'\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/shop edit " + shop.getId() + 
										"\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"§aMuokkaa kauppaa '" + shop.getName() + 
										"'\"}]}}}]", ChatMessageType.CHAT);
							}
						}
						p.sendMessage("");
					}
					else if (args[0].equalsIgnoreCase("add")) {
						if (args.length >= 5) {
							try {
								int id = Integer.parseInt(args[1]);
								int rows = Integer.parseInt(args[3]);
								String name = args[2].replace("_", " ");
								String trigger = args[4];
								if (economy.getShopManager().getShopById(id) == null) {
									economy.getShopManager().newShop(id, name, rows, trigger);
									p.sendMessage(tc2 + "Lisättiin kauppa ID:llä #" + id + "!");
								}
								else {
									p.sendMessage(tc3 + "Tällä ID:llä on jo kauppa!");
								}
							}
							catch (NumberFormatException e) {
								p.sendMessage(tc3 + "Virheellinen ID!");
							}
						}
						else {
							p.sendMessage(usage + "/shop add <id> <nimi> <rivien määrä> <NPC:n nimi>");
						}
					}
					else if (args[0].equalsIgnoreCase("remove")) {
						if (args.length >= 2) {
							try {
								int id = Integer.parseInt(args[1]);
								if (economy.getShopManager().getShopById(id) != null) {
									economy.getShopManager().removeShop(economy.getShopManager().getShopById(id));
									p.sendMessage(tc2 + "Poistettiin kauppa ID:llä #" + id + "!");
								}
								else {
									p.sendMessage(tc3 + "Ei löydetty kauppaa antamallasi ID:llä!");
								}
							}
							catch (NumberFormatException e) {
								p.sendMessage(tc3 + "Virheellinen ID!");
							}
						}
						else {
							p.sendMessage(usage + "/shop remove <id>");
						}
					}
					else if (args[0].equalsIgnoreCase("edit")) {
						if (args.length >= 2) {
							try {
								int id = Integer.parseInt(args[1]);
								if (economy.getShopManager().getShopById(id) != null) {
									economy.getShopManager().getShopById(id).edit(p);
								}
								else {
									p.sendMessage(tc3 + "Ei löydetty kauppaa antamallasi ID:llä!");
								}
							}
							catch (NumberFormatException e) {
								p.sendMessage(tc3 + "Virheellinen ID!");
							}
						}
						else {
							p.sendMessage(usage + "/shop edit <id>");
						}
					}
					else if (args[0].equalsIgnoreCase("save")) {
						if (args.length >= 2) {
							try {
								int id = Integer.parseInt(args[1]);
								if (economy.getShopManager().getShopById(id) != null) {
									economy.getShopManager().getShopById(id).save();
									p.sendMessage(tc2 + "Tallennettiin kauppa ID:llä #" + id + "!");
								}
								else {
									p.sendMessage(tc3 + "Ei löydetty kauppaa antamallasi ID:llä!");
								}
							}
							catch (NumberFormatException e) {
								p.sendMessage(tc3 + "Virheellinen ID!");
							}
						}
						else {
							p.sendMessage(usage + "/shop save <id>");
						}
					}
					else if (args[0].equalsIgnoreCase("price")) {
						if (args.length >= 3) {
							try {
								int id = Integer.parseInt(args[1]);
								int slot = Integer.parseInt(args[2]);
								double price = Double.parseDouble(args[3]);
								if (economy.getShopManager().getShopById(id) != null) {
									if (slot >= 0 && slot < economy.getShopManager().getShopById(id).getSize()) {
										economy.getConfig().set("shops." + id + ".prices." + slot, price);
										economy.saveConfig();
										p.sendMessage(tc2 + "Asetettiin kaupan #" + id + " slotin " + slot + " hinnaksi " + price + "£!");
										economy.getShopManager().getShopById(id).save();
									}
									else {
										p.sendMessage(tc3 + "Tuntematon slotti!");
									}
								}
								else {
									p.sendMessage(tc3 + "Ei löydetty kauppaa antamallasi ID:llä!");
								}
							}
							catch (NumberFormatException e) {
								p.sendMessage(tc3 + "Virheelliset argumentit!");
							}
						}
						else {
							p.sendMessage(usage + "/shop price <id> <slot> <hinta>");
						}
					}
					else if (args[0].equalsIgnoreCase("reload")) {
						economy.reloadConfig();
						economy.getShopManager().loadShopsFromConfig();
						p.sendMessage(tc2 + "Ladattiin kaupat uudestaan!");
					}
					else {
						p.sendMessage(usage + "/shop list/add/remove/edit/save/price/reload");
					}
				}
				else {
					p.sendMessage(usage + "/shop list/add/remove/edit/save/price/reload");
				}
			}
			else {
				p.sendMessage(noPermission);
			}
		}
		return true;
	}
}