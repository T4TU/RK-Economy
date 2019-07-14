package me.t4tu.rkeconomy.markets;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.meowj.langutils.lang.LanguageHelper;

import me.t4tu.rkcore.inventories.InventoryGUI;
import me.t4tu.rkcore.inventories.InventoryGUIAction;
import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkcore.utils.MySQLUtils;
import me.t4tu.rkcore.utils.SettingsUtils;
import me.t4tu.rkeconomy.Economy;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class MarketListener implements Listener {
	
	private Economy economy;
	private Map<Player, String> name;
	private Map<Player, String> buy;
	private Map<Player, String> price;
	
	public MarketListener(Economy economy) {
		this.economy = economy;
		name = new HashMap<Player, String>();
		buy = new HashMap<Player, String>();
		price = new HashMap<Player, String>();
	}
	
	public Map<Player, String> getNaming() {
		return name;
	}
	
	public Map<Player, String> getBuying() {
		return buy;
	}
	
	public Map<Player, String> getPricing() {
		return price;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		
		String tc3 = CoreUtils.getErrorBaseColor();
		
		Player player = e.getPlayer();
		
		if (name.containsKey(player)) {
			e.setCancelled(true);
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			player.sendMessage(tc3 + "Liikekiinteistön nimeäminen peruttu!");
			name.remove(player);
		}
		
		if (buy.containsKey(player) && !e.getMessage().startsWith("/kauppa esikatsele ")) {
			e.setCancelled(true);
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			player.sendMessage(tc3 + "Ostaminen peruttu!");
			buy.remove(player);
		}
		
		if (price.containsKey(player)) {
			e.setCancelled(true);
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			player.sendMessage(tc3 + "Hinnan asettaminen peruttu!");
			price.remove(player);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		
		Player player = e.getPlayer();
		
		if (name.containsKey(player)) {
			e.setCancelled(true);
			try {
				int id = Integer.parseInt(name.get(player).split(":")[0]);
				MarketStall marketStall = economy.getMarketManager().getMarketStall(id);
				String message = e.getMessage();
				if (message.length() <= 20) {
					marketStall.setName(message);
					marketStall.save();
					player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
					player.sendMessage(tc2 + "Asetit liikekiinteistösi nimeksi " + tc1 + ChatColor.translateAlternateColorCodes('&', message) + tc2 + "!");
					name.remove(player);
					new BukkitRunnable() {
						public void run() {
							marketStall.updateSign();
						}
					}.runTask(economy);
				}
				else {
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					player.sendMessage(tc3 + "Antamasi nimi on liian pitkä. Nimen maksimipituus on 20 merkkiä. (Huomioi, että liian pitkät nimet eivät näy kylteissä oikein.)");
					name.remove(player);
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				player.sendMessage(tc3 + "Tapahtui virhe! Ota yhteyttä ylläpitoon.");
				name.remove(player);
			}
		}
		
		if (buy.containsKey(player)) {
			e.setCancelled(true);
			try {
				int stallId = Integer.parseInt(buy.get(player).split(":")[0]);
				int pointId = Integer.parseInt(buy.get(player).split(":")[1]);
				int amount = Integer.parseInt(e.getMessage());
				MarketStall marketStall = economy.getMarketManager().getMarketStall(stallId);
				SellingPoint sellingPoint = marketStall.getSellingPoint(pointId);
				int amountInStorage = sellingPoint.getAmountInStorage();
				if (amount > 0) {
					amount *= sellingPoint.getAmount();
					if (amount <= amountInStorage) {
						ItemStack item = sellingPoint.getPreviewItemStack();
						item.setAmount(amount);
						int endPrice = amount * sellingPoint.getPrice() / sellingPoint.getAmount();
						if (CoreUtils.hasEnoughRoom(player, item, amount, economy.SILVER_COIN, 9)) {
							if (economy.takeCash(player, endPrice)) {
								String itemName = LanguageHelper.getItemDisplayName(item, "fi_FI");
								player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
								player.sendMessage(tc2 + "Ostit " + tc1 + amount + tc2 + " kappaletta tuotetta " + tc1 + itemName + tc2 + " hintaan " + tc1 + Economy.moneyAsString(endPrice) + tc2 + "!");
								player.getInventory().addItem(item);
								CoreUtils.removeItems(sellingPoint.getInventory(), item, amount);
								marketStall.setProfit(marketStall.getProfit() + endPrice);
								SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm");
								Date date = new Date(System.currentTimeMillis() + CoreUtils.TIME_OFFSET);
								String logEntry = "[" + f.format(date) + "] " + player.getName() + ": " + amount + "kpl " + ChatColor.stripColor(itemName) + " (+" + Economy.moneyAsString(endPrice) + ")";
								marketStall.getHistory().add(logEntry);
								marketStall.save();
								Player owner = Bukkit.getPlayer(UUID.fromString(marketStall.getOwnerUuid()));
								if (owner != null) {
									if (SettingsUtils.getSetting(owner, "show_bought_items")) {
										owner.sendMessage(tc1 + player.getName() + tc2 + " osti sinulta " + tc1 + amount + tc2 + " kappaletta tuotetta " + tc1 + itemName + tc2 + 
												" hintaan " + tc1 + Economy.moneyAsString(endPrice) + tc2 + "!");
									}
								}
								new BukkitRunnable() {
									public void run() {
										MySQLUtils.set("UPDATE player_stats SET money=money+" + endPrice + " WHERE uuid=?", marketStall.getOwnerUuid());
									}
								}.runTaskAsynchronously(economy);
							}
							else {
								player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
							}
						}
						else {
							player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
							player.sendMessage(tc3 + "Tavaraluettelossasi ei ole tarpeeksi tilaa!");
						}
					}
					else {
						player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
						player.sendMessage(tc3 + "Varastossa on jäljellä vain " + amountInStorage + " tuotetta!");
					}
				}
				else {
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					player.sendMessage(tc3 + "Virheellinen määrä, ostaminen peruttu!");
				}
			}
			catch (NumberFormatException ex) {
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				player.sendMessage(tc3 + "Virheellinen määrä, ostaminen peruttu!");
			}
			catch (Exception ex) {
				ex.printStackTrace();
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				player.sendMessage(tc3 + "Tapahtui virhe! Ota yhteyttä ylläpitoon.");
			}
			buy.remove(player);
		}
		
		if (price.containsKey(player)) {
			e.setCancelled(true);
			try {
				int stallId = Integer.parseInt(price.get(player).split(":")[0]);
				int pointId = Integer.parseInt(price.get(player).split(":")[1]);
				int price = Economy.moneyAsInt(Double.parseDouble(e.getMessage().replace(",", ".")));
				MarketStall marketStall = economy.getMarketManager().getMarketStall(stallId);
				SellingPoint sellingPoint = marketStall.getSellingPoint(pointId);
				if (price > 0) {
					player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
					player.sendMessage(tc2 + "Asetettiin myyntipisteen hinnaksi " + tc1 + Economy.moneyAsString(price) + tc2 + "!");
					sellingPoint.setPrice(price);
					sellingPoint.save();
					new BukkitRunnable() {
						public void run() {
							player.openInventory(sellingPoint.getInventory());
						}
					}.runTask(economy);
				}
				else {
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					player.sendMessage(tc3 + "Virheellinen hinta, hinnan asettaminen peruttu!");
				}
			}
			catch (NumberFormatException ex) {
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				player.sendMessage(tc3 + "Virheellinen hinta, hinnan asettaminen peruttu!");
			}
			catch (Exception ex) {
				ex.printStackTrace();
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				player.sendMessage(tc3 + "Tapahtui virhe! Ota yhteyttä ylläpitoon.");
			}
			price.remove(player);
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		for (MarketStall marketStall : economy.getMarketManager().getMarketStalls(e.getPlayer())) {
			marketStall.setOwnerName(e.getPlayer().getName());
			marketStall.updateSign();
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		
		if (e.getView() != null && e.getView().getTitle() != null && e.getView().getTitle().equals("Tuotteen esikatselu")) {
			e.setCancelled(true);
		}
		
		SellingPoint sellingPoint = economy.getMarketManager().getSellingPointByInventory(e.getClickedInventory());
		if (sellingPoint != null) {
			MarketStall marketStall = sellingPoint.getOwner();
			Player player = (Player) e.getWhoClicked();
			if (CoreUtils.getDisplayName(e.getCurrentItem()).equals("§a§lHinnan tyyppi")) {
				e.setCancelled(true);
				try {
					Inventory inventory = e.getInventory();
					ItemStack item = inventory.getItem(25);
					List<String> lore = item.getItemMeta().getLore();
					if (lore.get(0).startsWith("§a")) {
						inventory.setItem(25, CoreUtils.getItem(Material.OAK_SIGN, "§a§lHinnan tyyppi", Arrays.asList("§7  кк/1kpl", "§a> кк/10kpl <", "§7  кк/64kpl"), 1));
						sellingPoint.setAmount(10);
					}
					else if (lore.get(1).startsWith("§a")) {
						inventory.setItem(25, CoreUtils.getItem(Material.OAK_SIGN, "§a§lHinnan tyyppi", Arrays.asList("§7  кк/1kpl", "§7  кк/10kpl", "§a> кк/64kpl <"), 1));
						sellingPoint.setAmount(64);
					}
					else {
						inventory.setItem(25, CoreUtils.getItem(Material.OAK_SIGN, "§a§lHinnan tyyppi", Arrays.asList("§a> кк/1kpl <", "§7  кк/10kpl", "§7  кк/64kpl"), 1));
						sellingPoint.setAmount(1);
					}
					player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 2);
					sellingPoint.save();
				}
				catch (Exception ex) {
					e.getWhoClicked().sendMessage(tc3 + "Tapahtui virhe! Ota yhteyttä ylläpitoon.");
				}
			}
			if (CoreUtils.getDisplayName(e.getCurrentItem()).equals("§a§lAseta hinta")) {
				e.setCancelled(true);
				player.closeInventory();
				player.sendMessage(tc2 + "Kirjoita " + tc1 + "chattiin" + tc2 + ", minkä hinnan haluat asettaa tämän myyntipisteen tuotteille:");
				int identifier = new Random().nextInt(10000);
				String data = marketStall.getId() + ":" + sellingPoint.getId() + ":" + identifier;
				price.put(player, data);
				new BukkitRunnable() {
					public void run() {
						if (price.containsKey(player) && price.get(player).equals(data)) {
							player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
							player.sendMessage(tc3 + "Hinnan asettaminen peruttu!");
							price.remove(player);
						}
					}
				}.runTaskLater(economy, 140);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		
		Player player = e.getPlayer();
		
		if (e.getRightClicked() instanceof ItemFrame) {
			ItemFrame frame = (ItemFrame) e.getRightClicked();
			SellingPoint sellingPoint = economy.getMarketManager().getSellingPointByFrame(frame);
			if (sellingPoint != null) {
				e.setCancelled(true);
				MarketStall marketStall = sellingPoint.getOwner();
				if (marketStall.isRented()) {
					if (marketStall.getOwnerUuid().equals(player.getUniqueId().toString()) || (CoreUtils.hasAdminPowers(player) && player.isSneaking())) {
						player.openInventory(sellingPoint.getInventory());
						player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1);
					}
					else {
						new BukkitRunnable() {
							public void run() {
								String ownerName = marketStall.getOwnerName();
								ItemStack preview = sellingPoint.getPreviewItemStack();
								String product;
								if (preview != null) {
									product = LanguageHelper.getItemDisplayName(preview, "fi_FI");
								}
								else {
									product = "§cTuntematon";
								}
								int price = sellingPoint.getPrice();
								int amount = sellingPoint.getAmount();
								int amountInStorage = sellingPoint.getAmountInStorage();
								player.sendMessage("");
								player.sendMessage(tc2 + "§m----------" + tc1 + " Osta tuotetta " + tc2 + "§m----------");
								player.sendMessage("");
								player.sendMessage(tc2 + " Myyjä: " + tc1 + ownerName);
								TextComponent t = new TextComponent(" Tuote: ");
								t.setColor(ChatColor.getByChar(tc2.charAt(1)));
								TextComponent t2 = new TextComponent(product + " (Esikatsele klikkaamalla)");
								t2.setColor(ChatColor.YELLOW);
								t2.setClickEvent(new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/kauppa esikatsele " + marketStall.getId() + " " + sellingPoint.getId()));
								t2.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Klikkaa tästä esikatsellaksesi tuotetta!").color(ChatColor.YELLOW).create()));
								t.addExtra(t2);
								player.spigot().sendMessage(t);
								player.sendMessage(tc2 + " Hinta: " + tc1 + Economy.moneyAsString(price) + "/" + amount + "kpl");
								player.sendMessage(tc2 + " Varastossa: " + tc1 + amountInStorage + " kpl");
								player.sendMessage("");
								if (amountInStorage == 0) {
									player.sendMessage(tc3 + " Tämä tuote on loppuunmyyty!");
									player.sendMessage("");
								}
								else {
									if (amount == 1) {
										player.sendMessage(tc2 + "Kirjoita " + tc1 + "chattiin" + tc2 + ", kuinka monta " + tc1 + "kappaletta" + tc2 + " haluat ostaa:");
									}
									else {
										player.sendMessage(tc2 + "Kirjoita " + tc1 + "chattiin" + tc2 + ", kuinka monta " + tc1 + sellingPoint.getAmount() + " kappaleen erää" + tc2 + " haluat ostaa:");
									}
									int identifier = new Random().nextInt(10000);
									String data = marketStall.getId() + ":" + sellingPoint.getId() + ":" + identifier;
									buy.put(player, data);
									new BukkitRunnable() {
										public void run() {
											if (buy.containsKey(player) && buy.get(player).equals(data)) {
												player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
												player.sendMessage(tc3 + "Ostaminen peruttu!");
												buy.remove(player);
											}
										}
									}.runTaskLater(economy, 140);
								}
							}
						}.runTaskAsynchronously(economy);
					}
				}
			}
		}
		else if (CoreUtils.isNPCAndNamed(e.getRightClicked(), "§2Löytötavarat")) {
			openLostAndFoundMenu(player);
		}
	}
	
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		
		if (!(e.getPlayer() instanceof Player)) {
			return;
		}
		
		String tc3 = CoreUtils.getErrorBaseColor();
		
		Player player = (Player) e.getPlayer();
		
		SellingPoint sellingPoint = economy.getMarketManager().getSellingPointByInventory(e.getInventory());
		if (sellingPoint != null) {
			ItemFrame frame = sellingPoint.getFrame();
			if (frame != null) {
				frame.setSilent(true);
				ItemStack preview = null;
				for (int i = 0; i < sellingPoint.getInventory().getContents().length - 2; i++) {
					ItemStack item = sellingPoint.getInventory().getContents()[i];
					if (CoreUtils.isNotAir(item)) {
						preview = item.clone();
						preview.setAmount(1);
						ItemMeta meta = preview.getItemMeta();
						meta.setDisplayName("§a" + LanguageHelper.getItemDisplayName(preview, "fi_FI") + "§6 " + Economy.moneyAsString(sellingPoint.getPrice()) + "/" + sellingPoint.getAmount() + "kpl");
						preview.setItemMeta(meta);
						break;
					}
				}
				if (preview != null) {
					frame.setItem(preview);
				}
				else {
					frame.setItem(null);
				}
			}
			player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.5f, 1);
			ItemStack first = null;
			for (int i = 0; i < sellingPoint.getInventory().getContents().length - 2; i++) {
				ItemStack item = sellingPoint.getInventory().getContents()[i];
				if (CoreUtils.isNotAir(item)) {
					first = item.clone();
					break;
				}
			}
			if (first != null) {
				boolean b = false;
				for (int i = 0; i < sellingPoint.getInventory().getContents().length - 2; i++) {
					ItemStack item = sellingPoint.getInventory().getItem(i);
					if (CoreUtils.isNotAir(item)) {
						if (!first.isSimilar(item)) {
							Location location = sellingPoint.getLocationCenter();
							location.getWorld().dropItemNaturally(location, item);
							sellingPoint.getInventory().setItem(i, null);
							b = true;
						}
					}
				}
				if (b) {
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					player.sendMessage(tc3 + "Yhdessä myyntipisteessä voi myydä vain yhdenlaisia esineitä!");
				}
			}
			sellingPoint.save();
		}
	}
	
	@EventHandler
	public void onHangingBreak(HangingBreakEvent e) {
		if (e.getEntity() instanceof ItemFrame) {
			ItemFrame frame = (ItemFrame) e.getEntity();
			SellingPoint sellingPoint = economy.getMarketManager().getSellingPointByFrame(frame);
			if (sellingPoint != null) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof ItemFrame) {
			ItemFrame frame = (ItemFrame) e.getEntity();
			SellingPoint sellingPoint = economy.getMarketManager().getSellingPointByFrame(frame);
			if (sellingPoint != null) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = e.getClickedBlock();
			if (block.getState() instanceof Sign) {
				Sign sign = (Sign) block.getState();
				MarketStall marketStall = economy.getMarketManager().getMarketStallBySign(sign);
				if (marketStall != null) {
					if (marketStall.isRented() && marketStall.getOwnerUuid().equals(e.getPlayer().getUniqueId().toString())) {
						e.getPlayer().performCommand("kauppa tiedot " + marketStall.getId());
					}
					else if (!marketStall.isRented()) {
						InventoryGUI gui = new InventoryGUI(45, "Vuokraa " + marketStall.getType().getFriendlyName().toLowerCase());
						if (marketStall.getType() == MarketStallType.TORIKOJU) {
							int price = marketStall.getSellingPoints().size() * Economy.TORIKOJU_PRICE;
							gui.addItem(CoreUtils.getItem(Material.BOOK, "§a" + marketStall.getType().getFriendlyName(), Arrays.asList("", "§aMyyntipisteitä: §7" + 
									marketStall.getSellingPoints().size() + " kpl", "§aHinta/päivä: §7" + Economy.moneyAsString(price)), 1), 13, null);
							int price3 = marketStall.getSellingPoints().size() * Economy.TORIKOJU_PRICE * 3;
							gui.addItem(CoreUtils.getItem(Material.OAK_SIGN, "§a§lVuokraa 3 päiväksi", Arrays.asList("§7Hinta: §a" + Economy.moneyAsString(price3)), 1), 
									29, new InventoryGUIAction() {
								public void onClickAsync() { }
								public void onClick() {
									gui.close(e.getPlayer());
									rent(marketStall, 3, e.getPlayer());
								}
							});
							int price7 = marketStall.getSellingPoints().size() * Economy.TORIKOJU_PRICE * 7;
							gui.addItem(CoreUtils.getItem(Material.OAK_SIGN, "§a§lVuokraa 7 päiväksi", Arrays.asList("§7Hinta: §a" + Economy.moneyAsString(price7)), 1), 
									31, new InventoryGUIAction() {
								public void onClickAsync() { }
								public void onClick() {
									gui.close(e.getPlayer());
									rent(marketStall, 7, e.getPlayer());
								}
							});
							int price14 = marketStall.getSellingPoints().size() * Economy.TORIKOJU_PRICE * 14;
							gui.addItem(CoreUtils.getItem(Material.OAK_SIGN, "§a§lVuokraa 14 päiväksi", Arrays.asList("§7Hinta: §a" + Economy.moneyAsString(price14)), 1), 
									33, new InventoryGUIAction() {
								public void onClickAsync() { }
								public void onClick() {
									gui.close(e.getPlayer());
									rent(marketStall, 14, e.getPlayer());
								}
							});
						}
						else if (marketStall.getType() == MarketStallType.LIIKEKIINTEISTÖ) {
							int price = marketStall.getSellingPoints().size() * Economy.TORIKOJU_PRICE;
							gui.addItem(CoreUtils.getItem(Material.BOOK, "§a" + marketStall.getType().getFriendlyName(), Arrays.asList("", "§aMyyntipisteitä: §7" + 
									marketStall.getSellingPoints().size() + " kpl", "§aHinta/päivä: §7" + Economy.moneyAsString(price)), 1), 13, null);
							int price7 = marketStall.getSellingPoints().size() * Economy.TORIKOJU_PRICE * 7;
							gui.addItem(CoreUtils.getItem(Material.OAK_SIGN, "§a§lVuokraa 7 päiväksi", Arrays.asList("§7Hinta: §a" + Economy.moneyAsString(price7)), 1), 
									29, new InventoryGUIAction() {
								public void onClickAsync() { }
								public void onClick() {
									gui.close(e.getPlayer());
									rent(marketStall, 7, e.getPlayer());
								}
							});
							int price14 = marketStall.getSellingPoints().size() * Economy.TORIKOJU_PRICE * 14;
							gui.addItem(CoreUtils.getItem(Material.OAK_SIGN, "§a§lVuokraa 14 päiväksi", Arrays.asList("§7Hinta: §a" + Economy.moneyAsString(price14)), 1), 
									31, new InventoryGUIAction() {
								public void onClickAsync() { }
								public void onClick() {
									gui.close(e.getPlayer());
									rent(marketStall, 14, e.getPlayer());
								}
							});
							int price28 = marketStall.getSellingPoints().size() * Economy.TORIKOJU_PRICE * 28;
							gui.addItem(CoreUtils.getItem(Material.OAK_SIGN, "§a§lVuokraa 28 päiväksi", Arrays.asList("§7Hinta: §a" + Economy.moneyAsString(price28)), 1), 
									33, new InventoryGUIAction() {
								public void onClickAsync() { }
								public void onClick() {
									gui.close(e.getPlayer());
									rent(marketStall, 28, e.getPlayer());
								}
							});
						}
						gui.open(e.getPlayer());
					}
				}
			}
		}
	}
	
	private void rent(MarketStall marketStall, int days, Player player) {
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		
		if (!marketStall.isRented()) {
			
			int i = 0;
			for (MarketStall stall : economy.getMarketManager().getMarketStalls()) {
				if (stall.isRented() && stall.getOwnerUuid().equals(player.getUniqueId().toString())) {
					if (stall.getType() == marketStall.getType()) {
						i++;
					}
				}
			}
			
			if (marketStall.getType() == MarketStallType.TORIKOJU && i >= 3) {
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				player.sendMessage(tc3 + "Sinulla on jo maksimimäärä vuokrattuja torikojuja!");
				return;
			}
			else if (marketStall.getType() == MarketStallType.LIIKEKIINTEISTÖ && i >= 1) {
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				player.sendMessage(tc3 + "Sinulla on jo maksimimäärä vuokrattuja liikekiinteistöjä!");
				return;
			}
			
			int price = Economy.TORIKOJU_PRICE * marketStall.getSellingPoints().size() * days;
			if (marketStall.getType() == MarketStallType.LIIKEKIINTEISTÖ) {
				price = Economy.LIIKEKIINTEISTÖ_PRICE * marketStall.getSellingPoints().size() * days;
			}
			
			if (economy.takeCash(player, price)) {
				Economy.setStateMoney(Economy.getStateMoney() + price);
				String type = "torikojun";
				if (marketStall.getType() == MarketStallType.LIIKEKIINTEISTÖ) {
					type = "liikekiinteistön";
				}
				marketStall.setRented(true);
				marketStall.setOwnerUuid(player.getUniqueId().toString());
				marketStall.setOwnerName(player.getName());
				marketStall.setName(null);
				marketStall.setExpires(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * days);
				marketStall.setProfit(0);
				marketStall.setHistory(new ArrayList<String>());
				for (SellingPoint sellingPoint : marketStall.getSellingPoints()) {
					sellingPoint.reset();
				}
				marketStall.save();
				marketStall.updateSign();
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
				player.sendMessage(tc2 + "Vuokrasit itsellesi " + tc1 + type + " " + days + " päiväksi" + tc2 + "!");
			}
			else {
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			}
		}
		else {
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			player.sendMessage(tc3 + "Tämä " + marketStall.getType().getFriendlyName().toLowerCase() + " on jo vuokrattu jollekin!");
		}
	}
	
	private void openLostAndFoundMenu(Player player) {
		
		String tc3 = CoreUtils.getErrorBaseColor();
		
		InventoryGUI gui = new InventoryGUI(54, "Löytötavarat");
		List<ItemStack> items = getLostAndFoundItems(player);
		if (items.isEmpty()) {
			gui.addItem(CoreUtils.getItem(Material.RED_STAINED_GLASS_PANE, "§cEi löytötavaroita", Arrays.asList("", "§7Jos unohdat noutaa tavarasi torikojusta", "§7tai liikekiinteistöstä ennen kuin se", 
					"§7vanhenee, ilmestyvät tavarat tänne."), 1), 22, null);
		}
		else {
			for (int i = 0; i < items.size(); i++) {
				ItemStack item = items.get(i);
				if (i == 53) {
					gui.addItem(CoreUtils.getItem(Material.GRAY_STAINED_GLASS_PANE, "§8Ja " + (items.size() - 53) + " muuta tavaraa...", null, 1), 53, null);
				}
				else if (i < 53) {
					gui.addItem(item.clone(), i, new InventoryGUIAction() {
						public void onClickAsync() { }
						public void onClick() {
							if (CoreUtils.hasEnoughRoom(player, item, item.getAmount())) {
								if (redeemLostAndFoundItem(player, item)) {
									player.getInventory().addItem(item.clone());
									openLostAndFoundMenu(player);
								}
								else {
									player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
									player.sendMessage(tc3 + "Tapahtui virhe! Ota yhteyttä ylläpitoon.");
									openLostAndFoundMenu(player);
								}
							}
							else {
								player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
								player.sendMessage(tc3 + "Tavaraluettelossasi ei ole tarpeeksi tilaa tälle esineelle!");
							}
						}
					});
				}
			}
		}
		gui.open(player);
	}
	
	private List<ItemStack> getLostAndFoundItems(Player player) {
		List<ItemStack> items = new ArrayList<ItemStack>();
		for (MarketStall marketStall : economy.getMarketManager().getMarketStalls()) {
			if (marketStall.getPreviousOwner() != null && marketStall.getPreviousOwner().equals(player.getUniqueId().toString())) {
				items.addAll(marketStall.getLostAndFound());
			}
		}
		return items;
	}
	
	private boolean redeemLostAndFoundItem(Player player, ItemStack item) {
		for (MarketStall marketStall : economy.getMarketManager().getMarketStalls()) {
			if (marketStall.getPreviousOwner() != null && marketStall.getPreviousOwner().equals(player.getUniqueId().toString())) {
				ListIterator<ItemStack> iterator = marketStall.getLostAndFound().listIterator();
				while (iterator.hasNext()) {
					ItemStack stack = iterator.next();
					if (stack.equals(item)) {
						iterator.remove();
						marketStall.saveLostAndFound();
						return true;
					}
				}
			}
		}
		return false;
	}
}