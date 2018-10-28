package me.t4tu.rkeconomy.markets;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.t4tu.rkcore.inventories.InventoryGUI;
import me.t4tu.rkcore.inventories.InventoryGUIAction;
import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkeconomy.Economy;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class MarketCommand implements CommandExecutor {
	
	private Economy economy;
	
	public MarketCommand(Economy economy) {
		this.economy = economy;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		String tc4 = CoreUtils.getErrorHighlightColor();
		
		String usage = CoreUtils.getUsageString();
		String playersOnly = CoreUtils.getPlayersOnlyString();
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(tc3 + playersOnly);
			return true;
		}
		Player player = (Player) sender;
		
		if (cmd.getName().equalsIgnoreCase("kauppa") || cmd.getName().equalsIgnoreCase("kaupat") || cmd.getName().equalsIgnoreCase("torikoju") || cmd.getName().equalsIgnoreCase("torikojut") || 
				cmd.getName().equalsIgnoreCase("tori") || cmd.getName().equalsIgnoreCase("liikekiinteistö") || cmd.getName().equalsIgnoreCase("liikekiinteistöt")) {
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("info") && CoreUtils.hasRank(player, "ylläpitäjä")) {
					if (args.length >= 2) {
						try {
							int id = Integer.parseInt(args[1]);
							MarketStall marketStall = economy.getMarketManager().getMarketStall(id);
							if (marketStall != null) {
								new BukkitRunnable() {
									public void run() {
										Long l = marketStall.getExpires() - System.currentTimeMillis();
										player.sendMessage("");
										player.sendMessage(tc2 + "§m----------" + tc1 + " " + marketStall.getType().getFriendlyName() + " " + tc2 + "§m----------");
										player.sendMessage("");
										player.sendMessage(tc2 + " ID: " + tc1 + id);
										player.sendMessage(tc2 + " Tyyppi: " + tc1 + marketStall.getType().getFriendlyName());
										player.sendMessage(tc2 + " Myyntipisteitä: " + tc1 + marketStall.getSellingPoints().size());
										if (marketStall.isRented()) {
											String ownerName = marketStall.getOwnerName();
											player.sendMessage(tc2 + " Vuokrattu: §aKyllä");
											player.sendMessage(tc2 + " Vuokraaja: " + tc1 + ownerName);
											if (marketStall.getType() == MarketStallType.LIIKEKIINTEISTÖ) {
												if (marketStall.getName() != null) {
													player.sendMessage(tc2 + " Nimi: " + tc1 + ChatColor.translateAlternateColorCodes('&', marketStall.getName()));
												}
												else {
													player.sendMessage(tc2 + " Nimi: §cEi nimeä");
												}
											}
											player.sendMessage(tc2 + " Rahaa tuotettu: " + tc1 + marketStall.getProfit() + "£");
											player.sendMessage(tc2 + " Vuokra-aikaa jäljellä: " + tc1 + CoreUtils.getDaysAndHoursAndMinsFromMillis(l));
											player.sendMessage("");
										}
										else {
											player.sendMessage(tc2 + " Vuokrattu: §cEi");
											player.sendMessage("");
										}
									}
								}.runTaskAsynchronously(economy);
							}
							else {
								player.sendMessage(tc3 + "Ei löydetty torikojua/liikekiinteistöä antamallasi ID:llä!");
							}
						}
						catch (NumberFormatException e) {
							player.sendMessage(tc3 + "Virheellinen ID!");
						}
					}
					else {
						player.sendMessage(usage + "/kauppa info <ID>");
					}
				}
				else if ((args[0].equalsIgnoreCase("aseta") || args[0].equalsIgnoreCase("add")) && CoreUtils.hasRank(player, "ylläpitäjä")) {
					if (args.length >= 3) {
						try {
							int id = Integer.parseInt(args[1]);
							if (economy.getMarketManager().getMarketStall(id) == null) {
								List<ItemFrame> frames = new ArrayList<ItemFrame>();
								for (Entity entity : player.getNearbyEntities(20, 20, 20)) {
									if (entity instanceof ItemFrame) {
										ItemFrame frame = (ItemFrame) entity;
										if (CoreUtils.hasDisplayName(frame.getItem()) && frame.getItem().getType() == Material.NAME_TAG) {
											if (CoreUtils.getDisplayName(frame.getItem()).equals(id + "")) {
												frames.add(frame);
											}
										}
									}
								}
								if (!frames.isEmpty()) {
									Location signLocation = null;
									Block block = player.getTargetBlock(null, 20);
									if (block != null && block.getState() instanceof Sign) {
										signLocation = block.getLocation();
									}
									try {
										MarketStallType type = MarketStallType.valueOf(args[2].toUpperCase());
										economy.getMarketManager().newMarketStall(id, type, signLocation, frames);
										player.sendMessage(tc2 + "Luotiin uusi " + tc1 + type.getFriendlyName().toLowerCase() + tc2 + " ID:llä " + tc1 + "#" + id + tc2 + 
												", ja lisättiin siihen " + tc1 + frames.size() + tc2 + " myyntipistettä!");
									}
									catch (IllegalArgumentException e) {
										player.sendMessage(tc3 + "Virheellinen kaupan tyyppi!");
									}
								}
								else {
									player.sendMessage(tc3 + "Ei löydetty lisättäviä myyntipisteitä 20 metrin säteellä!");
								}
							}
							else {
								player.sendMessage(tc3 + "Torikoju/liikekiinteistö tällä ID:llä on jo olemassa!");
							}
						}
						catch (NumberFormatException e) {
							player.sendMessage(tc3 + "Virheellinen ID!");
						}
					}
					else {
						player.sendMessage(usage + "/kauppa aseta <ID> <tyyppi>");
					}
				}
				else if ((args[0].equalsIgnoreCase("poista") || args[0].equalsIgnoreCase("remove")) && CoreUtils.hasRank(player, "ylläpitäjä")) {
					if (args.length >= 2) {
						try {
							int id = Integer.parseInt(args[1]);
							MarketStall marketStall = economy.getMarketManager().getMarketStall(id);
							if (marketStall != null) {
								if (!marketStall.isRented() || (args.length >= 3 && args[2].equalsIgnoreCase("confirm"))) {
									marketStall.remove();
									player.sendMessage(tc2 + "Poistettiin " + tc1 + marketStall.getType().getFriendlyName().toLowerCase() + tc2 + " ID:llä " + tc1 + "#" + marketStall.getId() + tc2 + "!");
								}
								else {
									player.sendMessage(tc3 + "Tämä " + tc4 + marketStall.getType().getFriendlyName().toLowerCase() + tc3 + " on vuokrattuna jollekin! Vahvista poistaminen komennolla " + tc4 + 
											"/kauppa poista " + id + " confirm" + tc3 + ".");
								}
							}
							else {
								player.sendMessage(tc3 + "Ei löydetty torikojua/liikekiinteistöä antamallasi ID:llä!");
							}
						}
						catch (NumberFormatException e) {
							player.sendMessage(tc3 + "Virheellinen ID!");
						}
					}
					else {
						player.sendMessage(usage + "/kauppa poista <ID>");
					}
				}
				else if ((args[0].equalsIgnoreCase("työkalu") || args[0].equalsIgnoreCase("tool")) && CoreUtils.hasRank(player, "ylläpitäjä")) {
					if (args.length >= 2) {
						try {
							int id = Integer.parseInt(args[1]);
							player.getInventory().addItem(CoreUtils.getItem(Material.NAME_TAG, id + "", null, 1));
							player.sendMessage(tc2 + "Annettiin työkalu ID:llä " + tc1 + "#" + id + tc2 + "!");
						}
						catch (NumberFormatException e) {
							player.sendMessage(tc3 + "Virheellinen ID!");
						}
					}
					else {
						player.sendMessage(usage + "/kauppa poista <ID>");
					}
				}
				else if ((args[0].equalsIgnoreCase("päivitä") || args[0].equalsIgnoreCase("update")) && CoreUtils.hasRank(player, "ylläpitäjä")) {
					if (args.length >= 2) {
						try {
							int id = Integer.parseInt(args[1]);
							MarketStall marketStall = economy.getMarketManager().getMarketStall(id);
							if (marketStall != null) {
								marketStall.updateSign();
								player.sendMessage(tc2 + "Päivitettiin kyltti!");
							}
							else {
								player.sendMessage(tc3 + "Ei löydetty torikojua/liikekiinteistöä antamallasi ID:llä!");
							}
						}
						catch (NumberFormatException e) {
							player.sendMessage(tc3 + "Virheellinen ID!");
						}
					}
					else {
						player.sendMessage(usage + "/kauppa poista <ID>");
					}
				}
				else if (args[0].equalsIgnoreCase("reload") && CoreUtils.hasRank(player, "ylläpitäjä")) {
					economy.reloadConfig();
					economy.getMarketManager().loadMarketStallsFromConfig();
					player.sendMessage(tc2 + "Ladattiin torikojut ja liikekiinteistöt uudelleen!");
				}
				else if (args[0].equalsIgnoreCase("tiedot")) {
					if (args.length >= 2) {
						try {
							int id = Integer.parseInt(args[1]);
							MarketStall marketStall = economy.getMarketManager().getMarketStall(id);
							if (marketStall != null) {
								if (marketStall.isRented() && marketStall.getOwnerUuid().equals(player.getUniqueId().toString())) {
									InventoryGUI gui = new InventoryGUI(45, marketStall.getType().getFriendlyName());
									List<String> lore = Arrays.asList("", "§aOmistaja: §7" + player.getName(), "§aMyyntipisteitä: §7" + marketStall.getSellingPoints().size() + " kpl", 
											"§aRahaa tuotettu: §7" + marketStall.getProfit() + "£", "§aAikaa jäljellä: §7" + 
											CoreUtils.getDaysAndHoursAndMinsFromMillis(marketStall.getExpires() - System.currentTimeMillis()));
									if (marketStall.getType() == MarketStallType.LIIKEKIINTEISTÖ) {
										String name = "§cEi nimeä";
										if (marketStall.getName() != null) {
											name = ChatColor.translateAlternateColorCodes('&', marketStall.getName());
										}
										lore = Arrays.asList("", "§aOmistaja: §7" + player.getName(), "§aNimi: §7" + name, "§aMyyntipisteitä: §7" + marketStall.getSellingPoints().size() + " kpl", 
												"§aRahaa tuotettu: §7" + marketStall.getProfit() + "£", "§aAikaa jäljellä: §7" + 
												CoreUtils.getDaysAndHoursAndMinsFromMillis(marketStall.getExpires() - System.currentTimeMillis()));
									}
									gui.addItem(CoreUtils.getItem(Material.BOOK, "§a" + marketStall.getType().getFriendlyName(), lore, 1), 13, null);
									if (marketStall.getType() == MarketStallType.LIIKEKIINTEISTÖ) {
										gui.addItem(CoreUtils.getItem(Material.WRITABLE_BOOK, "§aMyyntihistoria", Arrays.asList("", "§7 » Näytä myyntihistoria klikkaamalla!"), 1), 29, new InventoryGUIAction() {
											public void onClickAsync() { }
											public void onClick() {
												gui.close(player);
												player.performCommand("kauppa myyntihistoria " + id);
											}
										});
										gui.addItem(CoreUtils.getItem(Material.SIGN, "§aNimeä", Arrays.asList("", "§7 » Nimeä liikekiinteistösi klikkaamalla!"), 1), 33, new InventoryGUIAction() {
											public void onClickAsync() { }
											public void onClick() {
												gui.close(player);
												player.performCommand("liikekiinteistö nimeä " + id);
											}
										});
									}
									else {
										gui.addItem(CoreUtils.getItem(Material.WRITABLE_BOOK, "§aMyyntihistoria", Arrays.asList("", "§7 » Näytä myyntihistoria klikkaamalla!"), 1), 31, new InventoryGUIAction() {
											public void onClickAsync() { }
											public void onClick() {
												gui.close(player);
												player.performCommand("kauppa myyntihistoria " + id);
											}
										});
									}
									gui.open(player);
								}
								else {
									player.sendMessage(tc3 + "Et ole vuokrannut tätä torikojua/liikekiinteistöä!");
								}
							}
							else {
								player.sendMessage(tc3 + "Ei löydetty torikojua/liikekiinteistöä antamallasi ID:llä!");
							}
						}
						catch (NumberFormatException e) {
							player.sendMessage(tc3 + "Virheellinen ID!");
						}
					}
					else {
						player.sendMessage(usage + "/kauppa tiedot <ID>");
					}
				}
				else if (args[0].equalsIgnoreCase("esikatsele")) {
					if (args.length >= 3) {
						try {
							int id = Integer.parseInt(args[1]);
							int number = Integer.parseInt(args[2]);
							MarketStall marketStall = economy.getMarketManager().getMarketStall(id);
							if (marketStall != null) {
								SellingPoint sellingPoint = marketStall.getSellingPoint(number);
								if (sellingPoint != null) {
									ItemStack preview = sellingPoint.getPreviewItemStack();
									if (preview != null) {
										Inventory inventory = Bukkit.createInventory(null, InventoryType.DISPENSER, "Tuotteen esikatselu");
										inventory.setItem(4, preview);
										player.openInventory(inventory);
									}
									else {
										player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
										player.sendMessage(tc3 + "Tämä tuote on loppuunmyyty!");
									}
								}
								else {
									player.sendMessage(tc3 + "Ei löydetty antamaasi myyntipistettä!");
								}
							}
							else {
								player.sendMessage(tc3 + "Ei löydetty torikojua/liikekiinteistöä antamallasi ID:llä!");
							}
						}
						catch (NumberFormatException e) {
							player.sendMessage(tc3 + "Virheellinen ID tai myyntipiste!");
						}
					}
					else {
						player.sendMessage(usage + "/kauppa esikatsele <ID> <myyntipiste>");
					}
				}
				else if (args[0].equalsIgnoreCase("myyntihistoria")) {
					if (args.length >= 3) {
						try {
							int id = Integer.parseInt(args[1]);
							int page = Integer.parseInt(args[2]);
							MarketStall marketStall = economy.getMarketManager().getMarketStall(id);
							if (marketStall != null) {
								if (marketStall.isRented() && marketStall.getOwnerUuid().equals(player.getUniqueId().toString())) {
									int pages = economy.round(marketStall.getHistory().size() / 6d, RoundingMode.UP);
									if (pages == 0) {
										pages = 1;
									}
									if (page > 0 && page <= pages) {
										player.sendMessage("");
										player.sendMessage(tc2 + "§m----------" + tc1 + " Myyntihistoria [" + page + "/" + pages + "]" + tc2 + "§m----------");
										player.sendMessage("");
										if (marketStall.getHistory().size() > 0) {
											for (int i = 0; i < 6; i++) {
												try {
													String s = marketStall.getHistory().get(i + (page - 1) * 6);
													player.sendMessage(tc1 + " - " + tc2 + s);
												}
												catch (IndexOutOfBoundsException e) {
												}
											}
											player.sendMessage("");
											TextComponent start = new TextComponent(" ");
											TextComponent space = new TextComponent("    ");
											TextComponent previous = new TextComponent("[ < Edellinen < ]");
											if (page > 1) {
												previous.setColor(ChatColor.GREEN);
												previous.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/kauppa myyntihistoria " + id + " " + (page - 1)));
											}
											else {
												previous.setColor(ChatColor.GRAY);
											}
											TextComponent next = new TextComponent("[ > Seuraava > ]");
											if (page < pages) {
												next.setColor(ChatColor.GREEN);
												next.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/kauppa myyntihistoria " + id + " " + (page + 1)));
											}
											else {
												next.setColor(ChatColor.GRAY);
											}
											start.addExtra(space);
											start.addExtra(previous);
											start.addExtra(space);
											start.addExtra(next);
											player.spigot().sendMessage(start);
											player.sendMessage("");
										}
										else {
											player.sendMessage(tc3 + " Ei myyntihistoriaa!");
											player.sendMessage("");
										}
									}
									else {
										player.sendMessage(tc3 + "Virheellinen sivunumero!");
									}
								}
								else {
									player.sendMessage(tc3 + "Et ole vuokrannut tätä torikojua/liikekiinteistöä!");
								}
							}
							else {
								player.sendMessage(tc3 + "Ei löydetty torikojua/liikekiinteistöä antamallasi ID:llä!");
							}
						}
						catch (NumberFormatException e) {
							player.sendMessage(tc3 + "Virheellinen ID tai sivunumero!");
						}
					}
					else if (args.length >= 2) {
						player.performCommand("kauppa myyntihistoria " + args[1] + " 1");
					}
					else {
						player.sendMessage(usage + "/kauppa myyntihistoria <ID> [sivu]");
					}
				}
				else if (args[0].equalsIgnoreCase("nimeä")) {
					if (args.length >= 2) {
						try {
							int id = Integer.parseInt(args[1]);
							MarketStall marketStall = economy.getMarketManager().getMarketStall(id);
							if (marketStall != null) {
								if (marketStall.isRented() && marketStall.getOwnerUuid().equals(player.getUniqueId().toString())) {
									if (marketStall.getType() == MarketStallType.LIIKEKIINTEISTÖ) {
										int identifier = new Random().nextInt(10000);
										economy.getMarketListener().getNaming().put(player, id + ":" + identifier);
										player.sendMessage("");
										player.sendMessage(tc2 + "Kirjoita " + tc1 + "chattiin" + tc2 + ", minkä nimen haluat antaa liikekiinteistöllesi:");
										new BukkitRunnable() {
											public void run() {
												if (economy.getMarketListener().getNaming().containsKey(player) && economy.getMarketListener().getNaming().get(player).equals(id + ":" + identifier)) {
													player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
													player.sendMessage(tc3 + "Liikekiinteistön nimeäminen peruttu!");
													economy.getMarketListener().getNaming().remove(player);
												}
											}
										}.runTaskLater(economy, 280);
									}
									else {
										player.sendMessage(tc3 + "Voit nimetä ainoastaan liikekiinteistöjä!");
									}
								}
								else {
									player.sendMessage(tc3 + "Et ole vuokrannut tätä torikojua/liikekiinteistöä!");
								}
							}
							else {
								player.sendMessage(tc3 + "Ei löydetty torikojua/liikekiinteistöä antamallasi ID:llä!");
							}
						}
						catch (NumberFormatException e) {
							player.sendMessage(tc3 + "Virheellinen ID!");
						}
					}
					else {
						player.sendMessage(usage + "/liikekiinteistö nimeä <ID>");
					}
				}
				else {
					if (CoreUtils.hasRank(player, "ylläpitäjä")) {
						player.sendMessage(usage + "/kauppa <info/aseta/poista/työkalu/päivitä/reload/tiedot/esikatsele/myyntihistoria/nimeä>");
					}
					else {
						player.performCommand("kauppa");
					}
				}
			}
			else {
				List<MarketStall> torikojut = new ArrayList<MarketStall>();
				List<MarketStall> liikekiinteistöt = new ArrayList<MarketStall>();
				for (MarketStall marketStall : economy.getMarketManager().getMarketStalls()) {
					if (marketStall.isRented() && marketStall.getOwnerUuid().equals(player.getUniqueId().toString())) {
						if (marketStall.getType() == MarketStallType.TORIKOJU) {
							torikojut.add(marketStall);
						}
						else if (marketStall.getType() == MarketStallType.LIIKEKIINTEISTÖ) {
							liikekiinteistöt.add(marketStall);
						}
					}
				}
				player.sendMessage("");
				player.sendMessage(tc2 + "§m----------" + tc1 + " Torikojut ja liikekiinteistöt " + tc2 + "§m----------");
				player.sendMessage("");
				if (torikojut.isEmpty()) {
					player.sendMessage(tc3 + " Et ole vuokrannut yhtäkään torikojua!");
				}
				else {
					player.sendMessage(tc2 + " Sinulla on " + tc1 + torikojut.size() + "/3" + tc2 + " vuokrattua torikojua!");
					int i = 1;
					for (MarketStall marketStall : torikojut) {
						TextComponent start = new TextComponent("  - ");
						start.setColor(ChatColor.getByChar(tc1.charAt(1)));
						TextComponent name = new TextComponent("Torikoju " + i + " ");
						name.setColor(ChatColor.getByChar(tc2.charAt(1)));
						TextComponent button = new TextComponent("[Näytä]");
						button.setColor(ChatColor.getByChar(tc1.charAt(1)));
						button.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/kauppa tiedot " + marketStall.getId()));
						button.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(tc1 + "Näytä tämän torikojun tiedot klikkaamalla!").create()));
						start.addExtra(name);
						start.addExtra(button);
						player.spigot().sendMessage(start);
						i++;
					}
				}
				player.sendMessage("");
				if (liikekiinteistöt.isEmpty()) {
					player.sendMessage(tc3 + " Et ole vuokrannut yhtäkään liikekiinteistöä!");
				}
				else {
					player.sendMessage(tc2 + " Sinulla on " + tc1 + liikekiinteistöt.size() + "/1" + tc2 + " vuokrattua liikekiinteistöä!");
					for (MarketStall marketStall : liikekiinteistöt) {
						TextComponent start = new TextComponent("  - ");
						start.setColor(ChatColor.getByChar(tc1.charAt(1)));
						TextComponent name = new TextComponent("Liikekiinteistö ");
						name.setColor(ChatColor.getByChar(tc2.charAt(1)));
						TextComponent button = new TextComponent("[Näytä]");
						button.setColor(ChatColor.getByChar(tc1.charAt(1)));
						button.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/kauppa tiedot " + marketStall.getId()));
						button.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(tc1 + "Näytä tämän liikekiinteistön tiedot klikkaamalla!").create()));
						start.addExtra(name);
						start.addExtra(button);
						player.spigot().sendMessage(start);
					}
				}
				player.sendMessage("");
			}
		}
		return true;
	}
}