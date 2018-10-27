package me.t4tu.rkeconomy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkcore.utils.MySQLResult;
import me.t4tu.rkcore.utils.MySQLUtils;
import me.t4tu.rkeconomy.banks.BankListener;
import me.t4tu.rkeconomy.markets.MarketCommand;
import me.t4tu.rkeconomy.markets.MarketListener;
import me.t4tu.rkeconomy.markets.MarketManager;
import me.t4tu.rkeconomy.markets.MarketStall;
import me.t4tu.rkeconomy.markets.MarketStallType;
import me.t4tu.rkeconomy.markets.SellingPoint;
import me.t4tu.rkeconomy.shops.ShopCommand;
import me.t4tu.rkeconomy.shops.ShopListener;
import me.t4tu.rkeconomy.shops.ShopManager;

public class Economy extends JavaPlugin {
	
	public static final double TORIKOJU_PRICE = 0.1;
	public static final double LIIKEKIINTEISTÖ_PRICE = 0.1;
	
	public final ItemStack GOLD_COIN = getGoldCoin();
	public final ItemStack SILVER_COIN = getSilverCoin();
	
	private EconomyCommand economyCommand;
	private BankListener bankListener;
	private MarketManager marketManager;
	private MarketCommand marketCommand;
	private MarketListener marketListener;
	private ShopManager shopManager;
	private ShopCommand shopCommand;
	private ShopListener shopListener;
	
	private void registerCommand(String s, CommandExecutor c, boolean tabCompletion) {
		getCommand(s).setExecutor(c);
		if (tabCompletion) {
			CoreUtils.getRegisteredCommandsWithTabCompletion().add(s);
		}
		else {
			CoreUtils.getRegisteredCommands().add(s);
		}
	}
	
	public void onEnable() {
		
		saveConfig();
		
		economyCommand = new EconomyCommand(this);
		bankListener = new BankListener(this);
		marketManager = new MarketManager(this);
		marketCommand = new MarketCommand(this);
		marketListener = new MarketListener(this);
		shopManager = new ShopManager(this);
		shopCommand = new ShopCommand(this);
		shopListener = new ShopListener(this);
		
		marketManager.loadMarketStallsFromConfig();
		shopManager.loadShopsFromConfig();
		
		Bukkit.getPluginManager().registerEvents(bankListener, this);
		Bukkit.getPluginManager().registerEvents(marketListener, this);
		Bukkit.getPluginManager().registerEvents(shopListener, this);
		
		registerCommand("rahat", economyCommand, true);
		registerCommand("raha", economyCommand, true);
		registerCommand("money", economyCommand, true);
		registerCommand("balance", economyCommand, true);
		registerCommand("bal", economyCommand, true);
		registerCommand("kultakolikko", economyCommand, false);
		registerCommand("hopeakolikko", economyCommand, false);
		registerCommand("shekki", economyCommand, true);
		registerCommand("sekki", economyCommand, true);
		registerCommand("kauppa", marketCommand, true);
		registerCommand("kaupat", marketCommand, true);
		registerCommand("torikoju", marketCommand, true);
		registerCommand("torikojut", marketCommand, true);
		registerCommand("tori", marketCommand, true);
		registerCommand("liikekiinteistö", marketCommand, true);
		registerCommand("liikekiinteistöt", marketCommand, true);
		registerCommand("shop", shopCommand, false);
		
		new BukkitRunnable() {
			public void run() {
				for (MarketStall marketStall : marketManager.getMarketStalls()) {
					if (marketStall.isRented()) {
						if (marketStall.getExpires() < System.currentTimeMillis()) {
							UUID uuid = UUID.fromString(marketStall.getOwnerUuid());
							Player player = Bukkit.getPlayer(uuid);
							if (player != null) {
								String tc3 = CoreUtils.getErrorBaseColor();
								if (marketStall.getType() == MarketStallType.TORIKOJU) {
									player.sendMessage(tc3 + "Torikojusi vuokra päättyi juuri! Jos et muistanut tyhjentää tavaroitasi, "
											+ "löydät ne minkä tahansa pankin löytötavaroista vielä §nkolmen" + tc3 + " päivän ajan.");
								}
								else if (marketStall.getType() == MarketStallType.LIIKEKIINTEISTÖ) {
									player.sendMessage(tc3 + "Liikekiinteistösi vuokra päättyi juuri! Jos et muistanut tyhjentää tavaroitasi, "
											+ "löydät ne minkä tahansa pankin löytötavaroista vielä §nseitsemän" + tc3 + " päivän ajan.");
								}
								player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
								if (player.getOpenInventory() != null && player.getOpenInventory().getTopInventory() != null) {
									SellingPoint sellingPoint = marketManager.getSellingPointByInventory(player.getOpenInventory().getTopInventory());
									if (sellingPoint != null) {
										if (sellingPoint.getOwner() == marketStall) {
											player.closeInventory();
										}
									}
								}
							}
							marketStall.setPreviousOwner(marketStall.getOwnerUuid());
							marketStall.setRented(false);
							marketStall.setOwnerUuid(null);
							marketStall.setOwnerName(null);
							marketStall.setName(null);
							marketStall.setExpires(0);
							marketStall.setProfit(0);
							marketStall.setHistory(new ArrayList<String>());
							marketStall.getLostAndFound().clear();
							for (SellingPoint sellingPoint : marketStall.getSellingPoints()) {
								ItemFrame frame = sellingPoint.getFrame();
								if (frame != null) {
									frame.setItem(null);
								}
								for (int i = 0; i < 25; i++) {
									ItemStack item = sellingPoint.getInventory().getContents()[i];
									if (CoreUtils.isNotAir(item)) {
										marketStall.getLostAndFound().add(item.clone());
									}
								}
								sellingPoint.reset();
							}
							marketStall.updateSign();
							marketStall.save();
						}
					}
				}
			}
		}.runTaskTimer(this, 100, 100);
	}
	
	public EconomyCommand getEconomyCommand() {
		return economyCommand;
	}
	
	public BankListener getBankListener() {
		return bankListener;
	}
	
	public MarketManager getMarketManager() {
		return marketManager;
	}
	
	public MarketCommand getMarketCommand() {
		return marketCommand;
	}
	
	public MarketListener getMarketListener() {
		return marketListener;
	}
	
	public ShopManager getShopManager() {
		return shopManager;
	}
	
	public ShopCommand getShopCommand() {
		return shopCommand;
	}
	
	public ShopListener getShopListener() {
		return shopListener;
	}
	
	public ItemStack getGoldCoin() {
		return getGoldCoin(1);
	}
	
	public ItemStack getGoldCoin(int amount) {
		ItemStack coin = new ItemStack(Material.DOUBLE_PLANT, amount);
		ItemMeta meta = coin.getItemMeta();
		meta.setDisplayName("§6Kultakolikko");
		meta.setLore(Arrays.asList("§8Arvo: 1£"));
		coin.setItemMeta(meta);
		return coin;
	}
	
	public ItemStack getSilverCoin() {
		return getSilverCoin(1);
	}
	
	public ItemStack getSilverCoin(int amount) {
		ItemStack coin = new ItemStack(Material.INK_SACK, amount);
		coin.setDurability((short) 8);
		ItemMeta meta = coin.getItemMeta();
		meta.setDisplayName("§7Hopeakolikko");
		meta.setLore(Arrays.asList("§8Arvo: 0.1£"));
		coin.setItemMeta(meta);
		return coin;
	}
	
	public int getGoldCoins(Player player) {
		int amount = 0;
		for (ItemStack item : player.getInventory().getContents()) {
			if (CoreUtils.isNotAir(item) && item.isSimilar(GOLD_COIN)) {
				amount += item.getAmount();
			}
		}
		return amount;
	}
	
	public int getSilverCoins(Player player) {
		int amount = 0;
		for (ItemStack item : player.getInventory().getContents()) {
			if (CoreUtils.isNotAir(item) && item.isSimilar(SILVER_COIN)) {
				amount += item.getAmount();
			}
		}
		return amount;
	}
	
	public boolean takeGoldCoins(Player player, int amount) {
		int c = 0;
		for (ItemStack i : player.getInventory().getContents()) {
			if (i == null || i.getType() == Material.AIR) {
				continue;
			}
			if (i.isSimilar(GOLD_COIN)) {
				if (i.getAmount() >= amount) {
					i.setAmount(i.getAmount() - amount);
					player.updateInventory();
					return true;
				}
				else {
					c += i.getAmount();
				}
			}
		}
		if (c >= amount) {
			c = amount;
			for (ItemStack i : player.getInventory().getContents()) {
				if (i == null || i.getType() == Material.AIR) {
					continue;
				}
				if (i.isSimilar(GOLD_COIN)) {
					if (i.getAmount() >= c) {
						i.setAmount(i.getAmount() - c);
						c = 0;
					}
					else {
						c -= i.getAmount();
						i.setAmount(0);
					}
				}
			}
			player.updateInventory();
			return true;
		}
		return false;
	}
	
	public boolean takeSilverCoins(Player player, int amount) {
		int c = 0;
		for (ItemStack i : player.getInventory().getContents()) {
			if (i == null || i.getType() == Material.AIR) {
				continue;
			}
			if (i.isSimilar(SILVER_COIN)) {
				if (i.getAmount() >= amount) {
					i.setAmount(i.getAmount() - amount);
					player.updateInventory();
					return true;
				}
				else {
					c += i.getAmount();
				}
			}
		}
		if (c >= amount) {
			c = amount;
			for (ItemStack i : player.getInventory().getContents()) {
				if (i == null || i.getType() == Material.AIR) {
					continue;
				}
				if (i.isSimilar(SILVER_COIN)) {
					if (i.getAmount() >= c) {
						i.setAmount(i.getAmount() - c);
						c = 0;
					}
					else {
						c -= i.getAmount();
						i.setAmount(0);
					}
				}
			}
			player.updateInventory();
			return true;
		}
		return false;
	}
	
	public boolean takeCash(Player player, double amount) {
		amount = round(amount, 1, RoundingMode.HALF_UP);
		String tc3 = CoreUtils.getErrorBaseColor();
		int goldCoins = getGoldCoins(player);
		int silverCoins = getSilverCoins(player);
		if (goldCoins >= amount) {
			int goldAmount = round(amount, RoundingMode.UP);
			takeGoldCoins(player, goldAmount);
			ItemStack change = getSilverCoin(round((goldAmount - amount) * 10, RoundingMode.DOWN));
			if (change.getAmount() != 0) {
				if (CoreUtils.hasEnoughRoom(player, change, change.getAmount())) {
					player.getInventory().addItem(change);
				}
				else {
					player.getInventory().addItem(getGoldCoin(goldAmount));
					player.sendMessage(tc3 + "Tavaraluettelossasi ei ole tarpeeksi tilaa!");
					return false;
				}
			}
			return true;
		}
		else if (goldCoins + (silverCoins / 10d) >= amount) {
			takeGoldCoins(player, goldCoins);
			takeSilverCoins(player, round((amount - goldCoins) * 10, RoundingMode.DOWN));
			return true;
		}
		player.sendMessage(tc3 + "Sinulla ei ole tarpeeksi käteistä rahaa!");
		return false;
	}
	
	public boolean hasAccount(String name) {
		MySQLResult statsData = MySQLUtils.get("SELECT money FROM player_stats WHERE name=?", name);
		return statsData != null;
	}
	
	public double getMoney(Player player) {
		MySQLResult statsData = MySQLUtils.get("SELECT money FROM player_stats WHERE uuid=?", player.getUniqueId().toString());
		if (statsData != null) {
			return round(statsData.getDouble(0, "money"), 1, RoundingMode.HALF_UP);
		}
		return 0;
	}
	
	public double getMoney(String name) {
		MySQLResult statsData = MySQLUtils.get("SELECT money FROM player_stats WHERE name=?", name);
		if (statsData != null) {
			return round(statsData.getDouble(0, "money"), 1, RoundingMode.HALF_UP);
		}
		return 0;
	}
	
	public void setMoney(Player player, double money) {
		money = round(money, 1, RoundingMode.HALF_UP);
		MySQLUtils.set("UPDATE player_stats SET money=? WHERE uuid=?", money + "", player.getUniqueId().toString());
	}
	
	public void setMoney(String name, double money) {
		money = round(money, 1, RoundingMode.HALF_UP);
		MySQLUtils.set("UPDATE player_stats SET money=? WHERE name=?", money + "", name);
	}
	
	public Double round(Double value, int scale, RoundingMode roundingMode) {
		return new BigDecimal(value.toString()).setScale(scale, roundingMode).doubleValue();
	}
	
	public Integer round(Double value, RoundingMode roundingMode) {
		return new BigDecimal(value.toString()).setScale(0, roundingMode).intValue();
	}
}