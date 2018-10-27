package me.t4tu.rkeconomy.markets;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkeconomy.Economy;

public class MarketManager {
	
	private Economy economy;
	
	private List<MarketStall> marketStalls = new ArrayList<MarketStall>();
	
	public MarketManager(Economy economy) {
		this.economy = economy;
	}
	
	public List<MarketStall> getMarketStalls() {
		return marketStalls;
	}
	
	public List<MarketStall> getMarketStalls(Player player) {
		List<MarketStall> stalls = new ArrayList<MarketStall>();
		for (MarketStall marketStall : marketStalls) {
			if (marketStall.getOwnerUuid() != null && marketStall.getOwnerUuid().equals(player.getUniqueId().toString())) {
				stalls.add(marketStall);
			}
		}
		return stalls;
	}
	
	public MarketStall getMarketStall(int id) {
		for (MarketStall marketStall : marketStalls) {
			if (marketStall.getId() == id) {
				return marketStall;
			}
		}
		return null;
	}
	
	public MarketStall getMarketStallBySign(Sign sign) {
		Location location = sign.getLocation();
		for (MarketStall marketStall : marketStalls) {
			if (location.equals(marketStall.getSignLocation())) {
				return marketStall;
			}
		}
		return null;
	}
	
	public SellingPoint getSellingPointByFrame(ItemFrame frame) {
		Location location = frame.getLocation();
		for (MarketStall marketStall : marketStalls) {
			for (SellingPoint sellingPoint : marketStall.getSellingPoints()) {
				if (sellingPoint.getWorld().equals(location.getWorld().getName()) && sellingPoint.getX() == location.getBlockX() && sellingPoint.getY() == location.getBlockY() && 
						sellingPoint.getZ() == location.getBlockZ() && sellingPoint.getFacing().equals(frame.getFacing().toString())) {
					return sellingPoint;
				}
			}
		}
		return null;
	}
	
	public SellingPoint getSellingPointByInventory(Inventory inventory) {
		for (MarketStall marketStall : marketStalls) {
			for (SellingPoint sellingPoint : marketStall.getSellingPoints()) {
				if (sellingPoint.getInventory().equals(inventory)) {
					return sellingPoint;
				}
			}
		}
		return null;
	}
	
	public MarketStall newMarketStall(int id, MarketStallType type, Location signLocation, List<ItemFrame> frames) {
		MarketStall marketStall = new MarketStall(id, type, null, null, null, false, 0, 0, signLocation, null, new ArrayList<String>(), new ArrayList<ItemStack>(), null, economy);
		List<SellingPoint> sellingPoints = new ArrayList<SellingPoint>();
		int i = 0;
		for (ItemFrame frame : frames) {
			frame.setItem(null);
			SellingPoint sellingPoint = new SellingPoint(i, frame.getLocation().getWorld().getName(), frame.getFacing().toString(), frame.getLocation().getBlockX(), 
					frame.getLocation().getBlockY(), frame.getLocation().getBlockZ(), 1, 1, null, marketStall, economy);
			sellingPoint.reset();
			sellingPoints.add(sellingPoint);
			i++;
		}
		marketStall.setSellingPoints(sellingPoints);
		marketStall.save();
		marketStall.updateSign();
		marketStalls.add(marketStall);
		return marketStall;
	}
	
	public void loadMarketStallsFromConfig() {
		marketStalls.clear();
		if (economy.getConfig().getConfigurationSection("stalls") != null) {
			for (String s : economy.getConfig().getConfigurationSection("stalls").getKeys(false)) {
				try {
					int id = Integer.parseInt(s);
					MarketStallType type = MarketStallType.valueOf(economy.getConfig().getString("stalls." + s + ".type").toUpperCase());
					String name = economy.getConfig().getString("stalls." + s + ".name");
					String ownerUuid = economy.getConfig().getString("stalls." + s + ".owner-uuid");
					String ownerName = economy.getConfig().getString("stalls." + s + ".owner-name");
					String previousOwner = economy.getConfig().getString("stalls." + s + ".previous-owner");
					boolean rented = economy.getConfig().getBoolean("stalls." + s + ".rented");
					long expires = economy.getConfig().getLong("stalls." + s + ".expires");
					double profit = economy.getConfig().getDouble("stalls." + s + ".profit");
					Location signLocation = CoreUtils.loadLocation(economy, "stalls." + s + ".sign-location");
					List<String> history = economy.getConfig().getStringList("stalls." + s + ".history");
					MarketStall marketStall = new MarketStall(id, type, name, ownerUuid, ownerName, rented, expires, profit, signLocation, null, history, null, previousOwner, economy);
					List<SellingPoint> sellingPoints = new ArrayList<SellingPoint>();
					if (economy.getConfig().getConfigurationSection("stalls." + s + ".selling-points") != null) {
						for (String s2 : economy.getConfig().getConfigurationSection("stalls." + s + ".selling-points").getKeys(false)) {
							String path = "stalls." + s + ".selling-points." + s2;
							int number = Integer.parseInt(s2);
							String world = economy.getConfig().getString(path + ".world");
							String facing = economy.getConfig().getString(path + ".facing");
							int x = economy.getConfig().getInt(path + ".x");
							int y = economy.getConfig().getInt(path + ".y");
							int z = economy.getConfig().getInt(path + ".z");
							int amount = economy.getConfig().getInt(path + ".amount");
							double price = economy.getConfig().getDouble(path + ".price");
							int size = 27;
							ItemStack[] items = new ItemStack[size];
							for (int i = 0; i < size; i++) {
								ItemStack item = (ItemStack) economy.getConfig().getList(path + ".inventory").get(i);
								if (item != null) {
									items[i] = item.clone();
								}
								else {
									items[i] = null;
								}
							}
							Inventory inventory = Bukkit.createInventory(null, size, "Muokkaa myyntipistettä");
							inventory.setContents(items);
							SellingPoint sellingPoint = new SellingPoint(number, world, facing, x, y, z, amount, price, inventory, marketStall, economy);
							sellingPoints.add(sellingPoint);
						}
					}
					marketStall.setSellingPoints(sellingPoints);
					List<ItemStack> lostAndFound = new ArrayList<ItemStack>();
					if (economy.getConfig().getConfigurationSection("stalls." + s + ".lost-and-found") != null) {
						for (int i = 0; i < economy.getConfig().getList("stalls." + s + ".lost-and-found").size(); i++) {
							ItemStack item = (ItemStack) economy.getConfig().getList("stalls." + s + ".lost-and-found").get(i);
							lostAndFound.add(item.clone());
						}
					}
					marketStall.setLostAndFound(lostAndFound);
					marketStalls.add(marketStall);
				}
				catch (Exception e) {
					Bukkit.getConsoleSender().sendMessage("Virhe ladattaessa torikojua/liikekiinteistöä ID:llä '" + s + "'");
				}
			}
		}
	}
}